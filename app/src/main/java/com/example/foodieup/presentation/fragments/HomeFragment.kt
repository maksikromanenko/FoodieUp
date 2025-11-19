package com.example.foodieup.presentation.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodieup.R
import com.example.foodieup.data.model.Restaurant
import com.example.foodieup.data.storage.RestaurantManager
import com.example.foodieup.data.storage.UserManager
import com.example.foodieup.databinding.FragmentHomeBinding
import com.example.foodieup.presentation.adapters.RestaurantAdapter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.window?.let { window ->
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController?.isAppearanceLightStatusBars = true
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

            binding.offersRecycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            binding.offersRecycler.adapter = RestaurantAdapter(restaurants.shuffled().take(3), onRestaurantClick)

            binding.newItemsRecycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            binding.newItemsRecycler.adapter = RestaurantAdapter(restaurants.shuffled(), onRestaurantClick)
        } else {
            val dummyRestaurants = listOf(
                Restaurant(id = -1, name = "Элемент 1", description = "", location = "", logoUrl = null, rating = 4.8),
                Restaurant(id = -2, name = "Элемент 2", description = "", location = "", logoUrl = null, rating = 4.5),
                Restaurant(id = -3, name = "Элемент 3", description = "", location = "", logoUrl = null, rating = 4.2),
                Restaurant(id = -4, name = "Элемент 4", description = "", location = "", logoUrl = null, rating = 4.9),
                Restaurant(id = -5, name = "Элемент 5", description = "", location = "", logoUrl = null, rating = 4.7)
            )

            val onDummyItemClick = { _: Restaurant -> }

            binding.popularRestaurantsRecycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            binding.popularRestaurantsRecycler.adapter = RestaurantAdapter(dummyRestaurants, onDummyItemClick)

            binding.offersRecycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            binding.offersRecycler.adapter = RestaurantAdapter(dummyRestaurants.take(3), onDummyItemClick)

            binding.newItemsRecycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            binding.newItemsRecycler.adapter = RestaurantAdapter(dummyRestaurants, onDummyItemClick)
        }
    }

    private fun setupToolbar() {
        val currentUser = UserManager.currentUser
        val addresses = UserManager.userAddress

        val primaryAddressId = currentUser?.addressid?.toIntOrNull()

        val primaryAddress = addresses?.find { it.id == primaryAddressId }

        if (primaryAddress != null) {
            binding.toolbarTitle.text = "${primaryAddress.addressLine}"
        } else if (!addresses.isNullOrEmpty()) {
            val firstAddress = addresses[0]
            binding.toolbarTitle.text = "${firstAddress.city}, ${firstAddress.location}"
        } else {
            binding.toolbarTitle.text = "Нет адреса"
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
