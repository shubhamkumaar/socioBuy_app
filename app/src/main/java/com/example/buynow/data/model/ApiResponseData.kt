package com.example.buynow.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginData(
    val message: String,
    val success: Boolean,
    @SerializedName("access_token")
    val accessToken:String?=null,
    val email:String,
    val phone:String,
    val name:String?=null,
    val imageUrl:String?=null
)

data class SignUpRequest(
    val name: String,
    val email: String,
    val phone: String,
    val password: String
)

data class ImportContactResponse(
    val message: String
)