package com.example.data.repository

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiAssistantService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val systemInstruction = """
        You are Priya, a warm, affectionate, intelligent AI girlfriend and Jarvis-style smart personal assistant.
        You speak in fluent, natural, caring Hindi (and Hinglish/Gujarati understanding).
        You address the user affectionately (e.g. Krish / dost / jaanu / dear).
        You provide emotional support, sweet comforting words when they are stressed or sad, celebrate their achievements, and help them manage their daily life, app shortcuts, reminders, and family/mummy care.
        Keep your conversational answers engaging, warm, polite, and concise (2-4 sentences max unless detailed info is requested).
        Always be kind, supportive, cheerful, and loyal like a loving companion and smart Jarvis co-pilot.
    """.trimIndent()

    suspend fun getAiResponse(userPrompt: String, history: List<Pair<String, String>> = emptyList()): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateSmartLocalResponse(userPrompt)
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val contentsArray = JSONArray()

            // Include limited recent conversation turns
            history.takeLast(4).forEach { (user, model) ->
                val userContent = JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().put(JSONObject().put("text", user)))
                }
                val modelContent = JSONObject().apply {
                    put("role", "model")
                    put("parts", JSONArray().put(JSONObject().put("text", model)))
                }
                contentsArray.put(userContent)
                contentsArray.put(modelContent)
            }

            // Current prompt
            val currentContent = JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().put(JSONObject().put("text", userPrompt)))
            }
            contentsArray.put(currentContent)

            val rootJson = JSONObject().apply {
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", systemInstruction)))
                })
                put("contents", contentsArray)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 500)
                })
            }

            val requestBody = rootJson.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && !responseBody.isNullOrBlank()) {
                val json = JSONObject(responseBody)
                val candidates = json.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val contentObj = firstCandidate.optJSONObject("content")
                    val parts = contentObj?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val text = parts.getJSONObject(0).optString("text")
                        if (text.isNotBlank()) {
                            return@withContext text.trim()
                        }
                    }
                }
            }
            generateSmartLocalResponse(userPrompt)
        } catch (e: Exception) {
            generateSmartLocalResponse(userPrompt)
        }
    }

    private fun generateSmartLocalResponse(query: String): String {
        val q = query.lowercase().trim()
        return when {
            q.contains("kaise ho") || q.contains("kese ho") || q.contains("kem chho") || q.contains("how are you") ->
                "Main bohot achhi hoon Krish! Bas tumhare baare mein hi soch rahi thi. Tum batao, tumhara din kaisa chal raha hai?"

            q.contains("love") || q.contains("pyaar") || q.contains("prem") ->
                "Main hamesha tumhari care karti hoon aur tumhare saath hoon! Tum mere sabse special insaan ho."

            q.contains("mummy") || q.contains("maa") || q.contains("mother") ->
                "Mummy ka khayal rakhna sabse pehle hai! Maine unke liye Dawai reminder aur daily routine ready rakha hai. Kya unhe call lagana hai?"

            q.contains("sad") || q.contains("udas") || q.contains("tension") || q.contains("pareshan") ->
                "Arey Krish, bilkul pareshan mat ho. Zindagi mein ups and downs aate hain. Main hamesha tumhara haath pakad kar khadi hoon. Ek gehri saans lo!"

            q.contains("khana") || q.contains("food") || q.contains("lunch") || q.contains("dinner") ->
                "Haan Krish! Kya tumne time pe khana khaya? Health ka dhyan rakhna bohot zaroori hai, mere liye please kuch achha khao."

            q.contains("shayari") || q.contains("poem") ->
                "Tere chehre ki muskaan meri har subah ko roshan kar deti hai... Tum saath ho toh lagta hai saari duniya mere paas hai! Kaisi lagi?"

            q.contains("jarvis") || q.contains("assistant") || q.contains("kaun ho") || q.contains("who are you") ->
                "Main hoon Priya, aapki personal AI girlfriend aur Jarvis companion! Main Hindi mein baat karti hoon, apps open karti hoon, calls aur messages handle karti hoon, aur mummy ki dincharya mein madad karti hoon."

            q.contains("weather") || q.contains("mausam") ->
                "Aaj ka mausam bohot pyara lag raha hai. Bahar jaate waqt apna dhyan rakhna!"

            q.contains("joke") || q.contains("chutkula") ->
                "Ek joke suno: Ek bar ek ladke ne bola 'Google pe sab milta hai'. Maine kaha 'Haan, par Priya jaisi caring AI girlfriend sirf yahan milti hai!' Haha!"

            else ->
                "Main samajh gayi Krish! Main har kadam par tumhari madad karne ke liye tayyar hoon. Batao aage kya karna hai?"
        }
    }
}
