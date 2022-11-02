package com.example.ubuexoworks

import android.content.Intent
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
                Toast.makeText(this, "Email vacío", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun irALogin() {
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun recuperarContraseña(email: String) {
        val call = service.recuperaContraseña(email)

        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if(response.isSuccessful && response.body() != null) {
                    try {
                        val jsonUser = JSONObject(response.body()!!)
                        val token = jsonUser.optString("token")


                        if(token.equals("OK")) {
                            Toast.makeText(applicationContext, "Funciona", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.d("fichar", e.toString())
                        Toast.makeText(applicationContext, response.body(), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(applicationContext, "Falla", Toast.LENGTH_SHORT).show()
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
}