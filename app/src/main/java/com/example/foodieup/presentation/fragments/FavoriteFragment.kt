package com.example.foodieup.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodieup.data.network.RetrofitClient
import com.example.foodieup.data.storage.TokenManager
import com.example.foodieup.databinding.FragmentFavoriteBinding
import com.example.foodieup.presentation.adapters.FavoriteRestaurantAdapter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FavoriteFragment : Fragment() {

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!

    private lateinit var favoriteAdapter: FavoriteRestaurantAdapter
    private lateinit var tokenManager: TokenManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        tokenManager = TokenManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        fetchFavoriteRestaurants()
    }

    private fun setupRecyclerView() {
        favoriteAdapter = FavoriteRestaurantAdapter(emptyList())
        binding.favoriteRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.favoriteRecyclerView.adapter = favoriteAdapter
    }

    private fun fetchFavoriteRestaurants() {
        lifecycleScope.launch {
            binding.favoriteRecyclerView.isVisible = false
            binding.errorTextView.isVisible = false

            val accessToken = tokenManager.getAccessToken().first()
            if (accessToken == null) {
                Toast.makeText(context, "Ошибка аутентификации", Toast.LENGTH_SHORT).show()
                binding.errorTextView.text = "Не удалось загрузить избранное. Требуется вход."
                binding.errorTextView.isVisible = true
                return@launch
            }

            try {
                val authHeader = "Bearer $accessToken"
                val response = RetrofitClient.apiService.getFavoriteRestaurants(authHeader)
                if (response.isSuccessful) {
                    val favoriteRestaurants = response.body()
                    if (!favoriteRestaurants.isNullOrEmpty()) {
                        favoriteAdapter.updateRestaurants(favoriteRestaurants)
                        binding.favoriteRecyclerView.isVisible = true
                    } else {
                        binding.errorTextView.text = "Список избранного пуст"
                        binding.errorTextView.isVisible = true
                    }
                } else {
                    binding.errorTextView.text = "Ошибка: ${response.code()}"
                    binding.errorTextView.isVisible = true
                }
            } catch (e: Exception) {
                binding.errorTextView.text = "Не удалось загрузить данные. Проверьте подключение к сети."
                binding.errorTextView.isVisible = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
