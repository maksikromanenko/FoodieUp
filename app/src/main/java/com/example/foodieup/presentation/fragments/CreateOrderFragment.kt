package com.example.foodieup.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.example.foodieup.R
import com.example.foodieup.data.model.AddFavoriteRequest
import com.example.foodieup.data.model.MenuItem
import com.example.foodieup.data.network.RetrofitClient
import com.example.foodieup.data.storage.TokenManager
import com.example.foodieup.data.storage.UserManager
import com.example.foodieup.databinding.FragmentCreateOrderBinding
import com.example.foodieup.presentation.adapters.OrderItemAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.math.BigDecimal

class CreateOrderFragment : Fragment() {

    private var _binding: FragmentCreateOrderBinding? = null
    private val binding get() = _binding!!

    private val args: CreateOrderFragmentArgs by navArgs()

    private lateinit var orderItemAdapter: OrderItemAdapter
    private lateinit var tokenManager: TokenManager

    private val cart = mutableMapOf<MenuItem, Int>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateOrderBinding.inflate(inflater, container, false)
        tokenManager = TokenManager(requireContext().applicationContext)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)?.visibility = View.GONE

        binding.restaurantName.text = args.restaurantName

        checkIfFavorite()
        setupRecyclerView()

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.favoriteButton.setOnClickListener {
            toggleFavoriteStatus()
        }

        binding.orderButton.setOnClickListener {
            if (cart.isNotEmpty()) {
                OrderSummaryBottomSheet.newInstance(cart, args.restaurantId)
                    .show(childFragmentManager, "OrderSummaryBottomSheet")
            }
        }

        updateOrderButton()
        fetchMenuItems(args.restaurantId)
    }

    private fun checkIfFavorite() {
        UserManager.favoriteRestaurants?.let { favorites ->
            val isFavorite = favorites.any { it.id == args.restaurantId }
            binding.favoriteButton.isSelected = isFavorite
        }
    }

    private fun toggleFavoriteStatus() {
        lifecycleScope.launch {
            val token = tokenManager.getAccessToken().first()
            if (token == null) {
                Toast.makeText(context, "Требуется вход", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val isFavorite = binding.favoriteButton.isSelected
            val restaurantId = args.restaurantId

            try {
                val authHeader = "Bearer $token"
                if (isFavorite) {
                    val response = RetrofitClient.apiService.removeFromFavorites(authHeader, restaurantId)
                    if (response.isSuccessful) {
                        binding.favoriteButton.isSelected = false
                        UserManager.favoriteRestaurants = UserManager.favoriteRestaurants?.filterNot { it.id == restaurantId }
                    }
                } else {
                    val request = AddFavoriteRequest(restaurantId)
                    val response = RetrofitClient.apiService.addToFavorites(authHeader, request)
                    if (response.isSuccessful) {
                        binding.favoriteButton.isSelected = true
                        response.body()?.let {
                            UserManager.favoriteRestaurants = UserManager.favoriteRestaurants?.plus(it)
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Сетевая ошибка", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchMenuItems(restaurantId: Int) {
        lifecycleScope.launch {
            val token = tokenManager.getAccessToken().first()
            if (token != null) {
                try {
                    val response = RetrofitClient.apiService.getMenuItems("Bearer $token", restaurantId)
                    if (response.isSuccessful) {
                        response.body()?.let {
                            orderItemAdapter.updateItems(it)
                        }
                    }
                } catch (e: Exception) {

                }
            }
        }
    }

    private fun setupRecyclerView() {
        orderItemAdapter = OrderItemAdapter(emptyList()) { menuItem, quantity ->
            if (quantity > 0) {
                cart[menuItem] = quantity
            } else {
                cart.remove(menuItem)
            }
            updateOrderButton()
        }
        binding.menuItemsRecyclerView.apply {
            adapter = orderItemAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
        }
    }

    private fun updateOrderButton() {
        val total = cart.entries.fold(BigDecimal.ZERO) { acc, entry ->
            val item = entry.key
            val quantity = entry.value
            val price = BigDecimal(item.price)
            acc + price * BigDecimal(quantity)
        }

        binding.orderButton.text = "Заказать - ${total.toPlainString()} BYN"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)?.visibility = View.VISIBLE
        _binding = null
    }
}
