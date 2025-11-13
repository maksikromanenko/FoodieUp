package com.example.foodieup.presentation.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodieup.R
import com.example.foodieup.data.model.Address
import com.example.foodieup.data.storage.UserManager
import com.example.foodieup.databinding.ItemAddressBinding

class AddressAdapter(
    private val items: List<Address>
) : RecyclerView.Adapter<AddressAdapter.AddressViewHolder>() {

    private var selectedPosition = items.indexOf(UserManager.userAddress?.firstOrNull())

    fun getSelectedAddress(): Address? {
        return if (selectedPosition != -1 && selectedPosition < items.size) {
            items[selectedPosition]
        } else {
            null
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val binding = ItemAddressBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AddressViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AddressViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val item = items[position]
        holder.bind(item, position == selectedPosition)

        holder.itemView.setOnClickListener {
            if (selectedPosition != position) {
                val oldPosition = selectedPosition
                selectedPosition = position
                notifyItemChanged(oldPosition)
                notifyItemChanged(selectedPosition)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    class AddressViewHolder(private val binding: ItemAddressBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(address: Address, isSelected: Boolean) {
            binding.addressText.text = "${address.city}, ${address.location}"
            if (isSelected) {
                binding.addressIcon.setImageResource(R.drawable.ic_checked_circle)
            } else {
                binding.addressIcon.setImageResource(R.drawable.ic_unchecked_circle)
            }
        }
    }
}
