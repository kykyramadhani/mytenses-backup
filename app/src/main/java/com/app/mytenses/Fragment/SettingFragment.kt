package com.app.mytenses.Fragment

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.app.mytenses.R
import com.app.mytenses.Activity.LoginActivity
import android.widget.Toast
// import com.google.firebase.auth.FirebaseAuth // Jika kamu pakai Firebase Auth

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SettingFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Tombol kembali
        val backIcon = view.findViewById<View>(R.id.back_icon)
        backIcon.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Ubah warna icon setting
        val settingsButton = view.findViewById<ImageButton>(R.id.settings_button)
        settingsButton.setColorFilter(Color.parseColor("#0D47A1"))

        // Klik "Edit Profil"
        val editProfileSection = view.findViewById<View>(R.id.edit_profile_section)
        editProfileSection.setOnClickListener {
            val profileFragment = ProfileFragment()
            profileFragment.arguments = Bundle().apply {
                putBoolean("edit_mode", true)
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, profileFragment)
                .addToBackStack(null)
                .commit()
        }

        // Klik "Keluar"
        val logoutSection = view.findViewById<View>(R.id.logout_section)
        logoutSection.setOnClickListener {
            // (Jika pakai Firebase Auth, bisa logout di sini juga)
            // FirebaseAuth.getInstance().signOut()

            // Hapus semua data login
            val sharedPref = requireActivity().getSharedPreferences("MyTensesPrefs", Context.MODE_PRIVATE)
            sharedPref.edit().clear().apply()

            // Feedback
            Toast.makeText(requireContext(), "Berhasil logout", Toast.LENGTH_SHORT).show()

            // Arahkan ke LoginActivity dan hapus backstack
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SettingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}


