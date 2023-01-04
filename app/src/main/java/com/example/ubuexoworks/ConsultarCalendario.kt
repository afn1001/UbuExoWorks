package com.example.ubuexoworks

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.ubuexoworks.ClasesDeDatos.FichajeObtenido
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Clase que permite trabajar con todos los fichajes realizados. Se pueden obtener los fichajes
 * de cada día, mirar la información en el mapa y solicitar eliminarlos
 * @author Alejandro Fraga Neila
 */
class ConsultarCalendario : Fragment() {

    private lateinit var service: ApiService
    private var idUsuario: Int = 0
    lateinit var listFichajes: ArrayList<FichajeObtenido>
    private var tokenUsuario: String? = ""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view : View =  inflater.inflate(R.layout.fragment_consultar_calendario, container, false)

        //Se obtienen los datos del id de usuario
        val preferences: SharedPreferences = requireActivity().getSharedPreferences("Login", Context.MODE_PRIVATE)
        idUsuario = preferences.getInt("Id", 0)
        tokenUsuario = preferences.getString("Token","")


        //Creamos el servicio
        service = (activity as MainActivity).createApiService()

        //Lista con todos los fichajes
        listFichajes = ArrayList()

        val fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
        obtenerFichajes(fecha)


        val calendario = view.findViewById<CalendarView>(R.id.cdvCalendario)
        calendario.setOnDateChangeListener { view, year, month, dayOfMonth ->
            //Obtenemos los fichajes del día que deseamos
            val estaFecha = LocalDate.of(year, month+1, dayOfMonth).format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
            obtenerFichajes(estaFecha)
        }


        return view
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun obtenerFichajes(fecha: String) {
        context?.let { (activity as MainActivity)?.comprobarConexion(it) }
        val call: Call<ArrayList<FichajeObtenido>> = service.getFichaje("Bearer " + tokenUsuario, idUsuario, fecha)

        call.enqueue(object : Callback<ArrayList<FichajeObtenido>> {
            override fun onResponse(call: Call<ArrayList<FichajeObtenido>>, response: Response<ArrayList<FichajeObtenido>>) {

                if(response.isSuccessful && response.body() != null) {
                    listFichajes = response.body()!!

                    Log.d("TAG", response.body().toString())

                    //Inicializamos el adaptador para la lista de fichajes
                    val adapter = context?.let { FichajeAdapter(it, listFichajes) }

                    val lvFichajes = activity?.findViewById<ListView>(R.id.lv_fichajes)
                    lvFichajes?.adapter = adapter
                    val tvVacio = activity?.findViewById<TextView>(R.id.vacio)
                    lvFichajes?.emptyView = tvVacio

                    val progressBar = activity?.findViewById<ProgressBar>(R.id.progress_bar)
                    //Desaparece la progressbar para dar lugar al resultado
                    progressBar?.visibility=View.INVISIBLE
                    lvFichajes?.visibility=View.VISIBLE


                } else {
                    Toast.makeText(requireContext(), "Falla", Toast.LENGTH_SHORT).show()
                }
            }


            override fun onFailure(call: Call<ArrayList<FichajeObtenido>>, t: Throwable) {
                Log.d("fichar", t.toString())
            }
        })
    }
}