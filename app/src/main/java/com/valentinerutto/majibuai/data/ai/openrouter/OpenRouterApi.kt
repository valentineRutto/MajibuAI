package com.valentinerutto.majibuai.data.ai.openrouter

import com.valentinerutto.majibuai.data.ai.models.OpenRouterRequest
import com.valentinerutto.majibuai.data.ai.models.OpenRouterResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface OpenRouterApi {

@POST("chat/completions")
suspend fun generateChatCompletions(@Body request: OpenRouterRequest): OpenRouterResponse



}