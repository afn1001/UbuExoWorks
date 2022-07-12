package com.example.ubuexoworks

import com.google.gson.JsonElement
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @POST("/api/login")
    fun login(@Body crdenciales: Credenciales) : Call<String>



}