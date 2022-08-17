package com.example.ubuexoworks

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.ubuexoworks.ClasesDeDatos.FichajeObtenido

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
        tvHora.setText(fichaje.hora_entrada)

        val tvLongitud: TextView = view.findViewById(R.id.tv_longitud)
        tvLongitud.setText(fichaje.entradaLongitud.toString())

        val tvLatitud: TextView = view.findViewById(R.id.tv_latitud)
        tvLatitud.setText(fichaje.entradaLatitud.toString())

        return view
    }
}