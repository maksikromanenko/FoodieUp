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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager
    private val TAG = "SplashActivity"

    private enum class NavigationTarget {
        MAIN_ACTIVITY,
        LOGIN_ACTIVITY
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        tokenManager = TokenManager(applicationContext)

        val animationView = findViewById<LottieAnimationView>(R.id.animation_view)
        animationView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                lifecycleScope.launch {
                    val destination = decideNextScreen()
                    when (destination) {
                        NavigationTarget.MAIN_ACTIVITY -> navigateToMain()
                        NavigationTarget.LOGIN_ACTIVITY -> navigateToLogin()
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
            Log.i(TAG, "Токены не найдены, переход на LogInActivity")
            return NavigationTarget.LOGIN_ACTIVITY
        }
        Log.d(TAG, "Токены найдены, проверка валидности...")
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.apiService.checkTokens(CheckTokenRequest(accessToken, refreshToken))
                if (!response.isSuccessful) {
                    Log.e(TAG, "Запрос checkTokens завершился с ошибкой ${response.code()}, переход на Login.")
                    return@withContext NavigationTarget.LOGIN_ACTIVITY
                }
                val checkResponse = response.body()!!
                var currentAccessToken = accessToken

                if (!checkResponse.accessValid && checkResponse.refreshValid) {
                    Log.i(TAG, "Access токен невалиден, refresh токен валиден. Обновление...")
                    val refreshResponse = RetrofitClient.apiService.refreshToken(RefreshTokenRequest(refreshToken))
                    if (refreshResponse.isSuccessful) {
                        val newAccessToken = refreshResponse.body()!!.access
                        tokenManager.saveTokens(newAccessToken, refreshToken)
                        currentAccessToken = newAccessToken
                        Log.i(TAG, "Токен успешно обновлен.")
                    } else {
                        Log.e(TAG, "Не удалось обновить токен, код: ${refreshResponse.code()}. Переход на Login.")
                        return@withContext NavigationTarget.LOGIN_ACTIVITY
                    }
                } else if (!checkResponse.accessValid && !checkResponse.refreshValid) {
                    Log.i(TAG, "Оба токена невалидны. Переход на Login.")
                    return@withContext NavigationTarget.LOGIN_ACTIVITY
                }

                Log.i(TAG, "Токен валиден. Загрузка начальных данных...")
                val dataFetchedSuccessfully = fetchData(currentAccessToken)

                if (dataFetchedSuccessfully) {
                    Log.i(TAG, "Данные успешно загружены. Переход на MainActivity.")
                    return@withContext NavigationTarget.MAIN_ACTIVITY
                } else {
                    Log.e(TAG, "Не удалось загрузить начальные данные. Переход на Login.")
                    return@withContext NavigationTarget.LOGIN_ACTIVITY
                }

            } catch (e: Exception) {
                Log.e(TAG, "Произошла ошибка во время проверки токена или загрузки данных.", e)
                return@withContext NavigationTarget.LOGIN_ACTIVITY
            }
        }
    }

    private suspend fun fetchData(accessToken: String): Boolean {
        val authHeader = "Bearer $accessToken"
        try {
            val profileResponse = RetrofitClient.apiService.getProfile(authHeader)
            if (profileResponse.isSuccessful) {
                UserManager.currentUser = profileResponse.body()
                Log.i(TAG, "Профиль успешно загружен")
            } else {
                Log.e(TAG, "Не удалось загрузить профиль: ${profileResponse.code()}")
                return false
            }

            val restaurantsResponse = RetrofitClient.apiService.getRestaurants(authHeader)
            if (restaurantsResponse.isSuccessful) {
                RestaurantManager.restaurants = restaurantsResponse.body()
                Log.i(TAG, "Рестораны успешно загружены")
            } else {
                Log.e(TAG, "Не удалось загрузить рестораны: ${restaurantsResponse.code()}")
                return false
            }
            
            val addressResponse = RetrofitClient.apiService.getAddresses(authHeader)
            if (addressResponse.isSuccessful) {
                UserManager.userAddress = addressResponse.body()
                Log.i(TAG, "Адреса успешно загружены и сохранены")
            } else {
                Log.e(TAG, "Не удалось загрузить адреса: ${addressResponse.code()}")
            }

            return true
        } catch (e: Exception) {
            Log.e(TAG, "Сетевая ошибка при загрузке данных", e)
            return false
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
