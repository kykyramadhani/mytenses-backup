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

class Chapter3ExampleFragment : Fragment() {

    private val TAG = "Chapter3ExampleFragment"
    private lateinit var progressBar: ProgressBar
    private lateinit var frameCard1: FrameLayout
    private lateinit var frameCard2: FrameLayout
    private lateinit var tvMainTitle: TextView
    private lateinit var tvSubTitle: TextView
    private lateinit var textOnImage1: TextView
    private lateinit var textOnImage2: TextView

    private val fragmentScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView called")
        return inflater.inflate(R.layout.fragment_chapter3_example, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageButton>(R.id.btnBackChapt3)
        val btnNext = view.findViewById<Button>(R.id.btnNextChapt3)
        tvMainTitle = view.findViewById(R.id.tvMainTitle) ?: throw IllegalStateException("tvMainTitle not found")
        tvSubTitle = view.findViewById(R.id.tvSubTitle) ?: throw IllegalStateException("tvSubTitle not found")
        textOnImage1 = view.findViewById(R.id.textOnImage1) ?: throw IllegalStateException("textOnImage1 not found")
        textOnImage2 = view.findViewById(R.id.textOnImage2) ?: throw IllegalStateException("textOnImage2 not found")
        progressBar = view.findViewById(R.id.progressBar) ?: throw IllegalStateException("progressBar not found")
        frameCard1 = view.findViewById(R.id.frameCard1) ?: throw IllegalStateException("frameCard1 not found")
        frameCard2 = view.findViewById(R.id.frameCard2) ?: throw IllegalStateException("frameCard2 not found")

        progressBar.visibility = View.VISIBLE
        frameCard1.visibility = View.GONE
        frameCard2.visibility = View.GONE

        fetchExampleData()

        btnBack?.setOnClickListener {
            Log.d(TAG, "Back button clicked")
            requireActivity().supportFragmentManager.popBackStack()
        }

        btnNext?.setOnClickListener {
            Log.d(TAG, "Next button clicked")
            fragmentScope.launch {

                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, QuizStartFragment.newInstance())
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    private fun fetchExampleData() {
        fragmentScope.launch {
            try {
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

                    val material = materialsResponse.materials.find { it.lesson_id == "simple_present" }
                    if (material == null) {
                        Log.e(TAG, "Material for 'simple_present' not found in response: ${materialsResponse.materials}")
                        showError("Material data not found")
                        return@launch
                    }

                    Log.d(TAG, "lesson_id: ${material.lesson_id}")
                    Log.d(TAG, "chapter_title: ${material.chapter_title}")
                    Log.d(TAG, "examples: ${material.examples}")

                    tvMainTitle.text = "Chapter 3"
                    tvSubTitle.text = "Contoh Simple Present"

                    val examples = material.examples ?: emptyList()
                    if (examples.isNotEmpty()) {
                        textOnImage1.text = "${examples[0].sentence}\n(${examples[0].example_translation})"
                        Log.d(TAG, "Card 1: ${textOnImage1.text}")
                    } else {
                        textOnImage1.text = "No example available"
                    }
                    if (examples.size > 1) {
                        textOnImage2.text = "${examples[1].sentence}\n(${examples[1].example_translation})"
                        Log.d(TAG, "Card 2: ${textOnImage2.text}")
                    } else {
                        textOnImage2.text = "No example available"
                    }

                    progressBar.visibility = View.GONE
                    frameCard1.visibility = View.VISIBLE
                    frameCard2.visibility = View.VISIBLE
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
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        progressBar.visibility = View.GONE
        frameCard1.visibility = View.VISIBLE
        frameCard2.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentScope.cancel()
    }

    companion object {
        fun newInstance() = Chapter3ExampleFragment()
    }
}