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
import com.example.foodieup.data.model.RegisterRequest
import com.example.foodieup.data.network.RetrofitClient
import com.example.foodieup.data.storage.RestaurantManager
import com.example.foodieup.data.storage.TokenManager
import com.example.foodieup.data.storage.UserManager
import kotlinx.coroutines.launch
import java.io.IOException

class RegActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager
    private val TAG = "RegActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reg)

        tokenManager = TokenManager(applicationContext)

        val userLogin = findViewById<EditText>(R.id.user_login_reg)
        val userPassword = findViewById<EditText>(R.id.user_password_reg)
        val userPassword2 = findViewById<EditText>(R.id.user_password2_reg)
        val signUpButton = findViewById<Button>(R.id.button_sign_up)
        val logInText = findViewById<TextView>(R.id.log_in_text)

        signUpButton.setOnClickListener {
            val login = userLogin.text.toString().trim()
            val password = userPassword.text.toString().trim()
            val password2 = userPassword2.text.toString().trim()

            if (login.isNotEmpty() && password.isNotEmpty() && password2.isNotEmpty()) {
                if (password != password2) {
                    Log.w(TAG, "Registration attempt failed: Passwords do not match")
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                Log.i(TAG, "Sign Up button clicked for user: $login")
                val registerRequest = RegisterRequest(username = login, password = password, passwordConfirm = password2)

                lifecycleScope.launch {
                    try {
                        Log.d(TAG, "Attempting to register...")
                        val response = RetrofitClient.apiService.register(registerRequest)
                        if (response.isSuccessful) {
                            val authResponse = response.body()
                            if (authResponse != null) {
                                tokenManager.saveTokens(authResponse.access, authResponse.refresh)
                                UserManager.currentUser = authResponse.user
                                Log.i(TAG, "Registration successful: ${authResponse.message}")

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

                                Toast.makeText(this@RegActivity, "Registration Successful: ${authResponse.message}", Toast.LENGTH_LONG).show()
                                val intent = Intent(this@RegActivity, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            } else {
                                Log.e(TAG, "Registration failed: Response body is null")
                                Toast.makeText(this@RegActivity, "Registration failed: Empty response", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            val errorBody = response.errorBody()?.string()
                            Log.e(TAG, "Registration failed with code ${response.code()}: $errorBody")
                            Toast.makeText(this@RegActivity, "Registration failed: $errorBody", Toast.LENGTH_LONG).show()
                        }                    } catch (e: IOException) {
                        Log.e(TAG, "Network error during registration", e)
                        Toast.makeText(this@RegActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Log.e(TAG, "An unexpected error occurred during registration", e)
                        Toast.makeText(this@RegActivity, "An unexpected error occurred: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Log.w(TAG, "Registration attempt with empty fields")
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        logInText.setOnClickListener {
            finish()
        }
    }
}
