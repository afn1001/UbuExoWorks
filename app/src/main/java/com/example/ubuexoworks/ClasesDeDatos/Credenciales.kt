package com.example.ubuexoworks.ClasesDeDatos

import kotlinx.serialization.Serializable

@Serializable
data class Credenciales(
    val login: String,
    val password: String
)
