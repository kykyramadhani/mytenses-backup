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
import com.android.volley.NoConnectionError
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.app.mytenses.R
import org.json.JSONObject

class Chapter2FormulaFragment : Fragment() {

    private val TAG = "Chapter2FormulaFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView called")
        return inflater.inflate(R.layout.fragment_chapter2_formula, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find views
        val btnBack = view.findViewById<ImageButton>(R.id.btnBackChapt2)
        val btnNext = view.findViewById<Button>(R.id.btnNextChapt2)
        val tvMainTitle = view.findViewById<TextView>(R.id.tvMainTitle)
        val tvSubTitle = view.findViewById<TextView>(R.id.tvSubTitle)
        val textOnImage1 = view.findViewById<TextView>(R.id.textOnImage1)
        val textOnImage2 = view.findViewById<TextView>(R.id.textOnImage2)
        val textOnImage3 = view.findViewById<TextView>(R.id.textOnImage3)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        // Initialize Volley request queue
        val queue = Volley.newRequestQueue(requireContext())
        val url = "https://mytenses-api.vercel.app/api/lessons"

        // Show ProgressBar, hide cards
        progressBar.visibility = View.VISIBLE
        view.findViewById<FrameLayout>(R.id.frameCard1).visibility = View.GONE
        view.findViewById<FrameLayout>(R.id.frameCard2).visibility = View.GONE
        view.findViewById<FrameLayout>(R.id.frameCard3).visibility = View.GONE

        // Create JSON request
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                Log.d(TAG, "API Response: $response")
                try {
                    // Check if simple_present exists
                    if (!response.has("simple_present")) {
                        Log.e(TAG, "Key 'simple_present' not found in response")
                        Toast.makeText(requireContext(), "Key 'simple_present' not found", Toast.LENGTH_LONG).show()
                        return@JsonObjectRequest
                    }

                    // Parse JSON response
                    val simplePresent = response.getJSONObject("simple_present")
                    val title = simplePresent.getString("title")
                    val materials = simplePresent.getJSONObject("materials")

                    // Get materials as a list (iterate over keys)
                    val materialList = mutableListOf<JSONObject>()
                    materials.keys().forEach { key ->
                        materialList.add(materials.getJSONObject(key))
                        Log.d(TAG, "Material Key: $key")
                    }

                    // Get formulas from the first material
                    val formulas = if (materialList.isNotEmpty()) {
                        materialList[0].getJSONArray("formulas")
                    } else {
                        null
                    }

                    // Update UI
                    tvMainTitle.text = "Chapter 2"
                    tvSubTitle.text = "Rumus $title"

                    // Update cards with formula data
                    if (formulas != null && formulas.length() > 0) {
                        val positiveFormula = formulas.getJSONObject(0)
                        textOnImage1.text = "${positiveFormula.getString("type").capitalize()}: ${positiveFormula.getString("formula")}"
                        Log.d(TAG, "Card 1: ${textOnImage1.text}")
                    }
                    if (formulas != null && formulas.length() > 1) {
                        val negativeFormula = formulas.getJSONObject(1)
                        textOnImage2.text = "${negativeFormula.getString("type").capitalize()}: ${negativeFormula.getString("formula")}"
                        Log.d(TAG, "Card 2: ${textOnImage2.text}")
                    }
                    if (formulas != null && formulas.length() > 2) {
                        val questionFormula = formulas.getJSONObject(2)
                        textOnImage3.text = "${questionFormula.getString("type").capitalize()}: ${questionFormula.getString("formula")}"
                        Log.d(TAG, "Card 3: ${textOnImage3.text}")
                    }

                    // Hide ProgressBar, show cards
                    progressBar.visibility = View.GONE
                    view.findViewById<FrameLayout>(R.id.frameCard1).visibility = View.VISIBLE
                    view.findViewById<FrameLayout>(R.id.frameCard2).visibility = View.VISIBLE
                    view.findViewById<FrameLayout>(R.id.frameCard3).visibility = View.VISIBLE
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing JSON: ${e.message}")
                    textOnImage1.text = "Error parsing data"
                    textOnImage2.text = "Error parsing data"
                    textOnImage3.text = "Error parsing data"
                    Toast.makeText(requireContext(), "Error parsing data: ${e.message}", Toast.LENGTH_LONG).show()
                    progressBar.visibility = View.GONE
                    view.findViewById<FrameLayout>(R.id.frameCard1).visibility = View.VISIBLE
                    view.findViewById<FrameLayout>(R.id.frameCard2).visibility = View.VISIBLE
                    view.findViewById<FrameLayout>(R.id.frameCard3).visibility = View.VISIBLE
                }
            },
            { error ->
                val errorMessage = when (error) {
                    is NoConnectionError -> "No internet connection"
                    else -> "Error loading data: ${error.message}"
                }
                Log.e(TAG, errorMessage)
                textOnImage1.text = errorMessage
                textOnImage2.text = errorMessage
                textOnImage3.text = errorMessage
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                progressBar.visibility = View.GONE
                view.findViewById<FrameLayout>(R.id.frameCard1).visibility = View.VISIBLE
                view.findViewById<FrameLayout>(R.id.frameCard2).visibility = View.VISIBLE
                view.findViewById<FrameLayout>(R.id.frameCard3).visibility = View.VISIBLE
            }
        )

        // Add request to queue
        Log.d(TAG, "Adding request to queue: $url")
        queue.add(jsonObjectRequest)

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

    companion object {
        @JvmStatic
        fun newInstance() = Chapter2FormulaFragment()
    }
}