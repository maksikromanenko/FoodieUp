package com.example.foodieup.presentation.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodieup.R
import com.example.foodieup.data.model.MenuItem
import com.example.foodieup.data.model.Restaurant
import com.example.foodieup.data.network.RetrofitClient
import com.example.foodieup.data.storage.RestaurantManager
import com.example.foodieup.data.storage.TokenManager
import com.example.foodieup.data.storage.UserManager
import com.example.foodieup.databinding.FragmentHomeBinding
import com.example.foodieup.presentation.adapters.MenuItemAdapter
import com.example.foodieup.presentation.adapters.RestaurantAdapter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var tokenManager: TokenManager

    companion object {
        private const val TAG = "HomeFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        tokenManager = TokenManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.window?.let { window ->
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.isAppearanceLightStatusBars = true
            window.statusBarColor = Color.WHITE
            window.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.orange)
        }

        setupToolbar()

        binding.toolbar.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_addressSelectionFragment)
        }

        binding.searchBar.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_nav_search)
        }

        val restaurants = RestaurantManager.restaurants

        if (!restaurants.isNullOrEmpty()) {

            val onRestaurantClick = { restaurant: Restaurant ->
                val bundle = bundleOf(
                    "restaurantId" to restaurant.id,
                    "restaurantName" to restaurant.name
                )
                findNavController().navigate(R.id.action_nav_home_to_createOrderFragment, bundle)
            }

            val popularRestaurants = restaurants.sortedByDescending { it.rating }.take(8)
            binding.popularRestaurantsRecycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            binding.popularRestaurantsRecycler.adapter = RestaurantAdapter(popularRestaurants, onRestaurantClick)
        } else {
            val dummyRestaurants = listOf(
                Restaurant(id = -1, name = "Элемент 1", description = "", location = "", logoUrl = null, rating = 4.8),
                Restaurant(id = -2, name = "Элемент 2", description = "", location = "", logoUrl = null, rating = 4.5),
            )

            val onDummyItemClick = { _: Restaurant -> }

            binding.popularRestaurantsRecycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            binding.popularRestaurantsRecycler.adapter = RestaurantAdapter(dummyRestaurants, onDummyItemClick)

        }
        loadNewItems()
        loadSaleItems()
    }

    private fun setupToolbar() {
        val currentUser = UserManager.currentUser
        val addresses = UserManager.userAddress

        val primaryAddressId = currentUser?.addressid?.toIntOrNull()

        val primaryAddress = addresses?.find { it.id == primaryAddressId }

        if (primaryAddress != null) {
            binding.toolbarTitle.text = primaryAddress.addressLine
        } else if (!addresses.isNullOrEmpty()) {
            val firstAddress = addresses[0]
            binding.toolbarTitle.text = "${firstAddress.city}, ${firstAddress.location}"
        } else {
            binding.toolbarTitle.text = "Нет адреса"
        }
    }

    private fun onMenuItemClick(menuItem: MenuItem) {
        val restaurantId = menuItem.restaurant
        val restaurant = RestaurantManager.restaurants?.find { it.id == restaurantId }

        if (restaurant != null) {
            val bundle = bundleOf(
                "restaurantId" to restaurantId,
                "restaurantName" to restaurant.name
            )
            findNavController().navigate(R.id.action_nav_home_to_createOrderFragment, bundle)
        } else {
            Toast.makeText(context, "Ресторан не найден", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadNewItems() {
        lifecycleScope.launch {
            try {
                val token = tokenManager.getAccessToken().firstOrNull()?.let { "Bearer $it" } ?: return@launch
                val response = RetrofitClient.apiService.getNewMenuItems(token)
                if (response.isSuccessful && response.body() != null) {
                    val newItems = response.body()!!
                    binding.newItemsRecycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    binding.newItemsRecycler.adapter = MenuItemAdapter(newItems) { menuItem ->
                        onMenuItemClick(menuItem)
                    }
                } else {
                    Log.e(TAG, "Error loading new items: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while loading new items", e)
            }
        }
    }

    private fun loadSaleItems() {
        lifecycleScope.launch {
            try {
                val token = tokenManager.getAccessToken().firstOrNull()?.let { "Bearer $it" } ?: return@launch
                val response = RetrofitClient.apiService.getSaleMenuItems(token)
                if (response.isSuccessful && response.body() != null) {
                    val saleItems = response.body()!!
                    binding.offersRecycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    binding.offersRecycler.adapter = MenuItemAdapter(saleItems) { menuItem ->
                        onMenuItemClick(menuItem)
                    }
                } else {
                    Log.e(TAG, "Error loading sale items: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while loading sale items", e)
            }
        }
    }


    override fun onResume() {
        super.onResume()
        setupToolbar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
