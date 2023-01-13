package com.example.ubuexoworks.Dialogs

import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.example.ubuexoworks.R

/**
 * Clase para mostrar la imagen obtenida con un tamaño mayor y permitiendo ampliar
 * @author Alejandro Fraga Neila
 */
class ImagenDialog: DialogFragment() {

    private var imagen: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        getDialog()!!.getWindow()?.setBackgroundDrawableResource(R.drawable.round_corner);

        val view: View = inflater.inflate(R.layout.dialog_imagen, container, false)

        //Se obtiene la longitud y la latitud del fichaje
        val preferences: SharedPreferences = requireActivity().getSharedPreferences("Ubicacion", Context.MODE_PRIVATE)
        imagen = preferences.getString("Imagen","")!!

        //Se decodifica la imagen para poder mostrarla por pantalla
        val imageBytes = Base64.decode(imagen, Base64.DEFAULT)
        val image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

        val iwImagen: ImageView = view.findViewById(R.id.iw_imagenAmpliada)
        iwImagen.setImageBitmap(image)

        return view
    }

}