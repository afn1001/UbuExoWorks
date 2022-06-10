package com.example.ubuexoworks

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    //Create our four fragments object
    lateinit var consultarCalendario: ConsultarCalendario
    lateinit var datosUsuario: DatosUsuario
    lateinit var fichar: Fichar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //now let's create our framelayout and bottomnav variables
        var bottomnav = findViewById<BottomNavigationView>(R.id.BottomNavMenu)
        var frame = findViewById<FrameLayout>(R.id.frameLayout)
        //Now let's the deffault Fragment
        datosUsuario = DatosUsuario()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frameLayout,datosUsuario)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .commit()
        //now we will need to create our different fragemnts
        //Now let's add the menu evenet listener
        bottomnav.setOnItemSelectedListener { item ->
            //we will select each menu item and add an event when it's selected
            when(item.itemId){
                R.id.datos -> {
                    datosUsuario = DatosUsuario()
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frameLayout,datosUsuario)
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
                R.id.fichar -> {
                    fichar = Fichar()
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frameLayout,fichar)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit()
                }
            }

            true
        }
    }
}