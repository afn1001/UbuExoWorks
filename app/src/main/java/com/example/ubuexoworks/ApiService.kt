package com.example.ubuexoworks

import com.example.ubuexoworks.ClasesDeDatos.Credenciales
import com.example.ubuexoworks.ClasesDeDatos.Fichaje
import com.example.ubuexoworks.ClasesDeDatos.FichajeObtenido
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @POST("/api/login")
    fun login(@Body crdenciales: Credenciales) : Call<String>

    @POST("/api/fichaje")
    fun fichar(@Body fichaje: Fichaje) : Call<String>

    @GET("/api/get/fichaje/fecha")
    fun getFichaje(@Query("idUsuario") id: String,
                   @Query("fecha") fecha: String) : Call<ArrayList<FichajeObtenido>>

}