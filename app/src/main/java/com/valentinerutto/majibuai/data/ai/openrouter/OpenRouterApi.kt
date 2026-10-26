package com.valentinerutto.majibuai.data.ai.openrouter

import com.valentinerutto.majibuai.data.ai.models.OpenRouterRequest
import com.valentinerutto.majibuai.data.ai.models.OpenRouterResponse
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response
import retrofit2.http.Streaming

interface OpenRouterApi {

    @Streaming
    @POST("chat/completions")
    suspend fun streamChatCompletions(@Body request: OpenRouterRequest): Response<ResponseBody>

//   Why not use Response<OpenRouterResponse> for streaming?
//    Non-streaming	                 Streaming
//    Response<OpenRouterResponse>	Response<ResponseBody>
//    One complete JSON object	    Multiple SSE events
//    choices[0].message.content	choices[0].delta.content
//    Retrofit deserializes JSON	You read and deserialize each event
//    Returns one answer	         Emits text chunks progressively



}
