package com.example.foodieup.data.model

import com.google.gson.annotations.SerializedName

data class Restaurant(
    val id: Int,
    val name: String,
    val description: String?,
    @SerializedName("logo_url")
    val logoUrl: String?,
    val rating: Double?
)