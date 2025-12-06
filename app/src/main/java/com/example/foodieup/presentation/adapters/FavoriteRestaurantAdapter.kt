package com.example.foodieup.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodieup.data.model.FavoriteRestaurant
import com.example.foodieup.databinding.FavoriteItemBinding

class FavoriteRestaurantAdapter(
    private var restaurants: List<FavoriteRestaurant>,
    private val onItemClick: (FavoriteRestaurant) -> Unit
) : RecyclerView.Adapter<FavoriteRestaurantAdapter.FavoriteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding = FavoriteItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = FavoriteViewHolder(binding)
        holder.itemView.setOnClickListener {
            val position = holder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onItemClick(restaurants[position])
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(restaurants[position])
    }

    override fun getItemCount() = restaurants.size

    fun updateRestaurants(newRestaurants: List<FavoriteRestaurant>) {
        restaurants = newRestaurants
        notifyDataSetChanged()
    }

    class FavoriteViewHolder(private val binding: FavoriteItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(restaurant: FavoriteRestaurant) {
            binding.restaurantName.text = restaurant.restaurantName
            binding.restaurantDescription.text = restaurant.description

            Glide.with(binding.root.context)
                .load(restaurant.restaurantLogo)
                .into(binding.restaurantLogo)
        }
    }
}
