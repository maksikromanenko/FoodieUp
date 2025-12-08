package com.example.foodieup

import android.app.Application
import com.example.foodieup.data.network.WebSocketService

class FoodieUpApp : Application() {
    override fun onCreate() {
        super.onCreate()
        WebSocketService.init(this)
    }
}
