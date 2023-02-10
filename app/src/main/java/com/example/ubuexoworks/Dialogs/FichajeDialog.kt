package com.example.ubuexoworks

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.location.Geocoder
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*

/**
 * Clase para mostrar el mapa con la ubicación del fichaje que se quiera
 * @author Alejandro Fraga Neila
 */
class FichajeDialog: DialogFragment(), OnMapReadyCallback {

    private var hora: String = "00:00:00"
    private var longitud: Float = 0F
    private var latitud: Float = 0F
    private lateinit var mMap: GoogleMap

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        getDialog()!!.getWindow()?.setBackgroundDrawableResource(R.drawable.round_corner);

        val view: View = inflater.inflate(R.layout.dialog_mapa, container, false)

        //Se obtiene la longitud y la latitud del fichaje
        val preferences: SharedPreferences = requireActivity().getSharedPreferences("Ubicacion", Context.MODE_PRIVATE)
        hora = preferences.getString("Hora","")!!
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

        //Para crear un texto de diálogo del snippet personalizado y entren todos los datos que queremos
        mMap.setInfoWindowAdapter(object : InfoWindowAdapter {
            override fun getInfoWindow(p0: Marker?): View? {
                return null
            }
            override fun getInfoContents(marker: Marker): View {
                val info = LinearLayout(context)
                info.orientation = LinearLayout.VERTICAL
                val title = TextView(context)
                title.gravity = Gravity.CENTER
                title.setTypeface(null, Typeface.BOLD)
                title.text = marker.title
                val snippet = TextView(context)
                snippet.text = marker.snippet
                info.addView(title)
                info.addView(snippet)
                return info
            }
        })

        //Obtiene el nombre de la ciudad desde la que se ha fichado
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val direccion = geocoder.getFromLocation(latitud.toDouble(), longitud.toDouble(), 3);
        val nombreCiudad = direccion.get(0).adminArea;

        //Añade el marcador al mapa con sus características
        val markerOptions = MarkerOptions()
        markerOptions.position(latitudLongitud)
        markerOptions.title(nombreCiudad)
        markerOptions.snippet("Hora: " + hora +"\nLatitud: " + latitud.toString() + "\nLongitud: " + longitud.toString())
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
        mMap.addMarker(markerOptions);
    }

}