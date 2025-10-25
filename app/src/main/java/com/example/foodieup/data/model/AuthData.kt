package com.example.foodieup.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val password: String,
    @SerializedName("password_confirm")
    val passwordConfirm: String
)

data class AuthResponse(
    val user: User,
    val refresh: String,
    val access: String,
    val message: String
)

data class User(
    val id: String,
    val username: String,
    val email: String,
    val role: String,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    @SerializedName("full_name")
    val fullName: String,
    @SerializedName("current_address")
    val addressid: String
)

data class Address(
    val id: Int,
    @SerializedName("address_line")
    val addressLine: String,
    val city: String,
    @SerializedName("postal_code")
    val postalCode: String,
    val location: String
)

data class ChangeAddressRequest(
    @SerializedName("new_address")
    val newAddressId: Int
)

data class CheckTokenRequest(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("refresh_token")
    val refreshToken: String
)

data class CheckTokenResponse(
    @SerializedName("access_valid")
    val accessValid: Boolean,
    @SerializedName("refresh_valid")
    val refreshValid: Boolean
)

data class RefreshTokenRequest(
    val refresh: String
)

data class RefreshTokenResponse(
    val access: String
)
