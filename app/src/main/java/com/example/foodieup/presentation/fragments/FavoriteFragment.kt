package com.example.foodieup.presentation.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodieup.R
import com.example.foodieup.data.network.RetrofitClient
import com.example.foodieup.data.storage.TokenManager
import com.example.foodieup.data.storage.UserManager
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
        activity?.window?.let { window ->
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController?.isAppearanceLightStatusBars = true
            window.statusBarColor = Color.WHITE
            window.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.orange)
        }
    }

    override fun onResume() {
        super.onResume()
        displayFavorites()
        fetchFavoriteRestaurants()
    }

    private fun setupRecyclerView() {
        favoriteAdapter = FavoriteRestaurantAdapter(emptyList())
        binding.favoriteRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.favoriteRecyclerView.adapter = favoriteAdapter
    }

    private fun displayFavorites() {
        UserManager.favoriteRestaurants?.let {
            if (it.isNotEmpty()) {
                favoriteAdapter.updateRestaurants(it)
                binding.favoriteRecyclerView.isVisible = true
                binding.errorTextView.isVisible = false
            } else {
                binding.errorTextView.text = "Список избранного пуст"
                binding.errorTextView.isVisible = true
                binding.favoriteRecyclerView.isVisible = false
            }
        }
    }

    private fun fetchFavoriteRestaurants() {
        lifecycleScope.launch {
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
                    UserManager.favoriteRestaurants = favoriteRestaurants
                    displayFavorites()
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
