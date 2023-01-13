package com.example.ubuexoworks.ClasesDeDatos

/**
 * Clase con los datos de los fichajes realizados que queremos obtener del calendario
 * @author Alejandro Fraga Neila
 */
data class FichajeObtenido(
    val ID: Int,
    val borrado: Boolean,
    val fecha: String,
    val hora: String,
    val incidencia: String,
    val latitud: Float,
    val longitud: Float,
    val tiempoTrabajado: String,
    val tipo: String,
    val solicitaBorrado: Boolean
)
