package com.example.ubuexoworks.ClasesDeDatos

import kotlinx.serialization.Serializable

/**
 * Clase de datos con las credenciales de inicio de sesión de un usuario
 * @author Alejandro Fraga Neila
 */
@Serializable
data class Credenciales(
    val login: String,
    val password: String,
    val imei: String
)
