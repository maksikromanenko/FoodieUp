package com.example.foodieup.data.model

import com.google.gson.annotations.SerializedName

data class Order(
    val id: Int,
    @SerializedName("restaurant_id")
    val restaurantId: Int,
    @SerializedName("restaurant_name")
    val restaurantName: String,
    @SerializedName("logo_url")
    val logoUrl: String?,
    val rating: Int?,
    val address: Int,
    val items: List<OrderItem>,
    @SerializedName("total_price")
    val totalPrice: String,
    val status: String?
)

data class OrderItem(
    @SerializedName("menu_item")
    val menuItem: Int,
    val quantity: Int
)

data class UpdateOrderStatusRequest(
    val status: String
)
