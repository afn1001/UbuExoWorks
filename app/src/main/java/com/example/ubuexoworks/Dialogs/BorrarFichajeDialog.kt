package com.example.ubuexoworks.Dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.example.ubuexoworks.ApiService
import com.example.ubuexoworks.ClasesDeDatos.Fichaje
import com.example.ubuexoworks.ClasesDeDatos.FichajeEliminar
import com.example.ubuexoworks.R
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class BorrarFichajeDialog : DialogFragment() {
    private lateinit var retrofit: Retrofit
    private lateinit var service: ApiService

    private var idUsuario: Int = 0
    private var idFichaje: Int = 0
    private var tokenUsuario: String? = ""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        //Creamos el servicio
        service = createApiService()

        //Obtenemos el token de usuario, el idUsuario y el idFichaje
        val preferences: SharedPreferences = requireActivity().getSharedPreferences("Login", Context.MODE_PRIVATE)
        tokenUsuario = preferences.getString("Token","")
        idUsuario = preferences.getInt("Id", 0)

        val preferencesFichaje: SharedPreferences = requireActivity().getSharedPreferences("FichajeAEliminar", Context.MODE_PRIVATE)
        idFichaje = preferencesFichaje.getInt("IDFichaje", 0)

        //Realizamos las acciones si se pulsa en sí o en no
        return activity?.let {
            //Se usa la clase Builder para construir convenientemente el dialog
            val builder = AlertDialog.Builder(it)
            builder.setMessage(R.string.pregunta)
                .setPositiveButton(R.string.aceptar,
                    DialogInterface.OnClickListener { dialog, id ->
                        solicitudBorrado(idUsuario, idFichaje)

                    })
                .setNegativeButton(R.string.rechazar,
                    DialogInterface.OnClickListener { dialog, id ->
                        // Se cancela el dialog
                    })
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun solicitudBorrado(idUsuario: Int, idFichaje: Int) {
        val fichajeEliminar = FichajeEliminar(idUsuario, idFichaje)
        val call = service.solicitudBorrado("Bearer " + tokenUsuario, fichajeEliminar)

        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                Log.d("respuesta", response.toString());
                if(response.isSuccessful && response.body() != null) {
                    try {
                        if(response.body().equals("Ok")) {
                            Toast.makeText(context, "Se ha solicitado eliminar el fichaje con éxito", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.d("solicitudBorrado", e.toString())
                        Toast.makeText(context, response.body(), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    //Toast.makeText(context, "Falla", Toast.LENGTH_SHORT).show()
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