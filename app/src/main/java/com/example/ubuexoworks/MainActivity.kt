package com.example.ubuexoworks

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.FragmentTransaction
import androidx.slidingpanelayout.widget.SlidingPaneLayout
import com.example.ubuexoworks.ClasesDeDatos.Usuario
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

/**
 * Clase donde se puede realizar todas las funciones de usuario
 * Permite moverse entre las pestañas "Fichar", "Calendario" y "Dietas" y ver los datos de usuario
 * @author Alejandro Fraga Neila
 */
class MainActivity : AppCompatActivity() {
    //Create our four fragments object
    lateinit var consultarCalendario: ConsultarCalendario
    lateinit var fichar: Fichar
    lateinit var dietas: Dietas
    var id: Int = 0

    private lateinit var retrofit: Retrofit
    private lateinit var service: ApiService
    private var idUsuario: Int = 0
    private var tokenUsuario: String? = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Sliding panel
        val layout: SlidingUpPanelLayout = findViewById(R.id.slidingDown)
        layout.addPanelSlideListener(object: SlidingPaneLayout.PanelSlideListener,
            SlidingUpPanelLayout.PanelSlideListener {
            override fun onPanelSlide(panel: View, slideOffset: Float) {
            }

            override fun onPanelStateChanged(
                panel: View?,
                previousState: SlidingUpPanelLayout.PanelState?,
                newState: SlidingUpPanelLayout.PanelState?
            ) {
                TODO("Not yet implemented")
            }

            override fun onPanelOpened(panel: View) {
                TODO("Not yet implemented")

            }

            override fun onPanelClosed(panel: View) {
                TODO("Not yet implemented")
            }

        })

        //Se obtienen los datos del id de usuario
        val preferences: SharedPreferences =
            this.getSharedPreferences("Login", Context.MODE_PRIVATE)
        idUsuario = preferences.getInt("Id", 0)
        tokenUsuario = preferences.getString("Token", "")

        //Creamos el servicio
        service = createApiService()

        //Obtenermos los datos del usuario
        obtenerDatosUsuario()

        //now let's create our framelayout and bottomnav variables
        var bottomnav = findViewById<BottomNavigationView>(R.id.BottomNavMenu)
        var frame = findViewById<FrameLayout>(R.id.frameLayout)
        //Now let's the deffault Fragment
        fichar = Fichar()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frameLayout,fichar)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .commit()
        //now we will need to create our different fragemnts
        //Now let's add the menu evenet listener
        bottomnav.setOnItemSelectedListener { item ->
            //we will select each menu item and add an event when it's selected
            when(item.itemId){
                R.id.fichar -> {
                    fichar = Fichar()
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frameLayout,fichar)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit()
                }
                R.id.calendario -> {
                    consultarCalendario = ConsultarCalendario()
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frameLayout,consultarCalendario)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit()
                }

                R.id.dietas -> {
                    dietas = Dietas()
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frameLayout,dietas)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit()
                }
            }

            true
        }
    }


    private fun obtenerDatosUsuario() {
        comprobarConexion(applicationContext)
        val call: Call<String> = service.getDatosUsuario("Bearer " + tokenUsuario, idUsuario)

        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if(response.isSuccessful && response.body() != null) {
                    try {
                        val jsonUser = JSONObject(response.body()!!)
                        val nombre = jsonUser.optString("nombre")
                        val apellidos = jsonUser.optString("apellidos")


                        val nombreView : TextView = findViewById(R.id.txt_nombreUsuario)
                        nombreView.setText(nombre)

                        val apellidosView : TextView = findViewById(R.id.txt_apellidosUsuario)
                        apellidosView.setText(apellidos)



                    } catch (e: Exception) {
                        Log.d("obtenerDatos", e.toString())
                        Toast.makeText(applicationContext, response.body(), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(applicationContext, "Falla", Toast.LENGTH_SHORT).show()
                }
            }


            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("obtenerDatos", t.toString())
            }
        })
    }


    fun createApiService() : ApiService {
        retrofit = Retrofit.Builder()
            .baseUrl("https://miubuapp.herokuapp.com/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(ApiService::class.java)
    }

    @SuppressLint("MissingPermission")
    fun comprobarConexion(context: Context) {
        val gestorConexion = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capacidadesDeRed = gestorConexion.activeNetwork
        val infromacionDeRed = gestorConexion.getNetworkCapabilities(capacidadesDeRed)
        if (infromacionDeRed?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true || infromacionDeRed?.hasTransport(
                NetworkCapabilities.TRANSPORT_CELLULAR) == true) {
        } else {
            Toast.makeText(context, "No hay conexión a internet", Toast.LENGTH_LONG).show()
        }
    }
}





