package com.example.ubuexoworks.ClasesDeDatos

data class Usuario(
    val apellidos: String,
    val estado: Boolean,
    val idEmpresa: Int,
    val idJornadaLaboral: Int,
    val idRol: Int,
    val login: String,
    val nombre: String,
    val password: String
)
