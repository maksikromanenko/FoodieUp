package com.example.foodieup.data.storage

import com.example.foodieup.data.model.Address
import com.example.foodieup.data.model.User

object UserManager {
    var currentUser: User? = null
    var userAddress: List<Address>? = null

    fun clearUser() {
        currentUser = null
        userAddress = null
    }
}
