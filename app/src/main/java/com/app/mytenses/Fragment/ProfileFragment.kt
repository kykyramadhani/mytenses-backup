package com.app.mytenses.Fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.mytenses.CompletedLessonAdapter
import com.app.mytenses.R
import com.app.mytenses.databinding.FragmentProfileBinding
import com.app.mytenses.network.RetrofitClient
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private var isEditMode = false
    private var shouldEditAfterLoad = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireContext().getSharedPreferences("MyTensesPrefs", Context.MODE_PRIVATE)
        val fullName = sharedPreferences.getString("name", "Nama Pengguna")
        val userId = sharedPreferences.getInt("user_id", -1)
        val username = sharedPreferences.getString("username", "user_$userId")
        Log.d(TAG, "User ID: $userId, Username: $username")

        binding.profileName.text = fullName
        binding.rvCompletedLesson.layoutManager = LinearLayoutManager(context)
        binding.rvCompletedLesson.adapter = CompletedLessonAdapter(emptyList())

        // Fitur masuk edit otomatis
        shouldEditAfterLoad = arguments?.getBoolean("edit_mode", false) == true

        if (!username.isNullOrBlank()) {
            fetchUserData(username)
        } else {
            Log.e(TAG, "Invalid username: $username")
            binding.profileBio.text = "Error: Username tidak valid"
        }

        binding.settingsButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SettingFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.editButton.setOnClickListener {
            toggleEditMode(fullName, username, true)
        }

        binding.cancelButton.setOnClickListener {
            toggleEditMode(fullName, username, false)
        }

        binding.saveButton.setOnClickListener {
            if (!username.isNullOrBlank()) {
                saveProfileChanges(username)
            } else {
                binding.profileBio.text = "Gagal menyimpan: Username tidak valid"
                Log.e(TAG, "Username is invalid: $username")
            }
        }
    }

    private fun fetchUserData(username: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getUserData(username)
                if (response.isSuccessful) {
                    val userData = response.body()
                    if (userData != null) {
                        val name = userData.name?.takeIf { it.isNotBlank() } ?: "Nama Pengguna"
                        val bio = userData.bio?.takeIf { it.isNotBlank() } ?: "Bio tidak tersedia"

                        binding.profileName.text = name
                        binding.profileBio.text = bio

                        requireContext().getSharedPreferences("MyTensesPrefs", Context.MODE_PRIVATE)
                            .edit()
                            .putString("name", userData.name)
                            .apply()

                        val completedLessons = userData.completed_lessons ?: emptyList()
                        if (completedLessons.isEmpty()) {
                            binding.emptyLessonsText.visibility = View.VISIBLE
                            binding.rvCompletedLesson.visibility = View.GONE
                        } else {
                            binding.emptyLessonsText.visibility = View.GONE
                            binding.rvCompletedLesson.visibility = View.VISIBLE
                        }
                        binding.rvCompletedLesson.adapter = CompletedLessonAdapter(completedLessons)

                        // Masuk edit mode jika diminta setelah load
                        if (shouldEditAfterLoad) {
                            toggleEditMode(name, username, true)
                            shouldEditAfterLoad = false
                        }
                    } else {
                        showError("Gagal memuat data: Data kosong")
                        Log.e(TAG, "User data is null")
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    showError("Gagal memuat data: $errorBody")
                    Log.e(TAG, "API error: ${response.code()} - $errorBody")
                }
            } catch (e: Exception) {
                showError("Gagal memuat data: ${e.message}")
                Log.e(TAG, "Network error: ${e.message}", e)
            }
        }
    }

    private fun showError(message: String) {
        binding.profileBio.text = message
        binding.emptyLessonsText.visibility = View.VISIBLE
        binding.rvCompletedLesson.visibility = View.GONE
        binding.rvCompletedLesson.adapter = CompletedLessonAdapter(emptyList())
    }

    private fun toggleEditMode(currentName: String?, username: String?, enterEditMode: Boolean) {
        isEditMode = enterEditMode
        Log.d(TAG, "Toggle edit mode: isEditMode=$isEditMode")

        if (isEditMode) {
            binding.profileName.visibility = View.GONE
            binding.profileBio.visibility = View.GONE
            binding.profileNameEdit.visibility = View.VISIBLE
            binding.profileBioEdit.visibility = View.VISIBLE
            binding.saveButton.visibility = View.VISIBLE
            binding.editButton.visibility = View.GONE
            binding.cancelButton.visibility = View.VISIBLE

            binding.profileNameEdit.setText(currentName?.takeIf { it != "Nama Pengguna" } ?: "")
            binding.profileBioEdit.setText(binding.profileBio.text.takeIf { it != "Bio tidak tersedia" } ?: "")
        } else {
            binding.profileName.visibility = View.VISIBLE
            binding.profileBio.visibility = View.VISIBLE
            binding.profileNameEdit.visibility = View.GONE
            binding.profileBioEdit.visibility = View.GONE
            binding.saveButton.visibility = View.GONE
            binding.editButton.visibility = View.VISIBLE
            binding.cancelButton.visibility = View.GONE

            if (!username.isNullOrBlank()) {
                fetchUserData(username)
            }
        }
    }

    private fun saveProfileChanges(username: String) {
        val newName = binding.profileNameEdit.text.toString().trim()
        val newBio = binding.profileBioEdit.text.toString().trim()
        Log.d(TAG, "Saving changes: username=$username, name=$newName, bio=$newBio")

        val updateData = mutableMapOf("name" to newName, "bio" to newBio)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.updateUserData(username, updateData)
                Log.d(TAG, "API response code: ${response.code()}")
                if (response.isSuccessful) {
                    requireContext().getSharedPreferences("MyTensesPrefs", Context.MODE_PRIVATE)
                        .edit()
                        .putString("name", newName)
                        .apply()

                    toggleEditMode(newName, username, false)
                    binding.profileBio.text = "Perubahan berhasil disimpan"
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    binding.profileBio.text = "Gagal menyimpan: $errorBody"
                    Log.e(TAG, "Update API error: ${response.code()} - $errorBody")
                }
            } catch (e: Exception) {
                binding.profileBio.text = "Gagal menyimpan: ${e.message}"
                Log.e(TAG, "Update network error: ${e.message}", e)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "ProfileFragment"
    }
}