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
import com.example.awesomedialog.*
import com.example.ubuexoworks.ApiService
import com.example.ubuexoworks.ClasesDeDatos.FichajeEliminar
import com.example.ubuexoworks.ConsultarCalendario
import com.example.ubuexoworks.MainActivity
import com.example.ubuexoworks.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

/**
 * Clase que sirve para confirmar si queremos realizar la petición de borrado de un fichaje
 * y si la respuesta es "Sí" enviar un correo electrónico al administrador
 * @author Alejandro Fraga Neila
 */
class BorrarFichajeDialog : DialogFragment() {
    private lateinit var service: ApiService

    private var idUsuario: Int = 0
    private var idFichaje: Int = 0
    private var tokenUsuario: String? = ""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        //Creamos el servicio
        service = (activity as MainActivity).createApiService()

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
            builder.create()
        } ?: throw IllegalStateException("La actividad no puede ser nula")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun solicitudBorrado(idUsuario: Int, idFichaje: Int) {
        //Se comprueba la conexión a internet
        context?.let { (activity as MainActivity)?.comprobarConexion(it) }
        val fichajeEliminar = FichajeEliminar(idUsuario, idFichaje)
        val call = service.solicitudBorrado("Bearer " + tokenUsuario, fichajeEliminar)

        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                Log.d("respuesta", response.toString());
                if(response.isSuccessful && response.body() != null) {
                    try {
                        AwesomeDialog.build(requireActivity())
                            .title("Solicitud de borrado")
                            .body("La solicitud de borrado ha sido enviada con éxito")
                            .icon(R.drawable.ic_funciona)
                            .onPositive("Aceptar") {
                                Log.d("TAG", "positive ")
                            }

                    } catch (e: Exception) {
                        Log.d("solicitudBorrado", e.toString())
                    }
                } else {
                    AwesomeDialog.build(requireActivity())
                        .title("Solicitud de borrado fallida")
                        .body("La solicitud de borrado no ha podido ser enviada")
                        .icon(R.drawable.ic_falla)
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
}