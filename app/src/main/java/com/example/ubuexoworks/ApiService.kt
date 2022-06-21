package com.example.ubuexoworks

import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @FormUrlEncoded
    @POST("/auth")
    fun login(@Field("funcion") function: String,
              @Field("username") username: String,
              @Field("password") password: String) : Call<String>

}