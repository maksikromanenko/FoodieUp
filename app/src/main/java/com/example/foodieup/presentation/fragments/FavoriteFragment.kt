package com.example.foodieup.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
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
        tokenManager = TokenManager(requireContext().applicationContext)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        fetchFavoriteRestaurants()
    }

    private fun setupRecyclerView() {
        favoriteAdapter = FavoriteRestaurantAdapter(emptyList())
        binding.favoriteRecyclerView.adapter = favoriteAdapter
    }

    private fun fetchFavoriteRestaurants() {
        lifecycleScope.launch {
            val token = tokenManager.getAccessToken().first()
            if (token != null) {
                try {
                    val response = RetrofitClient.apiService.getFavoriteRestaurants("Bearer $token")
                    if (response.isSuccessful) {
                        response.body()?.let {
                            favoriteAdapter.updateRestaurants(it)
                        }
                    }
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
