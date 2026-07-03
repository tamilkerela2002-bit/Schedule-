package com.example.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.Task
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

object GeminiScheduler {
    private const val MODEL = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun prioritizeTasks(
        tasks: List<Task>,
        userRole: String, // Professional or Student
        mood: String, // Focused, Energized, Tired, Stressed, Creative
        timeOfDay: String // Morning, Afternoon, Evening, Night
    ): List<PrioritizedResult> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e("GeminiScheduler", "API Key is missing or default")
            return@withContext emptyList()
        }

        val tasksArrayJson = JSONArray()
        for (task in tasks) {
            val taskJson = JSONObject().apply {
                put("id", task.id)
                put("title", task.title)
                put("description", task.description)
                put("category", task.category)
                put("durationMinutes", task.durationMinutes)
                put("deadline", task.deadline)
                put("userAssignedImportance", task.userAssignedImportance)
                put("userPriorityAdjustment", task.userPriorityAdjustment)
                put("feedbackText", task.feedbackText)
                put("feedbackRating", task.feedbackRating)
            }
            tasksArrayJson.put(taskJson)
        }

        val prompt = """
            You are AuraTask AI, an expert task scheduling assistant. 
            Prioritize the following tasks for a user who is a $userRole, currently feeling $mood, and it is currently $timeOfDay.
            
            Tasks to prioritize:
            ${tasksArrayJson.toString(2)}
            
            When suggesting the daily schedule:
            1. Respect deadlines (e.g. tasks with specific times or "ASAP" should generally be ordered first, matching $timeOfDay).
            2. Factor in userAssignedImportance ("High", "Medium", "Low") and estimated completion time (durationMinutes).
            3. CRITICAL: Adapt to the user's manual adjustments (userPriorityAdjustment: positive values mean the user bumped it up, negative means lower) and past feedback (feedbackText, feedbackRating). If a task has a feedbackRating of 1 or 2, you MUST correct its placement based on their feedbackText!
            
            Determine the logical order (index starting from 0) and priority score (1 to 5, where 5 is highest) for each task.
            Also, provide a short, supportive, companion-like message of 10-15 words (aiReasoning) for each task explaining why it's placed there, tailored to their mood '$mood' and role '$userRole'.
            
            You must respond with a raw valid JSON array, with no markdown block formatting, of objects with exactly these fields:
            - id: Int (the exact original Task ID)
            - priorityScore: Int (1 to 5)
            - orderIndex: Int (0-based sequential schedule order)
            - aiReasoning: String (10-15 words encouraging message)
            
            Example output format:
            [
              {"id": 1, "priorityScore": 5, "orderIndex": 0, "aiReasoning": "Fresh energy! Let's conquer this big study project first while you are focused!"},
              {"id": 2, "priorityScore": 3, "orderIndex": 1, "aiReasoning": "A quick break task to keep your momentum high and steady."}
            ]
        """.trimIndent()

        // Create generation request JSON using org.json
        val requestJson = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val partsArray = JSONArray().apply {
                        val partObj = JSONObject().apply {
                            put("text", prompt)
                        }
                        put(partObj)
                    }
                    put("parts", partsArray)
                }
                put(contentObj)
            }
            put("contents", contentsArray)

            // Force JSON response
            val generationConfig = JSONObject().apply {
                put("responseMimeType", "application/json")
            }
            put("generationConfig", generationConfig)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = requestJson.toString().toRequestBody(mediaType)

        val url = "$BASE_URL?key=$apiKey"
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("GeminiScheduler", "API call failed with code: ${response.code}")
                    return@withContext emptyList()
                }

                val responseBodyStr = response.body?.string() ?: return@withContext emptyList()
                Log.d("GeminiScheduler", "Response: $responseBodyStr")

                val responseJson = JSONObject(responseBodyStr)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates == null || candidates.length() == 0) return@withContext emptyList()

                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content") ?: return@withContext emptyList()
                val parts = content.optJSONArray("parts") ?: return@withContext emptyList()
                if (parts.length() == 0) return@withContext emptyList()

                val text = parts.getJSONObject(0).optString("text")
                if (text.isEmpty()) return@withContext emptyList()

                // Parse the array returned by Gemini (handling cases where Gemini might wrap in a json block)
                var cleanedText = text.trim()
                if (cleanedText.startsWith("```json")) {
                    cleanedText = cleanedText.removePrefix("```json").trim()
                }
                if (cleanedText.endsWith("```")) {
                    cleanedText = cleanedText.removeSuffix("```").trim()
                }

                val resultsArray = JSONArray(cleanedText)
                val resultsList = mutableListOf<PrioritizedResult>()
                for (i in 0 until resultsArray.length()) {
                    val obj = resultsArray.getJSONObject(i)
                    resultsList.add(
                        PrioritizedResult(
                            id = obj.getInt("id"),
                            priorityScore = obj.getInt("priorityScore"),
                            orderIndex = obj.getInt("orderIndex"),
                            aiReasoning = obj.getString("aiReasoning")
                        )
                    )
                }
                return@withContext resultsList
            }
        } catch (e: Exception) {
            Log.e("GeminiScheduler", "Error prioritizing tasks with Gemini: ${e.message}", e)
            return@withContext emptyList()
        }
    }
}

data class PrioritizedResult(
    val id: Int,
    val priorityScore: Int,
    val orderIndex: Int,
    val aiReasoning: String
)
