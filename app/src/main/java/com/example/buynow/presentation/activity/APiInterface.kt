package com.example.buynow.presentation.activity

import com.example.buynow.data.model.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiInterface {
    @POST("/login")
    suspend fun loginUser(@Body request: LoginRequest): LoginData

    @POST("/register")
    suspend fun register(@Body request: SignUpRequest): LoginData
}

