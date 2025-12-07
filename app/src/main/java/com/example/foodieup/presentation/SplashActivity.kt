package com.example.foodieup.presentation

import android.animation.Animator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.example.foodieup.R
import com.example.foodieup.data.model.CheckTokenRequest
import com.example.foodieup.data.model.RefreshTokenRequest
import com.example.foodieup.data.model.User
import com.example.foodieup.data.network.RetrofitClient
import com.example.foodieup.data.storage.RestaurantManager
import com.example.foodieup.data.storage.TokenManager
import com.example.foodieup.data.storage.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager
    private val TAG = "SplashActivity"

    private sealed class NavigationTarget {
        data class Main(val user: User) : NavigationTarget()
        data class Courier(val user: User) : NavigationTarget()
        object Login : NavigationTarget()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_splash)

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController?.isAppearanceLightStatusBars = true
        insetsController?.isAppearanceLightNavigationBars = true
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        tokenManager = TokenManager(applicationContext)

        val animationView = findViewById<LottieAnimationView>(R.id.animation_view)
        animationView.speed = 1.5f
        animationView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                lifecycleScope.launch {
                    when (val destination = decideNextScreen()) {
                        is NavigationTarget.Main -> navigateToMain(destination.user)
                        is NavigationTarget.Courier -> navigateToCourier(destination.user)
                        is NavigationTarget.Login -> navigateToLogin()
                    }
                }
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
    }

    private suspend fun decideNextScreen(): NavigationTarget {
        val accessToken = tokenManager.getAccessToken().first()
        val refreshToken = tokenManager.getRefreshToken().first()

        if (accessToken == null || refreshToken == null) {
            return NavigationTarget.Login
        }

        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.apiService.checkTokens(CheckTokenRequest(accessToken, refreshToken))
                if (!response.isSuccessful) {
                    return@withContext NavigationTarget.Login
                }

                val checkResponse = response.body()!!
                var currentAccessToken = accessToken

                if (!checkResponse.accessValid) {
                    if (checkResponse.refreshValid) {
                        val refreshResponse = RetrofitClient.apiService.refreshToken(RefreshTokenRequest(refreshToken))
                        if (refreshResponse.isSuccessful) {
                            val newAccessToken = refreshResponse.body()!!.access
                            tokenManager.saveTokens(newAccessToken, refreshToken)
                            currentAccessToken = newAccessToken
                        } else {
                            return@withContext NavigationTarget.Login
                        }
                    } else {
                        return@withContext NavigationTarget.Login
                    }
                }

                val authHeader = "Bearer $currentAccessToken"
                val profileResponse = RetrofitClient.apiService.getProfile(authHeader)
                if (profileResponse.isSuccessful) {
                    val user = profileResponse.body()!!
                    UserManager.currentUser = user
                    when (user.role) {
                        "customer" -> NavigationTarget.Main(user)
                        "courier" -> NavigationTarget.Courier(user)
                        else -> NavigationTarget.Login
                    }
                } else {
                    NavigationTarget.Login
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deciding next screen", e)
                NavigationTarget.Login
            }
        }!!
    }

    private suspend fun fetchCustomerData(authHeader: String): Boolean {
        return try {
            val addressResponse = RetrofitClient.apiService.getAddresses(authHeader)
            if (addressResponse.isSuccessful) {
                UserManager.userAddress = addressResponse.body()
            }
            val restaurantsResponse = RetrofitClient.apiService.getRestaurants(authHeader)
            if (restaurantsResponse.isSuccessful) {
                RestaurantManager.restaurants = restaurantsResponse.body()
            }
            val favoriteRestaurantsResponse = RetrofitClient.apiService.getFavoriteRestaurants(authHeader)
            if (favoriteRestaurantsResponse.isSuccessful) {
                UserManager.favoriteRestaurants = favoriteRestaurantsResponse.body()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching customer data", e)
            false
        }
    }

    private fun navigateToMain(user: User) {
        lifecycleScope.launch {
            val token = tokenManager.getAccessToken().first()!!
            val authHeader = "Bearer $token"
            if (fetchCustomerData(authHeader)) {
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                navigateToLogin()
            }
        }
    }

    private fun navigateToCourier(user: User) {
        val intent = Intent(this, CourierActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LogInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
