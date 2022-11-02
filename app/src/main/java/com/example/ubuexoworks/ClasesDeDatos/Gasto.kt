package com.example.ubuexoworks.ClasesDeDatos

import java.io.File

data class Gasto(
    val ticket: File?,
    val idUsuario: String,
    val fecha: String,
    val tipo: String,
    val descripcion: String,
    val importe: Double,
    val iva: Double,
    val cif: String,
    val razonSocial: String,
    val numeroTicket: String
)