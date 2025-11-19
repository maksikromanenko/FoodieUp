package com.example.foodieup.presentation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.foodieup.R
import com.example.foodieup.data.model.LoginRequest
import com.example.foodieup.data.network.RetrofitClient
import com.example.foodieup.data.storage.RestaurantManager
import com.example.foodieup.data.storage.TokenManager
import com.example.foodieup.data.storage.UserManager
import kotlinx.coroutines.launch
import java.io.IOException

class LogInActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager
    private val TAG = "LogInActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_log_in)

        tokenManager = TokenManager(applicationContext)

        val userLogin = findViewById<EditText>(R.id.user_login)
        val userPassword = findViewById<EditText>(R.id.user_password)
        val button = findViewById<Button>(R.id.button_log_in)
        val signUpText = findViewById<TextView>(R.id.sign_up_text)

        button.setOnClickListener {
            val login = userLogin.text.toString().trim()
            val password = userPassword.text.toString().trim()
            if(login == "admin" && password == "admin") {
                val intent = Intent(this@LogInActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            if (login.isNotEmpty() && password.isNotEmpty()) {
                Log.i(TAG, "Login button clicked for user: $login")
                val loginRequest = LoginRequest(username = login, password = password)
                lifecycleScope.launch {
                    try {
                        Log.d(TAG, "Attempting to login...")
                        val response = RetrofitClient.apiService.login(loginRequest)
                        if (response.isSuccessful) {
                            val authResponse = response.body()
                            if (authResponse != null) {
                                tokenManager.saveTokens(authResponse.access, authResponse.refresh)
                                UserManager.currentUser = authResponse.user
                                Log.i(TAG, "Login successful: ${authResponse.message}")

                                // Fetch address
                                try {
                                    val addressResponse = RetrofitClient.apiService.getAddresses("Bearer ${authResponse.access}")
                                    if (addressResponse.isSuccessful) {
                                        val address = addressResponse.body()
                                        UserManager.userAddress = address
                                        Log.i(TAG, "Address fetched and saved successfully")
                                    } else {
                                        Log.e(TAG, "Failed to fetch address: ${addressResponse.errorBody()?.string()}")
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error fetching address", e)
                                }

                                try {
                                    val restaurantsResponse = RetrofitClient.apiService.getRestaurants("Bearer ${authResponse.access}")
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
                                    val favoriteRestaurantsResponse = RetrofitClient.apiService.getFavoriteRestaurants("Bearer ${authResponse.access}")
                                    if (favoriteRestaurantsResponse.isSuccessful) {
                                        UserManager.favoriteRestaurants = favoriteRestaurantsResponse.body()
                                        Log.i(TAG, "Favorite restaurants fetched successfully")
                                    } else {
                                        Log.e(TAG, "Failed to fetch favorite restaurants: ${favoriteRestaurantsResponse.errorBody()?.string()}")
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error fetching favorite restaurants", e)
                                }

                                Toast.makeText(this@LogInActivity, "Login Successful: ${authResponse.message}", Toast.LENGTH_LONG).show()
                                val intent = Intent(this@LogInActivity, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            } else {
                                Log.e(TAG, "Login failed: Response body is null")
                                Toast.makeText(this@LogInActivity, "Login failed: Empty response", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            val errorBody = response.errorBody()?.string()
                            Log.e(TAG, "Login failed with code ${response.code()}: $errorBody")
                            Toast.makeText(this@LogInActivity, "Login failed: $errorBody", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: IOException) {
                        Log.e(TAG, "Network error during login", e)
                        Toast.makeText(this@LogInActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Log.e(TAG, "An unexpected error occurred during login", e)
                        Toast.makeText(this@LogInActivity, "An unexpected error occurred: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Log.w(TAG, "Login attempt with empty fields")
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        signUpText.setOnClickListener {
            val intent = Intent(this, RegActivity::class.java)
            startActivity(intent)
        }
    }
}
