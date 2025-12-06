package com.example.foodieup.data.model

import com.google.gson.annotations.SerializedName

data class MenuItem(
    val id: Int,
    val name: String,
    val restaurant: Int,
    val description: String?,
    val price: String,
    @SerializedName("is_available")
    val isAvailable: Boolean,
    @SerializedName("logo_url")
    val logoUrl: String?,
    @SerializedName("discount_percentage")
    val discountPercentage: Int?,
    @SerializedName("final_price")
    val finalPrice: String?,
    @SerializedName("is_new")
    val isNew: Boolean?
)
