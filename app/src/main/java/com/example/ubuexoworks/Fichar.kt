package com.example.ubuexoworks

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.content.Context.MODE_PRIVATE
import android.content.Context.NOTIFICATION_SERVICE
import android.content.pm.PackageManager
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.example.ubuexoworks.ClasesDeDatos.Fichaje
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.time.LocalDate
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

    //Timer
    lateinit var ayudaContador: AyudaContador

    private val timer = Timer()
    private lateinit var tvTiempo: TextView

    //Id de la notificación
    private val ID_CANAL = "canal01"

    @RequiresApi(Build.VERSION_CODES.O)
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
            obtenerUbicación()
        }

        //Timer
        tvTiempo = view.findViewById(R.id.txt_tiempo)
        ayudaContador = AyudaContador(requireContext())

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


        //Builder para la notificación
        createNotificationChannel()
        builder = NotificationCompat.Builder(requireContext(), ID_CANAL)
            .setSmallIcon(androidx.appcompat.R.drawable.abc_btn_check_to_on_mtrl_015)
            .setContentTitle("Recuerda el fichaje de salida")
            .setContentText("Han pasado 8 horas desde tu fichaje de entrada, recuerda realizar el fichaje de salida")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Han pasado 8 horas desde tu fichaje de entrada, recuerda realizar el fichaje de salida"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)


        barTimer = view.findViewById(R.id.progress_bar)


        return view

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
        context?.let { (activity as MainActivity)?.comprobarConexion(it) }
        val fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
        val hora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))

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
                            iniciarPararContador()
                            Toast.makeText(context, "Fichaje realizado", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.d("fichar", e.toString())
                        Toast.makeText(context, response.body(), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, R.string.esperar, Toast.LENGTH_LONG).show()
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
        if(ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
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

    private fun resetAction() {
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
            resetAction()
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


        if(timer.equals("08:00:00")) {
            with(NotificationManagerCompat.from(requireContext())) {
                // notificationId is a unique int for each notification that you must define
                notify(123, builder.build())
            }
        }

        return timer
    }


}

