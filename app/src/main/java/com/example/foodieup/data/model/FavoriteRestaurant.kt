package com.example.foodieup.data.model

import com.google.gson.annotations.SerializedName

data class FavoriteRestaurant(
    val id: Int,
    val restaurant: Int,
    @SerializedName("restaurant_name")
    val restaurantName: String,
    @SerializedName("restaurant_logo")
    val restaurantLogo: String?,
    val description: String?
)
