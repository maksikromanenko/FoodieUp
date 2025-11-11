package com.example.foodieup.presentation.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodieup.data.network.WebSocketService
import com.example.foodieup.data.network.RetrofitClient
import com.example.foodieup.data.storage.TokenManager
import com.example.foodieup.databinding.FragmentOrderHistoryBinding
import com.example.foodieup.presentation.adapters.OrderHistoryAdapter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class OrderHistoryFragment : Fragment() {

    private var _binding: FragmentOrderHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var tokenManager: TokenManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderHistoryBinding.inflate(inflater, container, false)
        tokenManager = TokenManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            WebSocketService.orderStatusUpdates.collect { statusUpdate ->
                Log.d("OrderHistoryFragment", "Получено обновление статуса: $statusUpdate. Обновляю список заказов...")
                fetchOrderHistory()
            }
        }

        fetchOrderHistory()
    }

    private fun fetchOrderHistory() {
        lifecycleScope.launch {
            binding.orderHistoryRecyclerView.isVisible = false
            binding.emptyText.isVisible = false

            val accessToken = tokenManager.getAccessToken().first()
            if (accessToken == null) {
                Toast.makeText(context, "Ошибка аутентификации", Toast.LENGTH_SHORT).show()
                binding.emptyText.text = "Не удалось загрузить историю"
                binding.emptyText.isVisible = true
                return@launch
            }

            try {
                val authHeader = "Bearer $accessToken"
                val response = RetrofitClient.apiService.getOrderHistory(authHeader)

                if (response.isSuccessful) {
                    val orders = response.body()
                    if (!orders.isNullOrEmpty()) {
                        binding.orderHistoryRecyclerView.isVisible = true
                        binding.orderHistoryRecyclerView.layoutManager = LinearLayoutManager(context)
                        binding.orderHistoryRecyclerView.adapter = OrderHistoryAdapter(orders)
                    } else {
                        binding.emptyText.isVisible = true
                        binding.emptyText.text = "История заказов пуста"
                    }
                } else {
                    binding.emptyText.isVisible = true
                    binding.emptyText.text = "Ошибка: ${response.code()}"
                    Toast.makeText(context, "Ошибка загрузки: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.emptyText.isVisible = true
                binding.emptyText.text = "Сетевая ошибка"
                Toast.makeText(context, "Сетевая ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
