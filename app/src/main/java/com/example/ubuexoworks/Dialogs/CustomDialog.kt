package com.example.ubuexoworks

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MyCustomDialog: DialogFragment(), OnMapReadyCallback {

    private var longitud: Float = 0F
    private var latitud: Float = 0F
    private lateinit var mMap: GoogleMap

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        getDialog()!!.getWindow()?.setBackgroundDrawableResource(R.drawable.round_corner);

        val view: View = inflater.inflate(R.layout.dialog_mapa, container, false)

        //Se obtiene la longitud y la latitud del fichaje
        val preferences: SharedPreferences = requireActivity().getSharedPreferences("Ubicacion", Context.MODE_PRIVATE)
        latitud = preferences.getFloat("Latitud", 0F)
        longitud = preferences.getFloat("Longitud", 0F)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)


        return view
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.80).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.80).toInt()
        dialog!!.window?.setLayout(width, height)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val latitudLongitud = LatLng(latitud.toDouble(),longitud.toDouble())

        //Acerca la cámara a la ubicación
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latitudLongitud, 11F));

        //Añade el marcador al mapa con sus características
        val markerOptions = MarkerOptions()
        markerOptions.position(latitudLongitud)
        markerOptions.title("Posición del fichaje")
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
        mMap.addMarker(markerOptions);
    }

}