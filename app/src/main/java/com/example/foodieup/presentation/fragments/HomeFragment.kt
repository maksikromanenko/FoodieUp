package com.example.foodieup.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodieup.R
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

        setupToolbar()

        binding.toolbar.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_addressSelectionFragment)
        }

        binding.searchBar.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_nav_search)
        }

        val restaurants = RestaurantManager.restaurants

        if (!restaurants.isNullOrEmpty()) {

            binding.popularRestaurantsRecycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            binding.popularRestaurantsRecycler.adapter = RestaurantAdapter(restaurants)

            binding.offersRecycler.layoutManager = GridLayoutManager(context, 3)
            binding.offersRecycler.adapter = RestaurantAdapter(restaurants.take(3))

            binding.newItemsRecycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            binding.newItemsRecycler.adapter = RestaurantAdapter(restaurants.shuffled())
        } else {

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
