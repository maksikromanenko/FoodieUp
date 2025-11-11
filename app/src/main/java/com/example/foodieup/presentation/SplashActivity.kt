package com.example.foodieup.presentation

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.example.foodieup.R
import com.example.foodieup.data.model.CheckTokenRequest
import com.example.foodieup.data.model.RefreshTokenRequest
import com.example.foodieup.data.network.RetrofitClient
import com.example.foodieup.data.storage.RestaurantManager
import com.example.foodieup.data.storage.TokenManager
import com.example.foodieup.data.storage.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager
    private val TAG = "SplashActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        tokenManager = TokenManager(applicationContext)

        val navigationActionDeferred = lifecycleScope.async { determineNavigationAction() }

        val animationView = findViewById<LottieAnimationView>(R.id.animation_view)
        animationView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
            }

            override fun onAnimationEnd(animation: Animator) {
                lifecycleScope.launch {
                    navigationActionDeferred.await()()
                }
            }

            override fun onAnimationCancel(animation: Animator) {
            }

            override fun onAnimationRepeat(animation: Animator) {
            }
        })
    }

    private suspend fun determineNavigationAction(): () -> Unit {
        val accessToken = tokenManager.getAccessToken().first()
        val refreshToken = tokenManager.getRefreshToken().first()
        return if (accessToken == null || refreshToken == null) {
            Log.i(TAG, "No tokens found, navigating to LogInActivity")
            ::navigateToLogin
        } else {
            Log.d(TAG, "Tokens found, checking validity...")
            checkTokenValidity(accessToken, refreshToken)
        }
    }

    private suspend fun checkTokenValidity(accessToken: String, refreshToken: String): () -> Unit {
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.apiService.checkTokens(CheckTokenRequest(accessToken, refreshToken))
                if (response.isSuccessful) {
                    val checkResponse = response.body()!!
                    when {
                        checkResponse.accessValid && checkResponse.refreshValid -> {
                            Log.i(TAG, "Both tokens are valid, fetching data...")
                            fetchData(accessToken)
                            Log.i(TAG, "Data fetched, navigating to MainActivity")
                            ::navigateToMain
                        }
                        !checkResponse.accessValid && checkResponse.refreshValid -> {
                            Log.i(TAG, "Access token is invalid, refreshing token...")
                            refreshAccessToken(refreshToken)
                        }
                        else -> {
                            Log.i(TAG, "Both tokens are invalid, navigating to LogInActivity")
                            ::navigateToLogin
                        }
                    }
                } else {
                    Log.e(TAG, "checkTokens request failed with code ${response.code()}")
                    ::navigateToLogin
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking tokens", e)
                ::navigateToLogin
            }
        }
    }

    private suspend fun refreshAccessToken(refreshToken: String): () -> Unit {
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.apiService.refreshToken(RefreshTokenRequest(refreshToken))
                if (response.isSuccessful) {
                    val newAccessToken = response.body()!!.access
                    tokenManager.saveTokens(newAccessToken, refreshToken)
                    Log.i(TAG, "Token refreshed successfully, fetching data...")
                    fetchData(newAccessToken)
                    Log.i(TAG, "Data fetched, navigating to MainActivity")
                    ::navigateToMain
                } else {
                    Log.e(TAG, "refreshToken request failed with code ${response.code()}")
                    ::navigateToLogin
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing token", e)
                ::navigateToLogin
            }
        }
    }

    private suspend fun fetchData(accessToken: String) {

        try {
            val addressResponse = RetrofitClient.apiService.getAddresses("Bearer $accessToken")
            if (addressResponse.isSuccessful) {
                UserManager.userAddress = addressResponse.body()
                Log.i(TAG, "Address fetched and saved successfully")
            } else {
                Log.e(TAG, "Failed to fetch address: ${addressResponse.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching address", e)
        }


        try {
            val restaurantsResponse = RetrofitClient.apiService.getRestaurants("Bearer $accessToken")
            if (restaurantsResponse.isSuccessful) {
                RestaurantManager.restaurants = restaurantsResponse.body()
                Log.i(TAG, "Restaurants fetched successfully")
            } else {
                Log.e(TAG, "Failed to fetch restaurants: ${restaurantsResponse.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching restaurants", e)
        }


        try {
            val profileResponse = RetrofitClient.apiService.getProfile("Bearer $accessToken")
            if (profileResponse.isSuccessful) {
                UserManager.currentUser = profileResponse.body()
                Log.i(TAG, "Profile fetched successfully")
            } else {
                Log.e(TAG, "Failed to fetch profile: ${profileResponse.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching profile", e)
        }
    }


    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
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
