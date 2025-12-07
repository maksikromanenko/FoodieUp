package com.example.foodieup.presentation.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodieup.R
import com.example.foodieup.data.model.ChangeAddressRequest
import com.example.foodieup.data.network.RetrofitClient
import com.example.foodieup.data.storage.TokenManager
import com.example.foodieup.data.storage.UserManager
import com.example.foodieup.databinding.FragmentAddressSelectionBinding
import com.example.foodieup.presentation.adapters.AddressAdapter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AddressSelectionFragment : Fragment() {

    private var _binding: FragmentAddressSelectionBinding? = null
    private val binding get() = _binding!!

    private lateinit var addressAdapter: AddressAdapter
    private lateinit var tokenManager: TokenManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddressSelectionBinding.inflate(inflater, container, false)
        tokenManager = TokenManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        setupAddressList()

        binding.saveButton.setOnClickListener {
            if (!::addressAdapter.isInitialized) {
                findNavController().popBackStack()
                return@setOnClickListener
            }

            val selectedAddress = addressAdapter.getSelectedAddress()
            if (selectedAddress != null) {
                lifecycleScope.launch {
                    val accessToken = tokenManager.getAccessToken().first()
                    if (accessToken == null) {
                        Toast.makeText(context, "Ошибка аутентификации", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                        return@launch
                    }

                    try {
                        val authHeader = "Bearer $accessToken"
                        val request = ChangeAddressRequest(newAddressId = selectedAddress.id)
                        val response = RetrofitClient.apiService.changeAddress(authHeader, request)

                        if (response.isSuccessful) {

                            UserManager.currentUser = UserManager.currentUser?.copy(addressid = selectedAddress.id.toString())

                            Toast.makeText(context, "Основной адрес изменен", Toast.LENGTH_SHORT).show()
                            findNavController().popBackStack()
                        } else {
                            Toast.makeText(context, "Ошибка: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Сетевая ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {

                findNavController().popBackStack()
            }
        }

        binding.addNewAddressButton.setOnClickListener {
            showAddNewAddressDialog()
        }
    }

    private fun setupAddressList() {
        val addresses = UserManager.userAddress

        if (!addresses.isNullOrEmpty()) {
            addressAdapter = AddressAdapter(addresses)
            binding.addressesRecyclerView.layoutManager = LinearLayoutManager(context)
            binding.addressesRecyclerView.adapter = addressAdapter
        } else {
            Toast.makeText(context, "Список адресов пуст", Toast.LENGTH_LONG).show()
        }
    }

    private fun showAddNewAddressDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_add_new_address, null)
        builder.setView(dialogView)

        val addressEditText = dialogView.findViewById<EditText>(R.id.addressEditText)
        val cityEditText = dialogView.findViewById<EditText>(R.id.cityEditText)
        val postalCodeEditText = dialogView.findViewById<EditText>(R.id.postalCodeEditText)
        val saveAddressButton = dialogView.findViewById<Button>(R.id.saveAddressButton)

        val dialog = builder.create()

        saveAddressButton.setOnClickListener {
            val address = addressEditText.text.toString()
            val city = cityEditText.text.toString()
            val postalCode = postalCodeEditText.text.toString()

            if (address.isNotEmpty() && city.isNotEmpty() && postalCode.isNotEmpty()) {
                // TODO: Implement add new address API call
                Toast.makeText(context, "Новый адрес сохранен (заглушка)", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                setupAddressList() 
            } else {
                Toast.makeText(context, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
