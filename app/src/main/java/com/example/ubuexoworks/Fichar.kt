package com.example.ubuexoworks

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.content.Context.*
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import com.example.awesomedialog.*
import com.example.ubuexoworks.ClasesDeDatos.Fichaje
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Clase que permite realizar el fichaje en la pestaña "Fichaje"
 * @author Alejandro Fraga Neila
 */
class Fichar : Fragment() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var service: ApiService
    private var longitud: String = ""
    private var latitud: String = ""
    private var idUsuario: Int = 0
    private var tokenUsuario: String? = ""
    private lateinit var builder: NotificationCompat.Builder
    private lateinit var barTimer: ProgressBar
    lateinit var fichajesBD: AyudaSQLite

    //Timer
    lateinit var ayudaContador: AyudaContador

    private val timer = Timer()
    private lateinit var tvTiempo: TextView

    //Id de la notificación
    private val ID_CANAL = "canal01"

    var gpsStatus: Boolean = false

    @RequiresApi(Build.VERSION_CODES.O)
    @RequiresPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_fichar, container, false)

        //Se obtienen los datos del id de usuario
        val preferences: SharedPreferences = requireActivity().getSharedPreferences("Login", MODE_PRIVATE)
        idUsuario = preferences.getInt("Id", 0)

        //Se obtiene el token para poder realizar llamadas a la Api con seguridad
        tokenUsuario = preferences.getString("Token","")

        //Creamos el servicio
        service = (activity as MainActivity).createApiService()

        //Servicio para la ubicación
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        //Botón para fichar
        view.findViewById<Button>(R.id.btn_fichar).setOnClickListener {
            locationEnabled()
            if (gpsStatus) {
                iniciarPararContador()
                obtenerUbicación()
            }
            else {
                AwesomeDialog.build(requireActivity())
                    .title("No se detecta GPS")
                    .body("Para poder realizar un fichaje es necesario diponer de GPS")
                    .icon(R.drawable.ic_falla)
                    .onPositive("Aceptar") {
                        Log.d("TAG", "positive ")
                    }
            }

        }


        //Timer
        tvTiempo = view.findViewById(R.id.txt_tiempo)
        ayudaContador = context?.let { AyudaContador(it) }!!

        if(ayudaContador.estaContando()) {
            iniciarContador()
        } else {
            pararContador()
            if(ayudaContador.startTime() != null && ayudaContador.pararTiempo() != null) {
                val time = Date().time - calcTiempoReinicio().time
                tvTiempo.text = tiempoDeLongAString(time)
            }
        }

        //El contador se modifica cada segundo
        timer.scheduleAtFixedRate(TimeTask(), 0, 1000)


        //Se obtiene si la jornada es de 4 o de 8 horas obteniéndolo de MainActivity (obtenerDatosUsuario())
        val preferencesTipoJornada: SharedPreferences = requireActivity().getSharedPreferences("tipoJornada", MODE_PRIVATE)
        val tipoJornada = preferencesTipoJornada.getInt("tipo", 0)

        barTimer = view.findViewById(R.id.progress_bar)

        //Dependiendo de la jornada la barra de progreso y el mensaje de la notificación será distinto
        if(tipoJornada == 1) {
            barTimer.max = 8*60*60  //8 horas
            crearNotificacion(8)

        } else if(tipoJornada == 2) {
            barTimer.max = 4*60*60  //4 horas
            crearNotificacion(4)
        }


        //Se añade la lista de fichajes sin conexión
        fichajesBD = AyudaSQLite(requireContext())
        view.findViewById<FloatingActionButton>(R.id.btn_mostrar).setOnClickListener {
            // Para recuperar la información
            obtenerFichajesEnEspera()
        }


        //Se obtiene la ubicación al entrar en la pestaña
        //Comprobamos que se disponene de los permisos necesarios para obtener la ubicación
        obtenerPermisosUbicacionInicio()

        //Comprobamos si hay conexión a internet a tiempo real y se lanzan los fichajes que se hayan realizado sin conexión
        val networkConnection= ConexionDeRed(requireContext())
        networkConnection.observe(requireActivity()) { isConnected ->
            if (isConnected) {
                lanzarFichajesEnEspera()
            } else {
            }
        }

        return view

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun lanzarFichajesEnEspera() {
        val db : SQLiteDatabase = fichajesBD.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM fichajes",
            null)

        var fecha = ""
        var hora = ""
        if (cursor.moveToFirst()) {
            do {
                fecha = cursor.getString(1)
                hora = cursor.getString(2)
                ficharSinConexion(fecha, hora)
            } while (cursor.moveToNext())
        }

        fichajesBD.borrarTabla()
    }

    private fun obtenerFichajesEnEspera() {
        val db : SQLiteDatabase = fichajesBD.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM fichajes",
            null)

        var result = ""
        if (cursor.moveToFirst()) {
            do {
                result = result + cursor.getInt(0).toString() + " - " +
                        cursor.getString(1) + " - " +
                        cursor.getString(2) + "\n"


            } while (cursor.moveToNext())
        }
        AwesomeDialog.build(requireActivity())
            .title("Fichajes en espera")
            .body(result)
            .icon(R.drawable.ic_sinconexion)
            .onPositive("Aceptar") {
                Log.d("TAG", "positive ")
            }
    }

    @RequiresPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    private fun obtenerPermisosUbicacionInicio() {
        if(context?.let { ActivityCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) } != PackageManager.PERMISSION_GRANTED &&
            context?.let { ActivityCompat.checkSelfPermission(it, Manifest.permission.ACCESS_COARSE_LOCATION) } != PackageManager.PERMISSION_GRANTED) {
            activity?.let { ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101) }
        }

        if(context?.let { ActivityCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) } == PackageManager.PERMISSION_GRANTED ||
            context?.let { ActivityCompat.checkSelfPermission(it, Manifest.permission.ACCESS_COARSE_LOCATION) } != PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    //Se obtiene la latitud y la longitud
                    latitud = location.latitude.toString()
                    longitud = location.longitude.toString()
                }
            }
        }
    }

    private fun crearNotificacion(horas: Int) {
        //Builder para la notificación
        createNotificationChannel()
        builder = NotificationCompat.Builder(requireContext(), ID_CANAL)
            .setSmallIcon(androidx.appcompat.R.drawable.abc_btn_check_to_on_mtrl_015)
            .setContentTitle("Recuerda el fichaje de salida")
            .setContentText("Han pasado " + horas + " horas desde tu fichaje de entrada, recuerda realizar el fichaje de salida")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Han pasado " + horas + " horas desde tu fichaje de entrada, recuerda realizar el fichaje de salida"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    }

    private fun locationEnabled() {
        val locationManager = activity?.getSystemService(LOCATION_SERVICE) as LocationManager
        gpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    //Permite crear una notificación
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nombre = "MiNotificación"
            val descripcion = "Mi descripción de la notificación"
            val importancia = NotificationManager.IMPORTANCE_DEFAULT
            val canal = NotificationChannel(ID_CANAL, nombre, importancia).apply {
                description = descripcion
            }
            //Registra el canal al sistema
            val notificationManager: NotificationManager = activity?.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(canal)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fichar() {
        context?.let { comprobarConexionFichar(it) }
        val fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
        val hora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))

        val fichaje = Fichaje(idUsuario, fecha, hora, longitud, latitud)
        val call = service.fichar("Bearer " + tokenUsuario, fichaje)

        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if(response.isSuccessful && response.body() != null) {
                    try {
                        val jsonUser = JSONObject(response.body()!!)
                        val token = jsonUser.optString("token")

                        //Si el fichaje es correcto nos devuelve un mensaje de confirmación y el contador comienza o se para
                        if(token.equals("Ok")) {
                            AwesomeDialog.build(requireActivity())
                                .title("Fichaje realizado")
                                .body("El fichaje ha sido realizado con éxito")
                                .icon(R.drawable.ic_funciona)
                                .onPositive("Aceptar") {
                                    Log.d("TAG", "positive ")
                                }
                        }
                    } catch (e: Exception) {
                        Log.d("fichar", e.toString())
                        AwesomeDialog.build(requireActivity())
                            .title("Fichaje fallido")
                            .body("El fichaje no ha sido realizado")
                            .icon(R.drawable.ic_falla)
                            .onPositive("Aceptar") {
                                Log.d("TAG", "positive ")
                            }
                    }
                } else {
                    AwesomeDialog.build(requireActivity())
                        .title("Espera")
                        .body(getString(R.string.esperar))
                        .onPositive("Aceptar") {
                            Log.d("TAG", "positive ")
                        }
                }
            }


            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("fichar", t.toString())
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun ficharSinConexion (fecha: String, hora: String){
        context?.let { comprobarConexionFichar(it) }

        val fichaje = Fichaje(idUsuario, fecha, hora, longitud, latitud)
        val call = service.fichar("Bearer " + tokenUsuario, fichaje)

        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if(response.isSuccessful && response.body() != null) {
                    try {
                        val jsonUser = JSONObject(response.body()!!)
                        val token = jsonUser.optString("token")

                        //Si el fichaje es correcto nos devuelve un mensaje de confirmación y el contador comienza o se para
                        if(token.equals("Ok")) {
                            activity?.let {
                                AwesomeDialog.build(it)
                                    .title("Fichaje realizado")
                                    .body("El fichaje ha sido realizado con éxito")
                                    .icon(R.drawable.ic_funciona)
                                    .onPositive("Aceptar") {
                                        Log.d("TAG", "positive ")
                                    }
                            }
                        }
                    } catch (e: Exception) {
                        Log.d("fichar", e.toString())
                        activity?.let {
                            AwesomeDialog.build(it)
                                .title("Fichaje fallido")
                                .body("El fichaje no ha sido realizado")
                                .icon(R.drawable.ic_falla)
                                .onPositive("Aceptar") {
                                    Log.d("TAG", "positive ")
                                }
                        }
                    }
                } else {
                    activity?.let {
                        AwesomeDialog.build(it)
                            .title("Espera")
                            .body(getString(R.string.esperar))
                            .onPositive("Aceptar") {
                                Log.d("TAG", "positive ")
                            }
                    }
                }
            }


            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("fichar", t.toString())
            }
        })
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun obtenerUbicación() {
        //Comprobamos que se disponene de los permisos necesarios para obtener la ubicación
        if(context?.let { ActivityCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) } != PackageManager.PERMISSION_GRANTED &&
            context?.let { ActivityCompat.checkSelfPermission(it, Manifest.permission.ACCESS_COARSE_LOCATION) } != PackageManager.PERMISSION_GRANTED) {
            activity?.let { ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101) }
        }

        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
            if(location != null) {
                //Se obtiene la latitud y la longitud
                latitud = location.latitude.toString()
                longitud = location.longitude.toString()
                fichar()
            }
        }
    }

    /**
     * Permite comprobar si el dispositivo tiene o no conexión a internet
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission", "Range")
    fun comprobarConexionFichar(context: Context) {
        val gestorConexion = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val capacidadesDeRed = gestorConexion.activeNetwork
        val infromacionDeRed = gestorConexion.getNetworkCapabilities(capacidadesDeRed)
        if (infromacionDeRed?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true || infromacionDeRed?.hasTransport(
                NetworkCapabilities.TRANSPORT_CELLULAR) == true) {
        } else {
            //Cuando no hay conexión, el fichaje se almacena en la base de datos
            AwesomeDialog.build(requireActivity())
                .title("Fichaje sin conexión")
                .body("No hay conexión a internet, los fichajes se almacenan para realizarse cuando disponga de ella")
                .icon(R.drawable.ic_sinconexion)
                .onPositive("Aceptar") {
                    Log.d("TAG", "positive ")
                }

            val fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
            val hora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
            // calling method to add
            // name to our database
            fichajesBD.añadirFichajeSinConexion(fecha, hora)


        }
    }


    private inner class TimeTask: TimerTask() {
        override fun run() {
            if(ayudaContador.estaContando()) {
                activity?.runOnUiThread({
                    val time = Date().time - ayudaContador.startTime()!!.time
                    tvTiempo.text = tiempoDeLongAString(time)

                    //Se añade el progreso al progressbar cada segundo
                    barTimer.setProgress(time.toInt()/1000);
                })
            }
        }
    }

    private fun reiniciarContador() {
        ayudaContador.setTiempoParar(null)
        ayudaContador.setTiempoIniciar(null)
        pararContador()
        tvTiempo.text = tiempoDeLongAString(0)

        barTimer.setProgress(0);
    }

    private fun pararContador() {
        ayudaContador.setEstaContando(false)
    }

    private fun iniciarContador() {
        ayudaContador.setEstaContando(true)
    }

    private fun iniciarPararContador() {
        if(ayudaContador.estaContando()) {
            ayudaContador.setTiempoParar(Date())
            pararContador()
            reiniciarContador()
        } else {
            if(ayudaContador.pararTiempo() != null) {
                ayudaContador.setTiempoIniciar(calcTiempoReinicio())
                ayudaContador.setTiempoParar(null)
            } else {
                ayudaContador.setTiempoIniciar(Date()) }
            iniciarContador()
        }
    }

    private fun calcTiempoReinicio(): Date {
        val diff = ayudaContador.startTime()!!.time - ayudaContador.pararTiempo()!!.time
        return Date(System.currentTimeMillis() + diff)
    }

    private fun tiempoDeLongAString(ms: Long): String {
        val seconds = (ms / 1000) % 60
        val minutes = (ms / (1000 * 60) % 60)
        val hours = (ms / (1000 * 60 * 60) % 24)

        val timer = String.format("%02d:%02d:%02d", hours, minutes, seconds)

        //Cuando termina la jornada laboral se manda una notificación
        if(timer.equals("00:00:10")) {
            with(NotificationManagerCompat.from(requireContext())) {
                notify(123, builder.build())
            }
        }

        return timer
    }


}

