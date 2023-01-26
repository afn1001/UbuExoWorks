package com.example.ubuexoworks

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.awesomedialog.*
import com.example.ubuexoworks.ClasesDeDatos.Fichaje
import com.google.android.gms.location.FusedLocationProviderClient
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
 * Clase que permite recuperar la contraseña
 * @author Alejandro Fraga Neila
 */
class RecordarClave : AppCompatActivity() {

    private lateinit var retrofit: Retrofit
    private lateinit var service: ApiService
    private var email: String = ""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recordar_clave_layout)

        //Creamos el servicio
        service = createApiService()


        val emailInput = findViewById<EditText>(R.id.et_correoRecuperacion)


        val btnEnviarCorreo = findViewById<TextView>(R.id.btn_enviarCorreo)
        btnEnviarCorreo.setOnClickListener {

            email = emailInput.text.toString().trim()
            if(email.isNotEmpty()) {
                recuperarContraseña(email)
            } else {
                AwesomeDialog.build(this)
                    .title("Correo vacío")
                    .body("No se ha introducido el correo electrónico")
                    .icon(R.drawable.ic_falla)
                    .onPositive("Aceptar") {
                        Log.d("Correo", "positive ")
                    }
            }
        }
    }

    /**
     * Permite acceder de vuelta a la pantalla principal de login
     */
    fun irALogin(view: View) {
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
        finish()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun recuperarContraseña(email: String) {
        comprobarConexion(applicationContext)
        val call = service.recuperaContraseña(email)

        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if(response.isSuccessful && response.body() != null) {
                    try {
                        AwesomeDialog.build(this@RecordarClave)
                            .title("Cambio de contraseña")
                            .body("Se ha enviado un correo electrónico con la nueva contraseña")
                            .icon(R.drawable.ic_funciona)
                            .onPositive("Aceptar") {
                                Log.d("Cambiar contraseña", "positive ")
                            }
                    } catch (e: Exception) {
                        Log.d("fichar", e.toString())
                    }
                } else {
                    AwesomeDialog.build(this@RecordarClave)
                        .title("Cambio de contraseña fallido")
                        .body("No se ha podido enviar el correo electrónico con la nueva contraseña")
                        .icon(R.drawable.ic_falla)
                        .onPositive("Aceptar") {
                            Log.d("Cambiar contraseña fallo", "positive ")
                        }
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

    /**
     * Comprueba si el dispositivo dispone de conexión a internet
     */
    @SuppressLint("MissingPermission")
    fun comprobarConexion(context: Context) {
        val gestorConexion = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capacidadesDeRed = gestorConexion.activeNetwork
        val infromacionDeRed = gestorConexion.getNetworkCapabilities(capacidadesDeRed)
        if (infromacionDeRed?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true || infromacionDeRed?.hasTransport(
                NetworkCapabilities.TRANSPORT_CELLULAR) == true) {
        } else {
            AwesomeDialog.build(this)
                .title("Sin conexión")
                .body("No hay conexión a internet")
                .icon(R.drawable.ic_sinconexion)
                .onPositive("Aceptar") {
                    Log.d("TAG", "positive ")
                }
        }
    }
}