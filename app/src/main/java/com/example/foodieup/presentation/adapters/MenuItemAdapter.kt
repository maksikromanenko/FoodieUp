package com.example.foodieup.presentation.adapters

import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodieup.R
import com.example.foodieup.data.model.MenuItem
import com.example.foodieup.databinding.ItemMenuItemBinding

class MenuItemAdapter(
    private val menuItems: List<MenuItem>,
    private val onMenuItemClick: (MenuItem) -> Unit
) : RecyclerView.Adapter<MenuItemAdapter.MenuItemViewHolder>() {

    companion object {
        private const val TAG = "MenuItemAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuItemViewHolder {
        val binding = ItemMenuItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = MenuItemViewHolder(binding)
        holder.itemView.setOnClickListener {
            val position = holder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onMenuItemClick(menuItems[position])
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: MenuItemViewHolder, position: Int) {
        holder.bind(menuItems[position])
    }

    override fun getItemCount(): Int = menuItems.size

    inner class MenuItemViewHolder(private val binding: ItemMenuItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(menuItem: MenuItem) {
            try {
                binding.menuItemName.text = menuItem.name

                if (menuItem.finalPrice != null && menuItem.price != menuItem.finalPrice) {
                    binding.menuItemOriginalPrice.visibility = View.VISIBLE
                    binding.menuItemOriginalPrice.text = "${menuItem.price} BYN"
                    binding.menuItemOriginalPrice.paintFlags = binding.menuItemOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

                    binding.menuItemPrice.text = "${menuItem.finalPrice} BYN"
                } else {
                    binding.menuItemOriginalPrice.visibility = View.GONE
                    binding.menuItemPrice.text = "${menuItem.price} BYN"
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

                Glide.with(itemView.context)
                    .load(menuItem.logoUrl)
                    .error(R.drawable.background_autori)
                    .into(binding.menuItemImage)

            } catch (e: Exception) {
                Log.e(TAG, "Error binding menu item: ${menuItem.name}", e)
            }
        }
    }
}
