package com.example.ubuexoworks

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.ubuexoworks.ClasesDeDatos.FichajeObtenido
import com.google.android.gms.location.FusedLocationProviderClient
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
 * A simple [Fragment] subclass.
 * Use the [ConsultarCalendario.newInstance] factory method to
 * create an instance of this fragment.
 */
class ConsultarCalendario : Fragment() {

    private lateinit var retrofit: Retrofit
    private lateinit var service: ApiService
    private var idUsuario: Int = 0
    lateinit var listFichajes: ArrayList<FichajeObtenido>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view : View =  inflater.inflate(R.layout.fragment_consultar_calendario, container, false)

        //Se obtienen los datos del id de usuario
        val preferences: SharedPreferences = requireActivity().getSharedPreferences("Login", Context.MODE_PRIVATE)
        idUsuario = preferences.getInt("Id", 0)

        val txtEntrada = view.findViewById<TextView>(R.id.txtHoraEntrada)
        val txtSalida = view.findViewById<TextView>(R.id.txtHoraSalida)

        //Creamos el servicio
        service = createApiService()



        //Lista con todos los fichajes
        listFichajes = ArrayList()

        val fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
        obtenerFichajes(fecha)


        val calendario = view.findViewById<CalendarView>(R.id.cdvCalendario)
        calendario.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val mes = "" + month
            val diaDelMes = "" + dayOfMonth
            txtEntrada.setText(mes)
            txtSalida.setText(diaDelMes)

            //Obtenemos los fichajes del día que deseamos
            val estaFecha = LocalDate.of(year, month+1, dayOfMonth).format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
            obtenerFichajes(estaFecha)
        }


        return view
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun obtenerFichajes(fecha: String) {
        val call: Call<ArrayList<FichajeObtenido>> = service.getFichaje(idUsuario.toString(), fecha)

        call.enqueue(object : Callback<ArrayList<FichajeObtenido>> {
            override fun onResponse(call: Call<ArrayList<FichajeObtenido>>, response: Response<ArrayList<FichajeObtenido>>) {
                if(response.isSuccessful && response.body() != null) {
                    listFichajes = response.body()!!

                    Log.d("TAG", response.body().toString())
                    try {
                        val txtHora = requireActivity().findViewById<TextView>(R.id.txtHoraSalida)
                        txtHora.setText(listFichajes[1].hora_entrada)
                    } catch (e: Exception) {

                    }

                    //Inicializamos el adaptador para la lista de fichajes
                    val adapter = FichajeAdapter(requireContext(), listFichajes)

                    val lvFichajes = requireActivity().findViewById<ListView>(R.id.lv_fichajes)
                    lvFichajes.adapter = adapter
                } else {
                    Toast.makeText(context, "Falla", Toast.LENGTH_SHORT).show()
                }
            }


            override fun onFailure(call: Call<ArrayList<FichajeObtenido>>, t: Throwable) {
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