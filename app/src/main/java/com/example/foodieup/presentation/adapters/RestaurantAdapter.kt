package com.example.foodieup.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodieup.R
import com.example.foodieup.data.model.Restaurant
import com.example.foodieup.databinding.ItemPopularRestaurantBinding

class RestaurantAdapter(
    private val items: List<Restaurant>,
    private val onItemClick: (Restaurant) -> Unit
) : RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val binding = ItemPopularRestaurantBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = RestaurantViewHolder(binding)

        holder.itemView.setOnClickListener {
            val position = holder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onItemClick(items[position])
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class RestaurantViewHolder(private val binding: ItemPopularRestaurantBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(restaurant: Restaurant) {
            binding.restaurantName.text = restaurant.name
            binding.restaurantRating.visibility = View.VISIBLE
            binding.restaurantRating.text = restaurant.rating.toString()

            if (!restaurant.logoUrl.isNullOrEmpty()) {
                Glide.with(binding.restaurantImage.context)
                    .load(restaurant.logoUrl)
                    .placeholder(R.drawable.background_autori)
                    .error(R.drawable.background_autori)
                    .into(binding.restaurantImage)
            } else {
                binding.restaurantImage.setImageResource(R.drawable.background_autori)
            }
        }
    }
}
