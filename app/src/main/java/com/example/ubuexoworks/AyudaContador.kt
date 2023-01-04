package com.example.ubuexoworks

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

/**
 * Clase que sirve como apoyo para mantener el tiempo del contador almacenado si se cierra la aplicación
 * @author Alejandro Fraga Neila
 */
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


    /**
     * Se introduce el tiempo de inicio almacenado en el sharedPref
     */
    fun startTime(): Date? = tiempoInicio


    /**
     * Sirve para que se sepa con qué tiempo queremos que se ponga el contador al reiniciar la aplicación
     * @param date Hora a la que queremos poner el contador
     */
    fun setTiempoIniciar(date: Date?) {
        tiempoInicio = date
        with(sharedPref.edit()) {
            val fechaString = if (date == null) null else formatoFecha.format(date)
            putString(START_TIME_KEY,fechaString)
            apply()
        }
    }

    /**
     * Se para el tiempo
     */
    fun pararTiempo(): Date? = tiempoParar

    /**
     * Sirve para que se sepa con qué tiempo queremos que se ponga el contador al reiniciar la aplicación
     * @param date Hora a la que queremos poner el contador
     */
    fun setTiempoParar(date: Date?) {
        tiempoParar = date
        with(sharedPref.edit()) {
            val fechaString = if (date == null) null else formatoFecha.format(date)
            putString(STOP_TIME_KEY,fechaString)
            apply()
        }
    }

    /**
     * Devuelve si el contador está funcionando o está parado
     */
    fun estaContando(): Boolean = estaContando

    /**
     * Introducimos el estado del contador (funcionando o parado) y se introduce al ShredPreferences
     * @param value True o False dependiendo si está funcionando o parado
     */
    fun setEstaContando(value: Boolean) {
        estaContando = value
        with(sharedPref.edit()) {
            putBoolean(COUNTING_KEY,value)
            apply()
        }
    }


}