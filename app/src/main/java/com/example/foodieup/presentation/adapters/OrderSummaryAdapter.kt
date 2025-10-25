package com.example.foodieup.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodieup.data.model.MenuItem
import com.example.foodieup.databinding.OrderSummaryItemBinding
import java.math.BigDecimal

class OrderSummaryAdapter(
    private val cart: Map<MenuItem, Int>
) : RecyclerView.Adapter<OrderSummaryAdapter.SummaryViewHolder>() {

    private val items = cart.keys.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SummaryViewHolder {
        val binding = OrderSummaryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SummaryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SummaryViewHolder, position: Int) {
        val menuItem = items[position]
        val quantity = cart[menuItem] ?: 0
        holder.bind(menuItem, quantity)
    }

    override fun getItemCount() = items.size

    class SummaryViewHolder(private val binding: OrderSummaryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(menuItem: MenuItem, quantity: Int) {
            binding.itemNameTextView.text = "${menuItem.name} x$quantity"
            val totalPrice = BigDecimal(menuItem.price) * BigDecimal(quantity)
            binding.itemPriceTextView.text = "${totalPrice.toPlainString()} BYN"
        }
    }
}
