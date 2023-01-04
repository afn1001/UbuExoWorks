package com.example.ubuexoworks.ClasesDeDatos

import kotlinx.serialization.Serializable

/**
 * Clase con los datos del fichaje que realizamos
 * @author Alejandro Fraga Neila
 */
@Serializable
data class Fichaje(
    val idUsuario: Int,
    val fecha: String,
    val hora: String,
    val longitud: String,
    val latitud: String,
)