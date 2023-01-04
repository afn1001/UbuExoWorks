package com.example.ubuexoworks

import com.example.ubuexoworks.ClasesDeDatos.*
import retrofit2.Call
import retrofit2.http.*

/**
 *  Servicio para poder obtener contactar con todos los métodos de la Api
 *  @author Alejandro Fraga Neila
 */
interface ApiService {

    /**
     * Permite conectarse al método login de la API para entrar en la aplicación como usuario
     */
    @POST("/api/login")
    fun login(@Body crdenciales: Credenciales) : Call<String>

    /**
     * Permite conectarse al método registraDispositivo de la API para registrar un dispositivo
     */
    @POST("/api/registraDispositivo")
    fun registraDispositivo(@Body credenciales: Credenciales) : Call<String>

    /**
     * Permite conectarse al método fichaje de la API para introducir un nuevo fichaje
     */
    @POST("/api/fichaje")
    fun fichar(@Header("Authorization") Token: String?,
               @Body fichaje: Fichaje) : Call<String>

    /**
     * Permite conectarse al método fichaje de la API que permite obtener un fichaje en una fecha específica
     */
    @GET("/api/get/fichaje")
    fun getFichaje(@Header("Authorization") Token: String?,
                   @Query("idUsuario") id: Int,
                   @Query("fecha", encoded = true) fecha: String) : Call<ArrayList<FichajeObtenido>>


    /**
     * Permite conectarse al método registraGasto de la API que envía los datos del ticket
     */
    @POST("/api/gasto/registraGasto")
    fun registrarGasto(@Header("Authorization") Token: String?,
                       @Body gasto: Gasto) : Call<String>

    /**
     * Permite conectarse al método usuario de la API que permite recoger los datos de usuario
     */
    @GET("/api/get/usuario")
    fun getDatosUsuario(@Header("Authorization") Token: String?,
                        @Query("idUsuario") id: Int) : Call<String>

    /**
     * Permite conectarse al método recuperaPassword de la API que permite recuperar la contraseña
     */
    @GET("/api/usuario/recuperaPassword")
    fun recuperaContraseña(@Query("email") email: String) : Call<String>

    /**
     * Permite conectarse al método solicitudBorradoApp de la API que permite solicitar el borrado de un fichaje al administrador
     */
    @POST("/api/usuario/solicitudBorradoApp")
    fun solicitudBorrado(@Header("Authorization") Token: String?,
                         @Body fichajeEliminar: FichajeEliminar) : Call<String>

}