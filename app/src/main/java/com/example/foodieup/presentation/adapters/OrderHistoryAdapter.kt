package com.example.foodieup.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodieup.R
import com.example.foodieup.data.model.Order
import com.example.foodieup.databinding.ItemOrderHistoryBinding

class OrderHistoryAdapter(private val items: List<Order>) : RecyclerView.Adapter<OrderHistoryAdapter.OrderHistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderHistoryViewHolder {
        val binding = ItemOrderHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderHistoryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class OrderHistoryViewHolder(private val binding: ItemOrderHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(order: Order) {
            binding.restaurantNameText.text = order.restaurantName
            binding.totalPriceText.text = "Total: ${order.totalPrice}BYN"

            binding.statusText.text = if (!order.status.isNullOrBlank()) {
                "Status: ${order.status}"
            } else {
                "Status: N/A"
            }

            binding.ratingText.text = if (order.rating != null) {
                "Rating: ${order.rating}"
            } else {
                "Rating: Not rated yet"
            }

            if (!order.logoUrl.isNullOrEmpty()) {
                Glide.with(binding.restaurantLogoImage.context)
                    .load(order.logoUrl)
                    .placeholder(R.drawable.background_autori)
                    .error(R.drawable.background_autori)
                    .into(binding.restaurantLogoImage)
            } else {
                binding.restaurantLogoImage.setImageResource(R.drawable.background_autori)
            }
        }
    }
}
