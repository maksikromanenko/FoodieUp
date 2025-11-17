package com.example.foodieup.data.storage

import com.example.foodieup.data.model.Address
import com.example.foodieup.data.model.FavoriteRestaurant
import com.example.foodieup.data.model.User

object UserManager {
    var currentUser: User? = null
    var userAddress: List<Address>? = null
    var favoriteRestaurants: List<FavoriteRestaurant>? = null

    fun clearUser() {
        currentUser = null
        userAddress = null
        favoriteRestaurants = null
    }
}
