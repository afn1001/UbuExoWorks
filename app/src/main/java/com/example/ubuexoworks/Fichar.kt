package com.example.ubuexoworks

import android.Manifest
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Use the [Fichar.newInstance] factory method to
 * create an instance of this fragment.
 */
class Fichar : Fragment() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var retrofit: Retrofit
    private lateinit var service: ApiService
    private var longitud: String = ""
    private var latitud: String = ""
    private var idUsuario: Int = 0
    private var tokenUsuario: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_fichar, container, false)
        val idView : TextView = view.findViewById(R.id.txt_idUsuario)
        val tokenView : TextView = view.findViewById(R.id.txt_tokenUsuario)

        //Se obtienen los datos del id de usuario
        val preferences: SharedPreferences = requireActivity().getSharedPreferences("Login", MODE_PRIVATE)
        idUsuario = preferences.getInt("Id", 0)
        idView.setText(idUsuario.toString())

        tokenUsuario = preferences.getString("Token","")
        tokenView.setText(tokenUsuario)

        //Creamos el servicio
        service = createApiService()

        //Botón para fichar

        view.findViewById<Button>(R.id.btn_fichar).setOnClickListener {
            obtenerUbicación()
        }

        return view

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fichar() {
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


                        if(token.equals("OK")) {
                            Toast.makeText(context, "Funciona", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.d("fichar", e.toString())
                        Toast.makeText(context, response.body(), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Falla", Toast.LENGTH_SHORT).show()
                }
            }


            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("fichar", t.toString())
            }
        })
    }

    private fun createApiService() : ApiService {
        retrofit = Retrofit.Builder()
            .baseUrl("https://miubuapp.herokuapp.com/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(ApiService::class.java)
    }

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

                val txtFecha = requireActivity().findViewById<TextView>(R.id.txt_fecha)
                txtFecha.setText(latitud)

                val txtHora = requireActivity().findViewById<TextView>(R.id.txt_hora)
                txtHora.setText(longitud)

                fichar()
            }

        }


    }
}