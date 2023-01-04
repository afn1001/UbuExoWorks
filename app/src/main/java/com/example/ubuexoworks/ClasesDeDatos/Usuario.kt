package com.example.ubuexoworks.ClasesDeDatos

/**
 * Clase con los datos del usuario para que los muestre por pantalla en el SlidePanel
 * @author Alejandro Fraga Neila
 */
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
