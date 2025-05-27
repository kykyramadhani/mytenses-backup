package com.app.mytenses.Activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
import android.widget.FrameLayout

class Chapter3ExampleFragment : Fragment() {

    private val TAG = "Chapter3ExampleFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView called")
        return inflater.inflate(R.layout.fragment_chapter3_example, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find views
        val btnBack = view.findViewById<ImageButton>(R.id.btnBackChapt3)
        val btnNext = view.findViewById<Button>(R.id.btnNextChapt3)
        val tvMainTitle = view.findViewById<TextView>(R.id.tvMainTitle)
        val tvSubTitle = view.findViewById<TextView>(R.id.tvSubTitle)
        val textOnImage1 = view.findViewById<TextView>(R.id.textOnImage1)
        val textOnImage2 = view.findViewById<TextView>(R.id.textOnImage2)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        // Initialize Volley
        val queue = Volley.newRequestQueue(requireContext())
        val url = "https://mytenses-api.vercel.app/api/api/lessons"

        // Show ProgressBar, hide cards
        progressBar.visibility = View.VISIBLE
        view.findViewById<FrameLayout>(R.id.frameCard1).visibility = View.GONE
        view.findViewById<FrameLayout>(R.id.frameCard2).visibility = View.GONE

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
                    val examples = simplePresent.getJSONArray("examples")

                    Log.d(TAG, "Title: $title")
                    Log.d(TAG, "Examples: $examples")

                    // Update UI
                    tvMainTitle.text = "Chapter 3"
                    tvSubTitle.text = "Contoh $title"

                    // Update cards with example data
                    if (examples.length() > 0) {
                        val firstExample = examples.getJSONObject(0)
                        textOnImage1.text = "“${firstExample.getString("sentence")}”\nArtinya: “${firstExample.getString("translation")}”"
                        Log.d(TAG, "Card 1: ${textOnImage1.text}")
                    }
                    if (examples.length() > 1) {
                        val secondExample = examples.getJSONObject(1)
                        textOnImage2.text = "“${secondExample.getString("sentence")}”\nArtinya: “${secondExample.getString("translation")}”"
                        Log.d(TAG, "Card 2: ${textOnImage2.text}")
                    }

                    // Hide ProgressBar, show cards
                    progressBar.visibility = View.GONE
                    view.findViewById<FrameLayout>(R.id.frameCard1).visibility = View.VISIBLE
                    view.findViewById<FrameLayout>(R.id.frameCard2).visibility = View.VISIBLE
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing JSON: ${e.message}")
                    textOnImage1.text = "Error parsing data"
                    textOnImage2.text = "Error parsing data"
                    Toast.makeText(requireContext(), "Error parsing data: ${e.message}", Toast.LENGTH_LONG).show()
                    progressBar.visibility = View.GONE
                    view.findViewById<FrameLayout>(R.id.frameCard1).visibility = View.VISIBLE
                    view.findViewById<FrameLayout>(R.id.frameCard2).visibility = View.VISIBLE
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
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                progressBar.visibility = View.GONE
                view.findViewById<FrameLayout>(R.id.frameCard1).visibility = View.VISIBLE
                view.findViewById<FrameLayout>(R.id.frameCard2).visibility = View.VISIBLE
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
                .replace(R.id.fragment_container, QuizStartFragment.newInstance())
                .addToBackStack(null)
                .commit()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = Chapter3ExampleFragment()
    }
}