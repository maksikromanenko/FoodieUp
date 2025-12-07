package com.example.foodieup.presentation

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodieup.data.model.Order
import com.example.foodieup.data.model.UpdateOrderStatusRequest
import com.example.foodieup.data.network.RetrofitClient
import com.example.foodieup.data.storage.TokenManager
import com.example.foodieup.databinding.ActivityCourierBinding
import com.example.foodieup.presentation.adapters.CourierOrderAdapter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CourierActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCourierBinding
    private lateinit var courierOrderAdapter: CourierOrderAdapter
    private lateinit var tokenManager: TokenManager
    private var currentOrders = mutableListOf<Order>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCourierBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        setupRecyclerView()
        fetchOrders()

        binding.logoutButton.setOnClickListener {
            lifecycleScope.launch {
                tokenManager.clearTokens()
                val intent = Intent(this@CourierActivity, LogInActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    private fun setupRecyclerView() {
        courierOrderAdapter = CourierOrderAdapter(
            currentOrders,
            onAcceptClick = { order ->
                updateOrderStatus(order, "delivered")
            },
            onDeclineClick = { order ->
                updateOrderStatus(order, "declined")
            }
        )
        binding.ordersRecyclerView.apply {
            adapter = courierOrderAdapter
            layoutManager = LinearLayoutManager(this@CourierActivity)
        }
    }

    private fun fetchOrders() {
        lifecycleScope.launch {
            binding.progressBar.isVisible = true
            val token = tokenManager.getAccessToken().first()
            if (token == null) {
                binding.progressBar.isVisible = false
                Toast.makeText(this@CourierActivity, "Authentication error", Toast.LENGTH_SHORT).show()
                return@launch
            }

            try {
                val authHeader = "Bearer $token"
                val response = RetrofitClient.apiService.getOrderHistory(authHeader)
                binding.progressBar.isVisible = false
                if (response.isSuccessful) {
                    val allOrders = response.body() ?: emptyList()
                    currentOrders.clear()
                    // Sort orders: pending first, then others
                    val sortedOrders = allOrders.sortedBy { it.status != "pending" }
                    currentOrders.addAll(sortedOrders)
                    courierOrderAdapter.notifyDataSetChanged()
                    binding.noOrdersTextView.isVisible = currentOrders.isEmpty()
                } else {
                    Toast.makeText(this@CourierActivity, "Failed to fetch orders: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.isVisible = false
                Toast.makeText(this@CourierActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateOrderStatus(order: Order, newStatus: String) {
        lifecycleScope.launch {
            val token = tokenManager.getAccessToken().first()
            if (token == null) {
                Toast.makeText(this@CourierActivity, "Authentication error", Toast.LENGTH_SHORT).show()
                return@launch
            }
            try {
                val authHeader = "Bearer $token"
                val request = UpdateOrderStatusRequest(status = newStatus)
                val response = RetrofitClient.apiService.updateOrderStatus(authHeader, order.id, request)

                if (response.isSuccessful) {
                    Toast.makeText(this@CourierActivity, "Order status updated to ${newStatus}", Toast.LENGTH_SHORT).show()
                    // Re-fetch all orders to get the correct sorted list
                    fetchOrders()
                } else {
                    Toast.makeText(this@CourierActivity, "Failed to update status: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CourierActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
