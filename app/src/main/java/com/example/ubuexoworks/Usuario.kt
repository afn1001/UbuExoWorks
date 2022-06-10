package com.example.ubuexoworks

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.w3c.dom.Text
import java.util.*


class Usuario : AppCompatActivity() {

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.usuario_layout)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        findViewById<Button>(R.id.btn_fichar).setOnClickListener() {
            obtenerUbicación()
        }
    }

    private fun obtenerUbicación() {
        val task = fusedLocationProviderClient.lastLocation
        //Comprobamos que se disponene de los permisos necesarios para obtener la ubicación
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 101)
        }
        task.addOnSuccessListener {
            if(it != null) {
                //Se obtiene la ciudad exacta en la que se ficha a partir de la latitud y la longitud
                val geocoder = Geocoder(this, Locale.getDefault())
                val direcciones: List<Address> = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                val nombreCiudad: String = direcciones.get(0).getAddressLine(0)
                val ubicacion : TextView = findViewById(R.id.txt_lugarFichaje)
                ubicacion.setText(nombreCiudad)
            }

        }
    }
}