package com.example.foodieup.presentation.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodieup.R
import com.example.foodieup.data.model.Restaurant
import com.example.foodieup.data.storage.RestaurantManager
import com.example.foodieup.databinding.FragmentSearchBinding
import com.example.foodieup.presentation.adapters.SearchAdapter

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var searchAdapter: SearchAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.searchEditText.requestFocus()
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.searchEditText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun setupRecyclerView() {
        searchAdapter = SearchAdapter(emptyList()) { restaurant ->
            val bundle = bundleOf(
                "restaurantName" to restaurant.name,
                "restaurantId" to restaurant.id
            )
            findNavController().navigate(R.id.action_nav_search_to_createOrderFragment, bundle)
        }
        binding.searchResultsRecycler.apply {
            adapter = searchAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                if (query.isNotEmpty()) {
                    val allRestaurants = RestaurantManager.restaurants ?: emptyList()
                    val filteredList = allRestaurants.filter {
                        it.name.contains(query, ignoreCase = true)
                    }
                    searchAdapter.updateRestaurants(filteredList)
                } else {
                    searchAdapter.updateRestaurants(emptyList())
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
