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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ambil data dari SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("MyTensesPrefs", Context.MODE_PRIVATE)
        val fullName = sharedPreferences.getString("name", "Nama Pengguna")
        val userId = sharedPreferences.getInt("user_id", -1)
        val username = sharedPreferences.getString("username", "user_$userId")
        Log.d(TAG, "User ID: $userId, Username: $username")

        // Tampilkan nama di profile_name
        binding.profileName.text = fullName

        // Setup RecyclerView
        binding.rvCompletedLesson.layoutManager = LinearLayoutManager(context)
        binding.rvCompletedLesson.adapter = CompletedLessonAdapter(emptyList())

        // Ambil bio dan completed lessons dari API
        if (username != null && username.isNotBlank()) {
            fetchUserData(username)
        } else {
            Log.e(TAG, "Invalid username: $username")
            binding.profileBio.text = "Error: Username tidak valid"
        }

        // Set listener untuk settings_button
        binding.settingsButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SettingFragment())
                .addToBackStack(null)
                .commit()
        }

        // Set listener untuk edit_button
        binding.editButton.setOnClickListener {
            toggleEditMode(fullName, username, true)
        }

        // Set listener untuk cancel_button
        binding.cancelButton.setOnClickListener {
            toggleEditMode(fullName, username, false)
        }

        // Set listener untuk save_button
        binding.saveButton.setOnClickListener {
            if (username != null && username.isNotBlank()) {
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
                        binding.profileName.text = userData.name?.takeIf { it.isNotBlank() } ?: "Nama Pengguna"
                        binding.profileBio.text = userData.bio?.takeIf { it.isNotBlank() } ?: "Bio tidak tersedia"
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
                            binding.rvCompletedLesson.adapter = CompletedLessonAdapter(completedLessons)
                        }
                    } else {
                        binding.profileBio.text = "Gagal memuat data: Data kosong"
                        binding.emptyLessonsText.visibility = View.VISIBLE
                        binding.rvCompletedLesson.visibility = View.GONE
                        binding.rvCompletedLesson.adapter = CompletedLessonAdapter(emptyList())
                        Log.e(TAG, "User data is null")
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    binding.profileBio.text = "Gagal memuat data: $errorBody"
                    binding.emptyLessonsText.visibility = View.VISIBLE
                    binding.rvCompletedLesson.visibility = View.GONE
                    binding.rvCompletedLesson.adapter = CompletedLessonAdapter(emptyList())
                    Log.e(TAG, "API error: ${response.code()} - $errorBody")
                }
            } catch (e: Exception) {
                binding.profileBio.text = "Gagal memuat data: ${e.message}"
                binding.emptyLessonsText.visibility = View.VISIBLE
                binding.rvCompletedLesson.visibility = View.GONE
                binding.rvCompletedLesson.adapter = CompletedLessonAdapter(emptyList())
                Log.e(TAG, "Network error: ${e.message}", e)
            }
        }
    }

    private fun toggleEditMode(currentName: String?, username: String?, enterEditMode: Boolean) {
        isEditMode = enterEditMode
        Log.d(TAG, "Toggle edit mode: isEditMode=$isEditMode")
        if (isEditMode) {
            // Masuk mode edit
            binding.profileName.visibility = View.GONE
            binding.profileBio.visibility = View.GONE
            binding.profileNameEdit.visibility = View.VISIBLE
            binding.profileBioEdit.visibility = View.VISIBLE
            binding.saveButton.visibility = View.VISIBLE
            binding.editButton.visibility = View.GONE
            binding.cancelButton.visibility = View.VISIBLE

            // Isi EditText dengan data saat ini
            binding.profileNameEdit.setText(currentName?.takeIf { it != "Nama Pengguna" } ?: "")
            binding.profileBioEdit.setText(binding.profileBio.text.takeIf { it != "Bio tidak tersedia" } ?: "")
        } else {
            // Keluar mode edit
            binding.profileName.visibility = View.VISIBLE
            binding.profileBio.visibility = View.VISIBLE
            binding.profileNameEdit.visibility = View.GONE
            binding.profileBioEdit.visibility = View.GONE
            binding.saveButton.visibility = View.GONE
            binding.editButton.visibility = View.VISIBLE
            binding.cancelButton.visibility = View.GONE

            // Refresh data dari API
            if (username != null && username.isNotBlank()) {
                fetchUserData(username)
            }
        }
    }

    private fun saveProfileChanges(username: String) {
        val newName = binding.profileNameEdit.text.toString().trim()
        val newBio = binding.profileBioEdit.text.toString().trim()
        Log.d(TAG, "Saving changes: username=$username, name=$newName, bio=$newBio")

        val updateData = mutableMapOf<String, String>()
        updateData["name"] = newName
        updateData["bio"] = newBio
        Log.d(TAG, "Update data: $updateData")

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