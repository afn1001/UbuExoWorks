package com.example.ubuexoworks

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.example.ubuexoworks.ClasesDeDatos.FichajeObtenido
import com.example.ubuexoworks.Dialogs.BorrarFichajeDialog

/**
 * Clase que sirve como adaptador para poder devolver la lista con todos los fichajes
 * @author Alejandro Fraga Neila
 */
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

        val tvTipoFichaje: TextView = view.findViewById(R.id.tv_tipofichaje)
        if(esPar(position)) {
            tvTipoFichaje.setText("Fichaje de entrada")
            view.setBackgroundResource(R.drawable.borde_fichaje_entrada)
        } else {
            tvTipoFichaje.setText("Fichaje de salida")
            view.setBackgroundResource(R.drawable.borde_fichaje_salida)
        }

        val tvLongitud: TextView = view.findViewById(R.id.tv_longitud)
        tvLongitud.setText(fichaje.longitud.toString())

        val tvLatitud: TextView = view.findViewById(R.id.tv_latitud)
        tvLatitud.setText(fichaje.latitud.toString())

        //Se pasa la latitud y la longitud al dialog con el map
        val iwUbicacion: ImageView = view.findViewById(R.id.iw_ubicacion)
        iwUbicacion.setOnClickListener {
            //Se pasa la ubicación al mapa para que pueda crear el marcador
            val sp = mContext.getSharedPreferences("Ubicacion", Context.MODE_PRIVATE)
            val ed = sp.edit()
            ed.putString("Hora", fichaje.hora)
            ed.putFloat("Longitud", fichaje.longitud)
            ed.putFloat("Latitud", fichaje.latitud)
            ed.commit()

            val fragmentActivity = mContext as FragmentActivity
            val fragmentManager: FragmentManager = fragmentActivity.supportFragmentManager
            MyCustomDialog().show(fragmentManager, "UbicacionFragment")
        }


        val iwEliminarFichaje: ImageView = view.findViewById(R.id.iw_eliminarFichaje)
        iwEliminarFichaje.setOnClickListener {
            val sp = mContext.getSharedPreferences("FichajeAEliminar", Context.MODE_PRIVATE)
            val ed = sp.edit()
            ed.putInt("IDFichaje", fichaje.ID)
            ed.commit()

            val fragmentActivity = mContext as FragmentActivity
            val fragmentManager: FragmentManager = fragmentActivity.supportFragmentManager
            BorrarFichajeDialog().show(fragmentManager, "EliminarFichajeFragment")
        }

        if(fichaje.incidencia != null) {

        }

        return view
    }

    /**
     * Comprueba si el fichaje es par o impar
     * @param num El número que se quiere comprobar
     */
    fun esPar(num: Int):Boolean {
        if (num % 2 == 0)
            return true
        else
            return false
    }
}