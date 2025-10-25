package com.example.foodieup.data.model

import com.google.gson.annotations.SerializedName

data class MenuItem(
    val id: Int,
    val name: String,
    val restaurant: Int,
    val description: String?,
    val price: String,
    @SerializedName("is_available")
    val isAvailable: Boolean
)
