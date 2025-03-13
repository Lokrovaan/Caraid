package com.caraid

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class NLPAnalyser {

    companion object {
        private const val API_URL_SENTIMENT =
            "https://language.googleapis.com/v1/documents:analyzeSentiment"
        private const val API_URL_ENTITIES =
            "https://language.googleapis.com/v1/documents:analyzeEntities"
        private const val API_KEY = "AIzaSyCL0Ff8BTvcjySzSf_XNPvRCEhJYV8S_tk"

        fun analyseMessage(text: String): Map<String, Any> {
            val client = OkHttpClient()

            val jsonBody = JSONObject().apply {
                put("document", JSONObject().apply {
                    put("content", text)
                    put("type", "PLAIN_TEXT")
                })
            }.toString()

            val requestBody =
                jsonBody.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

            val request = Request.Builder()
                .url("${API_URL_SENTIMENT}?key=$API_KEY")
                .post(requestBody)
                .build()

            return try {
                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string() // Get the response body here

                    if (!response.isSuccessful) {
                        // Log the error response
                        println("NLP API Error: ${response.code} - ${responseBody ?: "No body"}")
                        throw IOException("Unexpected response code: ${response.code}")
                    }

                    // Log the entire response body for debugging
                    println("NLP API Response (Full): ${responseBody ?: "No body"}")

                    parseAnalysisResponse(responseBody)
                }
            } catch (e: IOException) {
                // Handle the error appropriately, e.g., log it and return an empty map or a map with an error message
                e.printStackTrace()
                println("NLP API IOException: ${e.message}")
                return mapOf("error" to "API request failed: ${e.message}") // Or return a map with an error message
            } catch (e: Exception) {
                // Catch any other exceptions
                e.printStackTrace()
                println("NLP API Exception: ${e.message}")
                return mapOf("error" to "Analysis failed: ${e.message}")
            }
        }

        fun analyseEntities(text: String): Map<String, Any> {
            val client = OkHttpClient()

            val jsonBody = JSONObject().apply {
                put("document", JSONObject().apply {
                    put("content", text)
                    put("type", "PLAIN_TEXT")
                })
            }.toString()

            val requestBody =
                jsonBody.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

            val request = Request.Builder()
                .url("${API_URL_ENTITIES}?key=$API_KEY")
                .post(requestBody)
                .build()

            var result: Map<String, Any> // Declare result variable here

            try {
                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string()

                    if (!response.isSuccessful) {
                        println("NLP Entities API Error: ${response.code} - ${responseBody ?: "No body"}")
                        throw IOException("Unexpected response code: ${response.code}")
                    }

                    println("NLP Entities API Response: ${responseBody ?: "No body"}")
                    result = parseEntitiesResponse(responseBody) // Assign result here
                }
            } catch (e: IOException) {
                e.printStackTrace()
                println("NLP Entities API IOException: ${e.message}")
                result = mapOf("error" to "API request failed: ${e.message}") // Assign result here
            } catch (e: Exception) {
                e.printStackTrace()
                println("NLP Entities API Exception: ${e.message}")
                result = mapOf("error" to "Analysis failed: ${e.message}") // Assign result here
            }

            return result // Return result variable here
        }

        private fun parseAnalysisResponse(responseBody: String?): Map<String, Any> {
            val results = mutableMapOf<String, Any>()

            if (responseBody == null) {
                println("NLP: Response body is null")
                return results
            }

            try {
                val jsonResponse = JSONObject(responseBody)

                // Parse sentiment analysis
                val documentSentiment =
                    jsonResponse.optJSONObject("documentSentiment") // Use optJSONObject
                if (documentSentiment != null) {
                    val sentimentScore = documentSentiment.optDouble("score") // Use optDouble
                    results["sentiment"] = sentimentScore
                } else {
                    println("NLP: documentSentiment not found in response")
                    results["sentiment"] = "No sentiment data"
                }

                // You can add parsing for other features like entities, etc., here
                // Example (this might need adjustment based on the API response structure):
                // val entities = jsonResponse.getJSONArray("entities")
                // results["entities"] = parseEntities(entities)

            } catch (e: Exception) {
                e.printStackTrace()
                println("NLP Parsing Error: ${e.message}")
                results["error"] = "Parsing error: ${e.message}"
            }

            return results
        }

        private fun parseEntitiesResponse(responseBody: String?): Map<String, Any> {
            val results = mutableMapOf<String, Any>()

            if (responseBody == null) {
                println("NLP Entities: Response body is null")
                return results
            }

            try {
                val jsonResponse = JSONObject(responseBody)
                val entities = jsonResponse.optJSONArray("entities")

                if (entities != null) {
                    val entityList = mutableListOf<String>()
                    for (i in 0 until entities.length()) {
                        val entity = entities.optJSONObject(i)
                        val entityName = entity?.optString("name")
                        if (entityName != null) {
                            entityList.add(entityName)
                        }
                    }
                    results["entities"] = entityList
                } else {
                    println("NLP Entities: entities not found in response")
                    results["entities"] = emptyList<String>()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("NLP Entities Parsing Error: ${e.message}")
                results["error"] = "Parsing error: ${e.message}"
            }

            return results
        }
    }
}