package com.example.foodieup.data.network

import com.example.foodieup.data.model.Address
import com.example.foodieup.data.model.AuthResponse
import com.example.foodieup.data.model.ChangeAddressRequest
import com.example.foodieup.data.model.CheckTokenRequest
import com.example.foodieup.data.model.CheckTokenResponse
import com.example.foodieup.data.model.CreateOrderRequest
import com.example.foodieup.data.model.LoginRequest
import com.example.foodieup.data.model.MenuItem
import com.example.foodieup.data.model.Order
import com.example.foodieup.data.model.RefreshTokenRequest
import com.example.foodieup.data.model.RefreshTokenResponse
import com.example.foodieup.data.model.RegisterRequest
import com.example.foodieup.data.model.Restaurant
import com.example.foodieup.data.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST("/api/users/login/")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("/api/users/register/")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("/api/users/check-tokens/")
    suspend fun checkTokens(@Body request: CheckTokenRequest): Response<CheckTokenResponse>

    @POST("/api/users/token/refresh/")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<RefreshTokenResponse>

    @GET("/api/users/profile/")
    suspend fun getProfile(@Header("Authorization") token: String): Response<User>

    @GET("/api/users/addresses/")
    suspend fun getAddresses(@Header("Authorization") token: String): Response<List<Address>>

    @GET("/api/restaurants/")
    suspend fun getRestaurants(@Header("Authorization") token: String): Response<List<Restaurant>>

    @GET("/api/restaurants/{restaurantId}/menu_items/")
    suspend fun getMenuItems(
        @Header("Authorization") token: String,
        @Path("restaurantId") restaurantId: Int
    ): Response<List<MenuItem>>

    @PATCH("api/users/change-address/")
    suspend fun changeAddress(
        @Header("Authorization") token: String,
        @Body request: ChangeAddressRequest
    ): Response<Unit>

    @GET("api/orders/")
    suspend fun getOrderHistory(@Header("Authorization") token: String): Response<List<Order>>

    @POST("/api/orders/")
    suspend fun createOrder(
        @Header("Authorization") token: String,
        @Body orderRequest: CreateOrderRequest
    ): Response<Order>

}
