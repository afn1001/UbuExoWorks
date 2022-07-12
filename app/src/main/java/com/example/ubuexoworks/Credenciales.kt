package com.example.ubuexoworks

import kotlinx.serialization.Serializable

@Serializable
data class Credenciales(
    val login: String,
    val password: String
)
