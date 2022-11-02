package com.example.ubuexoworks

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

class AyudaContador(context: Context) {

    companion object {
        const val PREFERENCES = "prefs"
        const val START_TIME_KEY = "startKey"
        const val STOP_TIME_KEY = "stopKey"
        const val COUNTING_KEY = "countingKey"
    }

    private var sharedPref : SharedPreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
    private var formatoFecha = SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault())

    private var estaContando = false
    private var tiempoInicio: Date? = null
    private var tiempoParar: Date? = null

    init {
        estaContando = sharedPref.getBoolean(COUNTING_KEY, false)

        val temporizadorInicialString = sharedPref.getString(START_TIME_KEY, null)
        if (temporizadorInicialString != null)
            tiempoInicio = formatoFecha.parse(temporizadorInicialString)

        val stopString = sharedPref.getString(STOP_TIME_KEY, null)
        if (stopString != null)
            tiempoParar = formatoFecha.parse(stopString)
    }


    fun startTime(): Date? = tiempoInicio

    fun setTiempoIniciar(date: Date?) {
        tiempoInicio = date
        with(sharedPref.edit()) {
            val fechaString = if (date == null) null else formatoFecha.format(date)
            putString(START_TIME_KEY,fechaString)
            apply()
        }
    }

    fun pararTiempo(): Date? = tiempoParar

    fun setTiempoParar(date: Date?) {
        tiempoParar = date
        with(sharedPref.edit()) {
            val fechaString = if (date == null) null else formatoFecha.format(date)
            putString(STOP_TIME_KEY,fechaString)
            apply()
        }
    }

    fun estaContando(): Boolean = estaContando

    fun setEstaContando(value: Boolean) {
        estaContando = value
        with(sharedPref.edit()) {
            putBoolean(COUNTING_KEY,value)
            apply()
        }
    }


}