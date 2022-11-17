package com.example.ubuexoworks

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.example.ubuexoworks.ClasesDeDatos.FichajeObtenido
import com.example.ubuexoworks.Dialogs.BorrarFichajeDialog


class FichajeAdapter(private val mContext : Context, private val listaFichajes : ArrayList<FichajeObtenido>) : BaseAdapter() {
    override fun getCount(): Int {
        return listaFichajes.size
    }

    override fun getItem(p0: Int): Any {
        return p0
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(parent.context).inflate(R.layout.item_fichaje, parent, false)

        val fichaje = listaFichajes[position]

        val tvFecha: TextView = view.findViewById(R.id.tv_fecha)
        tvFecha.setText(fichaje.fecha)

        val tvHora: TextView = view.findViewById(R.id.tv_hora)
        tvHora.setText(fichaje.hora)

        val tvLongitud: TextView = view.findViewById(R.id.tv_longitud)
        tvLongitud.setText(fichaje.longitud.toString())

        val tvLatitud: TextView = view.findViewById(R.id.tv_latitud)
        tvLatitud.setText(fichaje.latitud.toString())

        //Le pasamos la latitud y la longitud al dialog con el map
        val btnUbicacion: Button = view.findViewById(R.id.btn_ubicacion)
        btnUbicacion.setOnClickListener {
            val sp = mContext.getSharedPreferences("Ubicacion", Context.MODE_PRIVATE)
            val ed = sp.edit()
            ed.putFloat("Longitud", fichaje.longitud)
            ed.putFloat("Latitud", fichaje.latitud)
            ed.commit()

            val fragmentActivity = mContext as FragmentActivity
            val fragmentManager: FragmentManager = fragmentActivity.supportFragmentManager
            MyCustomDialog().show(fragmentManager, "UbicacionFragment")
        }

        val btnEliminarFichaje: Button = view.findViewById(R.id.btn_eliminarFichaje)
        btnEliminarFichaje.setOnClickListener {
            val sp = mContext.getSharedPreferences("FichajeAEliminar", Context.MODE_PRIVATE)
            val ed = sp.edit()
            ed.putInt("IDFichaje", fichaje.ID)
            ed.commit()

            val fragmentActivity = mContext as FragmentActivity
            val fragmentManager: FragmentManager = fragmentActivity.supportFragmentManager
            BorrarFichajeDialog().show(fragmentManager, "EliminarFichajeFragment")
        }

        return view
    }
}