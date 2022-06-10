package com.example.ubuexoworks

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TextView



/**
 * A simple [Fragment] subclass.
 * Use the [ConsultarCalendario.newInstance] factory method to
 * create an instance of this fragment.
 */
class ConsultarCalendario : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,  savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view : View =  inflater.inflate(R.layout.fragment_consultar_calendario, container, false)

        val txtEntrada = view.findViewById<TextView>(R.id.txtHoraEntrada)
        val txtSalida = view.findViewById<TextView>(R.id.txtHoraSalida)

        val calendario = view.findViewById<CalendarView>(R.id.cdvCalendario)
        calendario.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val mes = "" + month
            val diaDelMes = "" + dayOfMonth
            txtEntrada.setText(mes)
            txtSalida.setText(diaDelMes)
        }

        return view
    }
}