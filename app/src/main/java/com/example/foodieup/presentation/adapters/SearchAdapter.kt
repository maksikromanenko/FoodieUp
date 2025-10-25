package com.example.foodieup.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodieup.R
import com.example.foodieup.data.model.Restaurant
import com.example.foodieup.databinding.SearchItemBinding

class SearchAdapter(
    private var restaurants: List<Restaurant>,
    private val onItemClick: (Restaurant) -> Unit
) : RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val binding = SearchItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(restaurants[position])
    }

    override fun getItemCount(): Int = restaurants.size

    fun updateRestaurants(newRestaurants: List<Restaurant>) {
        restaurants = newRestaurants
        notifyDataSetChanged()
    }

    inner class SearchViewHolder(private val binding: SearchItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                onItemClick(restaurants[adapterPosition])
            }
        }

        fun bind(restaurant: Restaurant) {
            binding.restaurantName.text = restaurant.name
            binding.restaurantRating.text = restaurant.rating?.toString() ?: "N/A"

            if (!restaurant.logoUrl.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(restaurant.logoUrl)
                    .into(binding.restaurantLogo)
            } else {
                binding.restaurantLogo.setImageResource(R.drawable.background_autori)
            }
        }
    }
}
