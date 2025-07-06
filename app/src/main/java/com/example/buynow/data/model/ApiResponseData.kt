package com.example.buynow.data.model

data class LoginRequest(
    val email: String,
    val password: String
)
data class SignUpRequest(
    val email: String,
    val password: String,
    val name: String,
    val phone: String
)
data class LoginData(
    val message: String,
    val success: Boolean,
    val token:String?=null,
    val email:String,
    val phone:String,
    val name:String?=null,
    val imageUrl:String?=null
)
data class SignupData(
    val name:String
)