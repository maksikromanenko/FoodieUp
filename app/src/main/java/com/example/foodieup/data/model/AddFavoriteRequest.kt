package com.example.foodieup.data.model

import com.google.gson.annotations.SerializedName

data class AddFavoriteRequest(
    @SerializedName("restaurant")
    val restaurant_id: Int
)
