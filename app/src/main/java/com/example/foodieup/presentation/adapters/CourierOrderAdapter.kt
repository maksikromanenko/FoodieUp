package com.example.foodieup.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.foodieup.data.model.Order
import com.example.foodieup.databinding.ItemCourierOrderBinding

class CourierOrderAdapter(
    private var orders: List<Order>,
    private val onAcceptClick: (Order) -> Unit,
    private val onDeclineClick: (Order) -> Unit
) : RecyclerView.Adapter<CourierOrderAdapter.CourierOrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourierOrderViewHolder {
        val binding = ItemCourierOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CourierOrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CourierOrderViewHolder, position: Int) {
        val order = orders[position]
        holder.bind(order)
    }

    override fun getItemCount(): Int = orders.size

    fun updateOrders(newOrders: List<Order>) {
        this.orders = newOrders
        notifyDataSetChanged()
    }

    inner class CourierOrderViewHolder(private val binding: ItemCourierOrderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(order: Order) {
            binding.restaurantNameTextView.text = order.restaurantName
            binding.totalPriceTextView.text = "${order.totalPrice} BYN"

            binding.statusTextView.text = getStatusText(order.status)

            val isPending = order.status == "pending"
            binding.actionButtonsLayout.isVisible = isPending

            binding.acceptButton.setOnClickListener {
                onAcceptClick(order)
            }
            binding.declineButton.setOnClickListener {
                onDeclineClick(order)
            }
        }

        private fun getStatusText(status: String?): String {
            return when (status) {
                "pending" -> "Ожидает подтверждения"
                "cancelled" -> "Отменён"
                "delivered" -> "Доставлен"
                else -> "Неизвестный статус"
            }
        }
    }
}
