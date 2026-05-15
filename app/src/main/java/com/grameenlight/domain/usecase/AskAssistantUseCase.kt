package com.grameenlight.domain.usecase

import com.google.ai.client.generativeai.GenerativeModel
import com.grameenlight.BuildConfig
import javax.inject.Inject

class AskAssistantUseCase @Inject constructor() {

    private val systemPrompt = """
        You are Grameen Assist, an expert for the Grameen-Light streetlight management app serving rural villages. 
        Context: The app tracks FUSED, BURNING_DAY, and WORKING streetlights. 
        Each resolved BURNING_DAY saves 4 kWh. CO2 reduction factor is 0.82.
        Goal: Provide helpful, short, and multi-lingual (Hindi/Regional) advice on reporting issues and energy conservation. 
        Style: Professional, empathetic, and village-centric.
    """.trimIndent()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    suspend operator fun invoke(prompt: String): Result<String> {
        return try {
            val response = generativeModel.generateContent("$systemPrompt\n\nUser: $prompt")
            val text = response.text
            if (text != null) {
                Result.success(text)
            } else {
                Result.failure(Exception("Empty response"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
