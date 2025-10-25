package com.example.foodieup.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.foodieup.R
import com.example.foodieup.data.model.CreateOrderRequest
import com.example.foodieup.data.model.MenuItem
import com.example.foodieup.data.model.OrderItem
import com.example.foodieup.data.network.RetrofitClient
import com.example.foodieup.data.storage.TokenManager
import com.example.foodieup.databinding.BottomSheetOrderSummaryBinding
import com.example.foodieup.presentation.adapters.OrderSummaryAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.math.BigDecimal

class OrderSummaryBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetOrderSummaryBinding? = null
    private val binding get() = _binding!!

    private lateinit var cart: Map<MenuItem, Int>
    private var restaurantId: Int = -1
    private lateinit var tokenManager: TokenManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetOrderSummaryBinding.inflate(inflater, container, false)
        tokenManager = TokenManager(requireContext().applicationContext)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.summaryRecyclerView.adapter = OrderSummaryAdapter(cart)

        val total = cart.entries.fold(BigDecimal.ZERO) { acc, entry ->
            val item = entry.key
            val quantity = entry.value
            val price = BigDecimal(item.price)
            acc + price * BigDecimal(quantity)
        }

        binding.payButton.text = "ОПЛАТИТЬ - ${total.toPlainString()} BYN"

        binding.payButton.setOnClickListener {
            createOrder()
        }
    }

    private fun createOrder() {
        lifecycleScope.launch {
            val token = tokenManager.getAccessToken().first()
            if (token == null) {
                Toast.makeText(requireContext(), "Authentication error", Toast.LENGTH_SHORT).show()
                return@launch
            }

            try {
                val profileResponse = RetrofitClient.apiService.getProfile("Bearer $token")
                if (!profileResponse.isSuccessful) {
                    Toast.makeText(requireContext(), "Failed to get user profile", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val addressId = profileResponse.body()?.addressid?.toIntOrNull()
                if (addressId == null) {
                    Toast.makeText(requireContext(), "No address selected", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val orderItems = cart.map { (menuItem, quantity) ->
                    OrderItem(menuItem.id, quantity)
                }

                val orderRequest = CreateOrderRequest(
                    restaurantId = restaurantId,
                    address = addressId,
                    items = orderItems
                )

                val createOrderResponse = RetrofitClient.apiService.createOrder("Bearer $token", orderRequest)

                if (createOrderResponse.isSuccessful) {
                    Toast.makeText(requireContext(), "Order created successfully!", Toast.LENGTH_LONG).show()
                    findNavController().navigate(
                        R.id.nav_home,
                        null,
                        NavOptions.Builder()
                            .setPopUpTo(R.id.nav_graph, true)
                            .build()
                    )
                } else {
                    Toast.makeText(requireContext(), "Failed to create order", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(cart: Map<MenuItem, Int>, restaurantId: Int): OrderSummaryBottomSheet {
            return OrderSummaryBottomSheet().apply {
                this.cart = cart
                this.restaurantId = restaurantId
            }
        }
    }
}
