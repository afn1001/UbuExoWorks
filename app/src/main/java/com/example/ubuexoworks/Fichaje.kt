package com.example.ubuexoworks

import kotlinx.serialization.Serializable

@Serializable
data class Fichaje(
    val idUsuario: Int,
    val fecha: String,
    val hora: String,
    val longitud: String,
    val latitud: String,
)