/*
Callum Smith - S2145086
 */

package com.caraid

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

//This class is used to analyse the content of a message using Google Cloud Natural Language API.
class NLPAnalyser {

    companion object {
        private val client = OkHttpClient()
        private const val API_KEY =
            "AIzaSyCL0Ff8BTvcjySzSf_XNPvRCEhJYV8S_tk"
        private const val BASE_URL = "https://language.googleapis.com/v1/documents"

        //This function takes a string as input and returns a user-friendly interpretation of sentiment analysis.
        fun analyseMessage(text: String): String {
            val sentiment = analyzeSentiment(text)
            return interpretSentiment(sentiment["score"]!!, sentiment["magnitude"]!!)
        }

        private fun analyzeSentiment(text: String): Map<String, Double> {
            val url = "$BASE_URL:analyzeSentiment?key=$API_KEY"
            val json = """
            {
                "document": {
                    "type": "PLAIN_TEXT",
                    "content": "$text"
                }
            }
            """.trimIndent()

            val requestBody =
                json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            return try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    val sentiment = jsonResponse.getJSONObject("documentSentiment")
                    val score = sentiment.getDouble("score")
                    val magnitude = sentiment.getDouble("magnitude")
                    mapOf("score" to score, "magnitude" to magnitude)
                } else {
                    // Handle error, log it, and return default values
                    println("Error analyzing sentiment: ${response.code} - ${response.message}")
                    mapOf("score" to 0.0, "magnitude" to 0.0) // Default sentiment values
                }
            } catch (e: IOException) {
                e.printStackTrace()
                mapOf("score" to 0.0, "magnitude" to 0.0) // Default sentiment values
            }
        }

        private fun interpretSentiment(score: Double, magnitude: Double): String {
            val baseSentiment = when {
                score > 0.25 -> "Positive"
                score < -0.25 -> "Negative"
                else -> "Neutral"
            }

            val strength = when {
                magnitude < 1 -> ""
                magnitude < 2 -> "slightly "
                magnitude < 5 -> "moderately "
                else -> "strongly "
            }

            return when (baseSentiment) {
                "Positive" -> "The sentiment is ${strength}positive."
                "Negative" -> "The sentiment is ${strength}negative."
                "Neutral" -> "The sentiment is neutral."
                else -> "Unable to determine sentiment."
            }
        }
    }
}