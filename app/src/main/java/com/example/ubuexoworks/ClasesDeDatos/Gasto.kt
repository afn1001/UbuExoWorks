package com.example.ubuexoworks.ClasesDeDatos

import java.io.File

/**
 * Clase con los datos del ticket que queremos enviar
 * @author Alejandro Fraga Neila
 */
data class Gasto(
    val foto: String,
    val idUsuario: Int,
    val fecha: String,
    val tipo: String,
    val importe: Double,
    val iva: Double,
    val cif: String,
    val razonSocial: String,
    val descripcion: String,
    val numeroTicket: String
)
