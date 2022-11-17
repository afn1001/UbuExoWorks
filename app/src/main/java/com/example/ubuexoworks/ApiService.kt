package com.example.ubuexoworks

import com.example.ubuexoworks.ClasesDeDatos.*
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @POST("/api/login")
    fun login(@Body crdenciales: Credenciales) : Call<String>

    @POST("/api/registraDispositivo")
    fun registraDispositivo(@Body credenciales: Credenciales) : Call<String>

    @POST("/api/fichaje")
    fun fichar(@Header("Authorization") Token: String?,
               @Body fichaje: Fichaje) : Call<String>

    @GET("/api/get/fichaje")
    fun getFichaje(@Header("Authorization") Token: String?,
                   @Query("idUsuario") id: Int,
                   @Query("fecha", encoded = true) fecha: String) : Call<ArrayList<FichajeObtenido>>


    @POST("/api/gasto/registraGasto")
    fun registrarGasto(@Body gasto: Gasto) : Call<String>

    @GET("/api/get/usuario")
    fun getDatosUsuario(@Header("Authorization") Token: String?,
                        @Query("idUsuario") id: Int) : Call<String>

    @GET("/api/usuario/recuperaPassword")
    fun recuperaContraseña(@Query("email") email: String) : Call<String>

    @POST("/api/usuario/solicitudBorrado")
    fun solicitudBorrado(@Header("Authorization") Token: String?,
                         @Body fichajeEliminar: FichajeEliminar) : Call<String>

}