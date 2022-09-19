package com.example.ubuexoworks

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.ubuexoworks.ClasesDeDatos.FichajeObtenido
import com.example.ubuexoworks.ClasesDeDatos.Usuario
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory


/**
 * A simple [Fragment] subclass.
 * Use the [Fichar.newInstance] factory method to
 * create an instance of this fragment.
 */
class DatosUsuario : Fragment() {
    private lateinit var retrofit: Retrofit
    private lateinit var service: ApiService
    private var idUsuario: Int = 0
    lateinit var usuario: Usuario
    private var tokenUsuario: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_datos_usuario, container, false)

        //Se obtienen los datos del id de usuario
        val preferences: SharedPreferences =
            requireActivity().getSharedPreferences("Login", Context.MODE_PRIVATE)
        idUsuario = preferences.getInt("Id", 0)
        tokenUsuario = preferences.getString("Token", "")

        //Creamos el servicio
        service = createApiService()

        //Obtenermos los datos del usuario
        obtenerDatosUsuario()

        return view
    }

    private fun obtenerDatosUsuario() {
        val call: Call<String> = service.getDatosUsuario("Bearer " + tokenUsuario, idUsuario)

        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if(response.isSuccessful && response.body() != null) {
                    try {
                        val jsonUser = JSONObject(response.body()!!)
                        val nombre = jsonUser.optString("nombre")
                        val apellidos = jsonUser.optString("apellidos")


                        val nombreView : TextView = view!!.findViewById(R.id.txt_nombre)
                        nombreView.setText(nombre)

                        val apellidosView : TextView = view!!.findViewById(R.id.txt_apellidos)
                        apellidosView.setText(apellidos)



                    } catch (e: Exception) {
                        Log.d("obtenerDatos", e.toString())
                        Toast.makeText(context, response.body(), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Falla", Toast.LENGTH_SHORT).show()
                }
            }


            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("obtenerDatos", t.toString())
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