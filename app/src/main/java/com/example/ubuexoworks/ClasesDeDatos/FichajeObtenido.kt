package com.example.ubuexoworks.ClasesDeDatos

data class FichajeObtenido(
    val ID: Int,
    val borrado: Boolean,
    val fecha: String,
    val hora: String,
    val incidencia: String,
    val latitud: Float,
    val longitud: Float,
    val tiempoTrabajado: String,
    val tipo: String
)
