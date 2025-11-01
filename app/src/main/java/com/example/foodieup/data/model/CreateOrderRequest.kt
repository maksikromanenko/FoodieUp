package com.example.foodieup.data.model

import com.google.gson.annotations.SerializedName

data class CreateOrderRequest(
    @SerializedName("restaurant_id")
    val restaurantId: Int,
    val address: Int,
    val items: List<OrderItem>
)



