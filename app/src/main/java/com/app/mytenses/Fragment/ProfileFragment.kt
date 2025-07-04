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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.app.mytenses.model.CompletedLesson
import com.app.mytenses.CompletedLessonAdapter
import com.app.mytenses.R
import com.app.mytenses.data.database.AppDatabase
import com.app.mytenses.data.repository.UserRepository
import com.app.mytenses.databinding.FragmentProfileBinding
import com.app.mytenses.network.RetrofitClient
import com.app.mytenses.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("Binding is null")
    private var isEditMode = false
    private lateinit var userRepository: UserRepository
    private val apiService = RetrofitClient.apiService
    private var fetchUserDataJob: Job? = null
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = view.context
        if (context == null) {
            Log.e(TAG, "Context is null, skipping initialization")
            return
        }

        // Inisialisasi UserRepository
        userRepository = UserRepository(apiService, context, AppDatabase.getDatabase(context).userDao())

        val sharedPreferences = requireContext().getSharedPreferences("MyTensesPrefs", Context.MODE_PRIVATE)
        val fullName = sharedPreferences.getString("name", "Nama Pengguna")
        val username = sharedPreferences.getString("username",null).toString()
        Log.d(TAG, "Username: $username")

        if (username.isEmpty()) {
            Log.e(TAG, "Invalid username: $username")
            binding.profileBio.text = "Error: Username tidak valid"
            return
        }

        binding.rvCompletedLesson.layoutManager = LinearLayoutManager(context)
        binding.rvCompletedLesson.adapter = CompletedLessonAdapter(emptyList())

        swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout) ?: run {
            Log.e(TAG, "swipeRefreshLayout not found in layout")
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            fetchLocalUserData(username, fullName)
        }

        swipeRefreshLayout.setOnRefreshListener {
            viewLifecycleOwner.lifecycleScope.launch {
                fetchUserData(username, fullName)
                swipeRefreshLayout.isRefreshing = false
            }
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
            saveProfileChanges(username)
        }
    }

    private fun fetchUserData(username: String, defaultName: String?) {
        // Batalkan job sebelumnya jika ada
        fetchUserDataJob?.cancel()
        fetchUserDataJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                val context = view?.context ?: return@launch
                if (NetworkUtils.isOnline(context)) {
                    Log.d(TAG, "Online: Fetching user data from API")
                    val response = withContext(Dispatchers.IO) {
                        apiService.getUserData(username)
                    }
                    if (response.isSuccessful) {
                        val userData = response.body()
                        if (userData != null && _binding != null) {
                            binding.profileName.text = userData.name?.takeIf { it.isNotBlank() } ?: defaultName ?: "Nama Pengguna"
                            binding.profileBio.text = userData.bio?.takeIf { it.isNotBlank() } ?: "Bio tidak tersedia"
                            requireContext().getSharedPreferences("MyTensesPrefs", Context.MODE_PRIVATE)
                                .edit()
                                .putString("name", userData.name)
                                .apply()
                            val completedLessons = userData.completed_lessons?.map { CompletedLesson(it.lesson_id, it.title) } ?: emptyList()
                            updateCompletedLessons(completedLessons)
                            // Sinkronkan data ke database
                            withContext(Dispatchers.IO) {
                                userRepository.syncUserData(username)
                            }
                        } else {
                            Log.e(TAG, "User data is null or binding is null")
                        }
                    } else {
                        Log.e(TAG, "API error: ${response.code()} - ${response.errorBody()?.string()}")
                    }
                }
                fetchLocalUserData(username, defaultName)
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) {
                    Log.d(TAG, "Fetch user data cancelled")
                    throw e
                }
                Log.e(TAG, "Error fetching user data: ${e.message}", e)
                if (_binding != null) {
                    binding.profileBio.text = "Gagal memuat data: ${e.message}"
                    updateCompletedLessons(emptyList())
                }
            } finally {
                fetchUserDataJob = null
                if (swipeRefreshLayout.isRefreshing) {
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }

    private suspend fun fetchLocalUserData(username: String, defaultName: String?) {
        val userEntity = withContext(Dispatchers.IO) {
            userRepository.getUserByUsername(username)
        }
        if (_binding != null) {
            if (userEntity != null) {
                binding.profileName.text = userEntity.name.takeIf { it.isNotBlank() } ?: defaultName ?: "Nama Pengguna"
                binding.profileBio.text = userEntity.bio.takeIf { it.isNotBlank() } ?: "Bio tidak tersedia"
                val completedLessons = userEntity.completedLessons.map {
                    CompletedLesson(it, it.replace("_", " ").split(" ").joinToString(" ") { word ->
                        word.replaceFirstChar { char ->
                            if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
                        }
                    })
                }
                updateCompletedLessons(completedLessons)
            } else {
                binding.profileBio.text = "Data offline tidak tersedia"
                updateCompletedLessons(emptyList())
            }
        }
    }

    private fun updateCompletedLessons(completedLessons: List<CompletedLesson>) {
        if (_binding != null) {
            if (completedLessons.isEmpty()) {
                binding.emptyLessonsText.visibility = View.VISIBLE
                binding.rvCompletedLesson.visibility = View.GONE
            } else {
                binding.emptyLessonsText.visibility = View.GONE
                binding.rvCompletedLesson.visibility = View.VISIBLE
                binding.rvCompletedLesson.adapter = CompletedLessonAdapter(completedLessons)
            }
        }
    }

    private fun toggleEditMode(currentName: String?, username: String?, enterEditMode: Boolean) {
        isEditMode = enterEditMode
        Log.d(TAG, "Toggle edit mode: isEditMode=$isEditMode")
        if (_binding != null) {
            if (isEditMode) {
                binding.profileName.visibility = View.GONE
                binding.profileBio.visibility = View.GONE
                binding.profileNameEdit.visibility = View.VISIBLE
                binding.profileBioEdit.visibility = View.VISIBLE
                binding.saveButton.visibility = View.VISIBLE
                binding.editButton.visibility = View.GONE
                binding.cancelButton.visibility = View.VISIBLE

                binding.profileNameEdit.setText(currentName?.takeIf { it != "Nama Pengguna" } ?: "")
                binding.profileBioEdit.setText(binding.profileBio.text?.takeIf { it != "Bio tidak tersedia" } ?: "")
            } else {
                binding.profileName.visibility = View.VISIBLE
                binding.profileBio.visibility = View.VISIBLE
                binding.profileNameEdit.visibility = View.GONE
                binding.profileBioEdit.visibility = View.GONE
                binding.saveButton.visibility = View.GONE
                binding.editButton.visibility = View.VISIBLE
                binding.cancelButton.visibility = View.GONE

                if (username != null && username.isNotBlank()) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        fetchLocalUserData(username, currentName)
                    }
                }
            }
        }
    }

    private fun saveProfileChanges(username: String) {
        val context = view?.context ?: return
        if (!NetworkUtils.isOnline(context)) {
            binding.profileBio.text = "Gagal menyimpan: Tidak ada koneksi internet"
            Log.e(TAG, "Cannot save changes: Offline")
            return
        }

        val newName = binding.profileNameEdit.text?.toString()?.trim() ?: ""
        val newBio = binding.profileBioEdit.text?.toString()?.trim() ?: ""
        Log.d(TAG, "Saving changes: username=$username, name=$newName, bio=$newBio")

        val updateData = mutableMapOf<String, String>()
        updateData["name"] = newName
        updateData["bio"] = newBio

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.updateUserData(username, updateData)
                }
                if (response.isSuccessful) {
                    requireContext().getSharedPreferences("MyTensesPrefs", Context.MODE_PRIVATE)
                        .edit()
                        .putString("name", newName)
                        .apply()
                    toggleEditMode(newName, username, false)
                    binding.profileBio.text = "Perubahan berhasil disimpan"
                    // Fetch data baru langsung setelah save berhasil
                    fetchUserData(username, newName)
                    // Sinkronkan data ke database
                    withContext(Dispatchers.IO) {
                        userRepository.syncUserData(username)
                    }
                } else {
                    binding.profileBio.text = "Gagal menyimpan: ${response.errorBody()?.string() ?: "Unknown error"}"
                    Log.e(TAG, "Update API error: ${response.code()}")
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) {
                    Log.d(TAG, "Save profile changes cancelled")
                    throw e
                }
                binding.profileBio.text = "Gagal menyimpan: ${e.message}"
                Log.e(TAG, "Update network error: ${e.message}", e)
            }
        }
    }

    override fun onDestroyView() {
        fetchUserDataJob?.cancel()
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val TAG = "ProfileFragment"
    }
}