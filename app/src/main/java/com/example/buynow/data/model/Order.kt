package com.example.buynow.data.model

class Order {
    data class Order(
        val order_id: String,
        val product_id: String,
        val user_id: String,
        val status: String
    )

    data class OrderCreate(
        val product_id: String,
        val user_id: String
    )

    data class OrderStatusUpdate(
        val status: String
    )
}