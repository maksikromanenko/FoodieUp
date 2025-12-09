package com.example.foodieup.presentation.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.foodieup.data.model.UpdateUserRequest
import com.example.foodieup.data.model.User
import com.example.foodieup.data.network.ApiService
import com.example.foodieup.data.network.RetrofitClient
import com.example.foodieup.data.storage.TokenManager
import com.example.foodieup.databinding.FragmentProfileBinding
import com.example.foodieup.presentation.LogInActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var tokenManager: TokenManager
    private lateinit var apiService: ApiService

    private var isEditMode = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        tokenManager = TokenManager(requireContext())
        apiService = RetrofitClient.apiService
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadUserProfile()

        binding.logoutButton.setOnClickListener {
            lifecycleScope.launch {
                tokenManager.clearTokens()
                val intent = Intent(requireContext(), LogInActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
            }
        }

        binding.editSaveButton.setOnClickListener {
            isEditMode = !isEditMode
            if (isEditMode) {
                enableEditMode()
            } else {
                saveUserProfile()
            }
        }
    }

    private fun loadUserProfile() {
        lifecycleScope.launch {
            val token = tokenManager.getAccessToken().first()
            if (token != null) {
                try {
                    val response = apiService.getProfile("Bearer $token")
                    if (response.isSuccessful) {
                        response.body()?.let { setupProfileData(it) }
                    } else {
                        Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupProfileData(user: User) {
        binding.firstNameEditText.setText(user.firstName)
        binding.lastNameEditText.setText(user.lastName)
        binding.emailEditText.setText(user.email)

        if (!user.firstName.isNullOrEmpty()) {
            binding.firstNameEditText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
        }
        if (!user.lastName.isNullOrEmpty()) {
            binding.lastNameEditText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
        }
        if (!user.email.isNullOrEmpty()) {
            binding.emailEditText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
        }
    }

    private fun enableEditMode() {
        binding.firstNameEditText.isEnabled = true
        binding.lastNameEditText.isEnabled = true
        binding.emailEditText.isEnabled = true
        binding.editSaveButton.text = "Сохранить"
    }

    private fun saveUserProfile() {
        lifecycleScope.launch {
            val token = tokenManager.getAccessToken().first()
            if (token != null) {
                val firstName = binding.firstNameEditText.text.toString()
                val lastName = binding.lastNameEditText.text.toString()
                val email = binding.emailEditText.text.toString()
                val request = UpdateUserRequest(firstName, lastName, email)
                try {
                    val response = apiService.updateProfile("Bearer $token", request)
                    if (response.isSuccessful) {
                        disableEditMode()
                        Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun disableEditMode() {
        binding.firstNameEditText.isEnabled = false
        binding.lastNameEditText.isEnabled = false
        binding.emailEditText.isEnabled = false
        binding.editSaveButton.text = "Изменить"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
