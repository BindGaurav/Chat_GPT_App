package com.example.chatgptapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val client = OkHttpClient()

    // creating variables on below line.
    lateinit var txtResponse: TextView
    lateinit var idTVQuestion: TextView
    lateinit var etQuestion: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etQuestion = findViewById(R.id.etQuestion)
        idTVQuestion = findViewById(R.id.idTVQuestion)
        txtResponse = findViewById(R.id.txtResponse)

        etQuestion.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                txtResponse.text = "Please wait.."

                val question = etQuestion.text.toString().trim()
                if (question.isNotEmpty()) {
                    Toast.makeText(this, question, Toast.LENGTH_SHORT).show()
                    getResponse(question) { response ->
                        runOnUiThread {
                            txtResponse.text = response
                        }
                    }
                } else {
                    Toast.makeText(this, "Please enter a question", Toast.LENGTH_SHORT).show()
                }
                return@OnEditorActionListener true
            }
            false
        })
    }

    fun getResponse(question: String, callback: (String) -> Unit) {
        idTVQuestion.text = question
        etQuestion.setText("")

        val apiKey = " "  // Replace with your actual Azure OpenAI API key
        val url = " "

        // Request body for Azure OpenAI
        val requestBody = """
        {
            "messages": [
                {
                    "role": "user",
                    "content": "$question"
                }
            ],
            "temperature": 0.7,
            "max_tokens": 800,
            "top_p": 0.95
        }
    """.trimIndent()

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("api-key", apiKey)  // For Azure OpenAI, you need to use "api-key"
            .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("error", "API failed", e)
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "API request failed", Toast.LENGTH_SHORT).show()
                    txtResponse.text = "Error occurred"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (body != null) {
                    Log.v("data", "Response Body: $body")  // Log the full response for debugging
                    try {
                        // Directly parse and log the response for debugging
                        val jsonObject = JSONObject(body)
                        val choicesArray = jsonObject.getJSONArray("choices")
                        val messageObject = choicesArray.getJSONObject(0).getJSONObject("message")
                        val messageContent = messageObject.getString("content")
                        callback(messageContent)
                    } catch (e: Exception) {
                        Log.e("error", "Failed to parse response", e)
                        runOnUiThread {
                            txtResponse.text = "Failed to parse the response"
                        }
                    }
                } else {
                    Log.v("data", "empty")
                    runOnUiThread {
                        txtResponse.text = "Empty response from the server"
                    }
                }
            }
        })
    }



}
