package com.example.buynow.presentation.activity

import com.example.buynow.data.model.*
import com.example.buynow.presentation.fragment.ImportContactRequest
import com.google.android.gms.common.internal.safeparcel.SafeParcelable.Param
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiInterface {
    @FormUrlEncoded // MANDATORY: Indicates that the request body will be URL-encoded form data
    @POST("/login") // The endpoint for the login API
    suspend fun loginUser(
        @Field("grant_type") grantType: String = "password", // As per the original curl command
        @Field("username") username: String, // Maps to 'email' from your LoginRequest for the form data
        @Field("password") password: String,
        @Field("scope") scope: String = "", // As per the original curl command
        @Field("client_id") clientId: String = "string", // As per the original curl command
        @Field("client_secret") clientSecret: String = "string" // As per the original curl command
    ): LoginData

    @POST("/register")
    suspend fun register(@Body request: SignUpRequest): LoginData

    @POST("/users/import_contacts") // Use /import_contacts as per your backend
    suspend fun importContact(
        @Header("Authorization") authToken: String,
        @Body request: ImportContactRequest // <--- This is correct
    ): ImportContactResponse

    @GET("/")
    fun getProducts(
        @Header("Authorization") token: String = "Bearer eyJhbGciOi..."
    ): Call<ProductResponse>
}

