package com.app.mytenses.Activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.app.mytenses.R
import com.app.mytenses.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.UnknownHostException

class Chapter2FormulaFragment : Fragment() {

    private val TAG = "Chapter2FormulaFragment"
    private lateinit var progressBar: ProgressBar
    private lateinit var frameCard1: FrameLayout
    private lateinit var frameCard2: FrameLayout
    private lateinit var frameCard3: FrameLayout
    private lateinit var tvMainTitle: TextView
    private lateinit var tvSubTitle: TextView
    private lateinit var textOnImage1: TextView
    private lateinit var textOnImage2: TextView
    private lateinit var textOnImage3: TextView

    // Coroutine scope for the Fragment
    private val fragmentScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView called")
        return inflater.inflate(R.layout.fragment_chapter2_formula, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        val btnBack = view.findViewById<ImageButton>(R.id.btnBackChapt2)
        val btnNext = view.findViewById<Button>(R.id.btnNextChapt2)
        tvMainTitle = view.findViewById(R.id.tvMainTitle)
        tvSubTitle = view.findViewById(R.id.tvSubTitle)
        textOnImage1 = view.findViewById(R.id.textOnImage1)
        textOnImage2 = view.findViewById(R.id.textOnImage2)
        textOnImage3 = view.findViewById(R.id.textOnImage3)
        progressBar = view.findViewById(R.id.progressBar)
        frameCard1 = view.findViewById(R.id.frameCard1)
        frameCard2 = view.findViewById(R.id.frameCard2)
        frameCard3 = view.findViewById(R.id.frameCard3)

        // Show ProgressBar, hide cards
        progressBar.visibility = View.VISIBLE
        frameCard1.visibility = View.GONE
        frameCard2.visibility = View.GONE
        frameCard3.visibility = View.GONE

        // Fetch data using coroutine
        fetchFormulaData()

        // Set back button listener
        btnBack?.setOnClickListener {
            Log.d(TAG, "Back button clicked")
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Set next button listener
        btnNext?.setOnClickListener {
            Log.d(TAG, "Next button clicked")
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, Chapter3ExampleFragment.newInstance())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun fetchFormulaData() {
        fragmentScope.launch {
            try {
                // Perform API call on IO dispatcher
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getMaterials()
                }

                if (response.isSuccessful) {
                    val materialsResponse = response.body()
                    Log.d(TAG, "Raw API Response: ${materialsResponse?.materials}")

                    if (materialsResponse == null || materialsResponse.materials.isEmpty()) {
                        Log.e(TAG, "API response is null or empty")
                        showError("No material data received")
                        return@launch
                    }

                    // Find material for "simple_present"
                    val material = materialsResponse.materials.find { it.lesson_id == "simple_present" }
                    if (material == null) {
                        Log.e(TAG, "Material for 'simple_present' not found in response: ${materialsResponse.materials}")
                        showError("Material data not found")
                        return@launch
                    }

                    // Verify and log the material fields
                    Log.d(TAG, "lesson_id: ${material.lesson_id}")
                    Log.d(TAG, "chapter_title: ${material.chapter_title}")
                    Log.d(TAG, "formulas: ${material.formulas}")

                    // Update UI
                    tvMainTitle.text = "Chapter 2"
                    tvSubTitle.text = "Rumus Simple Present" // Hardcoded since no lessons in MaterialsResponse

                    // Update cards with formula data
                    val formulas = material.formulas ?: emptyList()
                    if (formulas.isNotEmpty()) {
                        textOnImage1.text = "${formulas[0].type?.replaceFirstChar { it.uppercase() } ?: "Positive"}: ${formulas[0].formula}"
                        Log.d(TAG, "Card 1: ${textOnImage1.text}")
                    }
                    if (formulas.size > 1) {
                        textOnImage2.text = "${formulas[1].type?.replaceFirstChar { it.uppercase() } ?: "Negative"}: ${formulas[1].formula}"
                        Log.d(TAG, "Card 2: ${textOnImage2.text}")
                    }
                    if (formulas.size > 2) {
                        textOnImage3.text = "${formulas[2].type?.replaceFirstChar { it.uppercase() } ?: "Question"}: ${formulas[2].formula}"
                        Log.d(TAG, "Card 3: ${textOnImage3.text}")
                    }

                    // Hide ProgressBar, show cards
                    progressBar.visibility = View.GONE
                    frameCard1.visibility = View.VISIBLE
                    frameCard2.visibility = View.VISIBLE
                    frameCard3.visibility = View.VISIBLE
                } else {
                    val errorMessage = "Error: ${response.code()} - ${response.message()}"
                    Log.e(TAG, "HTTP Error: $errorMessage")
                    showError(errorMessage)
                }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is HttpException -> "HTTP Error: ${e.code()} - ${e.message()}"
                    is UnknownHostException -> "No internet connection"
                    else -> "Error loading data: ${e.message}"
                }
                Log.e(TAG, errorMessage, e)
                showError(errorMessage)
            }
        }
    }

    private fun showError(message: String) {
        textOnImage1.text = message
        textOnImage2.text = message
        textOnImage3.text = message
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        progressBar.visibility = View.GONE
        frameCard1.visibility = View.VISIBLE
        frameCard2.visibility = View.VISIBLE
        frameCard3.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Cancel coroutines when the view is destroyed
        fragmentScope.cancel()
    }

    companion object {
        fun newInstance() = Chapter2FormulaFragment()
    }
}