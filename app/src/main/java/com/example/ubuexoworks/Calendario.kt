package com.example.ubuexoworks

import android.os.Bundle
import android.widget.CalendarView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Calendario : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendario_layout)

        val txtEntrada = findViewById<TextView>(R.id.txtHoraEntrada)
        val txtSalida = findViewById<TextView>(R.id.txtHoraSalida)

        val calendario = findViewById<CalendarView>(R.id.cdvCalendario)
        calendario.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val mes = "" + month
            val diaDelMes = "" + dayOfMonth
            txtEntrada.setText(mes)
            txtSalida.setText(diaDelMes)
        }


    }
}