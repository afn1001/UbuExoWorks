package com.example.ubuexoworks.ClasesDeDatos

data class FichajeObtenido(
    val borrado: Boolean,
    val entradaLatitud: Float,
    val entradaLongitud: Float,
    val fecha: String,
    val hora_entrada:String,
    val idFichaje: Int,
    val idUsuario: Int,
    val incidencia: String,
    val tipo: String
)
