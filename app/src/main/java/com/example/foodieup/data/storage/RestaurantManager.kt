package com.example.foodieup.data.storage

import com.example.foodieup.data.model.Restaurant

object RestaurantManager {
    var restaurants: List<Restaurant>? = null

    fun clearRestaurants() {
        restaurants = null
    }
}
