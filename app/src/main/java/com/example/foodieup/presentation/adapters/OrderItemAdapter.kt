package com.example.foodieup.presentation.adapters

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodieup.R
import com.example.foodieup.data.model.MenuItem
import com.example.foodieup.databinding.OrderItemBinding

class OrderItemAdapter(
    private var menuItems: List<MenuItem>,
    private val onQuantityChanged: (MenuItem, Int) -> Unit
) : RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderItemViewHolder {
        val binding = OrderItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderItemViewHolder, position: Int) {
        val menuItem = menuItems[position]
        holder.bind(menuItem)
    }

    override fun getItemCount() = menuItems.size

    fun updateItems(newItems: List<MenuItem>) {
        menuItems = newItems
        notifyDataSetChanged()
    }

    inner class OrderItemViewHolder(private val binding: OrderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(menuItem: MenuItem) {
            binding.itemName.text = menuItem.name

            if (menuItem.finalPrice != null && menuItem.price != menuItem.finalPrice) {

                binding.itemOriginalPrice.visibility = View.VISIBLE
                binding.itemOriginalPrice.text = "${menuItem.price} BYN"
                binding.itemOriginalPrice.paintFlags = binding.itemOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

                binding.itemPrice.text = "${menuItem.finalPrice} BYN"
            } else {
                binding.itemOriginalPrice.visibility = View.GONE
                binding.itemPrice.text = "${menuItem.price} BYN"
            }

            if (menuItem.isNew == true) {
                binding.newTag.visibility = View.VISIBLE
            } else {
                binding.newTag.visibility = View.GONE
            }

            if (menuItem.discountPercentage != null && menuItem.discountPercentage > 0) {
                binding.discountTag.visibility = View.VISIBLE
                binding.discountTag.text = "-${menuItem.discountPercentage}%"
            } else {
                binding.discountTag.visibility = View.GONE
            }

            binding.quantityTextView.text = "0"

            Glide.with(binding.root.context)
                .load(menuItem.logoUrl)
                .centerCrop()
                .error(R.drawable.background_autori)
                .into(binding.itemLogo)

            var quantity = 0

            binding.minusButton.setOnClickListener {
                if (quantity > 0) {
                    quantity--
                    binding.quantityTextView.text = quantity.toString()
                    onQuantityChanged(menuItem, quantity)
                }
            }

            binding.plusButton.setOnClickListener {
                quantity++
                binding.quantityTextView.text = quantity.toString()
                onQuantityChanged(menuItem, quantity)
            }
        }
    }
}
