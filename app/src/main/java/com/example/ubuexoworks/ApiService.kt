package com.example.ubuexoworks

import com.example.ubuexoworks.ClasesDeDatos.*
import retrofit2.Call
import retrofit2.http.*
import java.io.File

interface ApiService {

    @POST("/api/login")
    fun login(@Body crdenciales: Credenciales) : Call<String>

    @POST("/api/registraDispositivo")
    fun registraDispositivo(@Body credenciales: Credenciales) : Call<String>

    @POST("/api/fichaje")
    fun fichar(@Header("Authorization") Token: String?,
               @Body fichaje: Fichaje) : Call<String>

    @GET("/api/get/fichaje/fecha")
    fun getFichaje(@Header("Authorization") Token: String?,
                   @Query("idUsuario") id: Int,
                   @Query(value="fecha", encoded = true) fecha: String) : Call<ArrayList<FichajeObtenido>>


    @POST("/api/gasto/registraGasto")
    fun registrarGasto(@Body gasto: Gasto) : Call<String>

    @GET("/api/get/usuario")
    fun getDatosUsuario(@Header("Authorization") Token: String?,
                        @Query("idUsuario") id: Int) : Call<String>



}