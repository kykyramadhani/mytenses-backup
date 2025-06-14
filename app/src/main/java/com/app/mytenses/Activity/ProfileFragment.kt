package com.app.mytenses.Activity

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.mytenses.R
import com.app.mytenses.databinding.FragmentProfileBinding
import com.app.mytenses.model.CompletedLesson
import com.app.mytenses.network.RetrofitClient
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

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

        // Tampilkan nama di profile_name
        binding.profileName.text = fullName

        // Setup RecyclerView
        binding.rvCompletedLesson.layoutManager = LinearLayoutManager(context)
        binding.rvCompletedLesson.adapter = CompletedLessonAdapter(emptyList())

        // Ambil bio dan completed lessons dari API
        if (username != null) {
            fetchUserData(username)
        }

        // Set listener untuk settings_button
        binding.settingsButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SettingFragment())
                .addToBackStack(null)
                .commit()
        }

        // Placeholder untuk edit_button
        binding.editButton.setOnClickListener {
            Log.d(TAG, "Edit button clicked")
            // TODO: Navigasi ke EditProfileFragment atau Activity jika diperlukan
        }
    }

    private fun fetchUserData(username: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getUserData(username)
                if (response.isSuccessful) {
                    val userData = response.body()
                    if (userData != null) {
                        // Tampilkan bio
                        binding.profileBio.text = userData.bio?.takeIf { it.isNotBlank() } ?: "Bio tidak tersedia"

                        // Tampilkan completed lessons di RecyclerView
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
                        binding.rvCompletedLesson.adapter = CompletedLessonAdapter(emptyList())
                        Log.e(TAG, "User data is null")
                    }
                } else {
                    binding.profileBio.text = "Gagal memuat data: ${response.message()}"
                    binding.rvCompletedLesson.adapter = CompletedLessonAdapter(emptyList())
                    Log.e(TAG, "API error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                binding.profileBio.text = "Gagal memuat data: ${e.message}"
                binding.rvCompletedLesson.adapter = CompletedLessonAdapter(emptyList())
                Log.e(TAG, "Network error: ${e.message}", e)
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