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

data class SimilarProduct(
    val timestamp: String,
    val productBrand: String,
    val name: String,
    val relation: String,
    val productName: String,
    val productId: Int
)
data class ProductById (
    val product :Product,
    val same_brand: List<SimilarProduct>,
    val same_product: List<SimilarProduct>
)

data class AiRequest(val productId: List<Int>)

data class AiResponse(val message: String)