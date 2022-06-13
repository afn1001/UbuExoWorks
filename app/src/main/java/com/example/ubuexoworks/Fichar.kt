package com.example.ubuexoworks

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.*



/**
 * A simple [Fragment] subclass.
 * Use the [Fichar.newInstance] factory method to
 * create an instance of this fragment.
 */
class Fichar : Fragment() {

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_fichar, container, false)
        view.findViewById<Button>(R.id.btn_fichar).setOnClickListener() {
            obtenerUbicación(view)
        }

        return view

    }

    private fun obtenerUbicación(view : View) {
        val task = fusedLocationProviderClient.lastLocation
        //Comprobamos que se disponene de los permisos necesarios para obtener la ubicación
        if(ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
        }
        task.addOnSuccessListener {
            if(it != null) {
                //Se obtiene la ciudad exacta en la que se ficha a partir de la latitud y la longitud
                val geocoder = Geocoder(this.context, Locale.getDefault())
                val direcciones: List<Address> = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                val nombreCiudad: String = direcciones.get(0).getAddressLine(0)
                val ubicacion : TextView = view.findViewById(com.example.ubuexoworks.R.id.txt_lugarFichaje)
                ubicacion.setText(nombreCiudad)
            }

        }
    }
}