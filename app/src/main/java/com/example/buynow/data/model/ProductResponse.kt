package com.example.buynow.data.model


data class ProductResponse(

    val categories: Map<String, List<Product>>,
    val cover_products: List<Product>?=null

)
