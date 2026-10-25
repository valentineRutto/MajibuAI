package com.valentinerutto.majibuai.data.ai

import com.valentinerutto.majibuai.data.ai.models.AiChatRequest
import com.valentinerutto.majibuai.data.ai.models.AiError
import com.valentinerutto.majibuai.data.ai.models.AiProviderException
import com.valentinerutto.majibuai.data.ai.models.AiResult
import com.valentinerutto.majibuai.data.ai.models.ChatMessage
import com.valentinerutto.majibuai.data.ai.models.toAiError
import com.valentinerutto.majibuai.data.ai.openrouter.OpenRouterEngine
import kotlin.coroutines.cancellation.CancellationException

class AiRepository(private val firebaseEngine: FirebaseGeminiAIEngine, private val openRouterEngine: OpenRouterEngine) {

    suspend fun explain(messages: List<ChatMessage>): AiResult<String> =
        explainOpenRouterAi(messages)

    suspend fun explainFirebaseAi(messages: List<ChatMessage>): AiResult<String> {

        return try {
            val request = AiChatRequest(messages)

            val response = firebaseEngine.generate(request)

            if (response.text.isBlank()){
                AiResult.Failure(AiError.EmptyResponse)
            }else{
                AiResult.Success(response.text)
            }


        }catch (e: CancellationException){
          throw e
        }
        catch (e: Exception) {
            AiResult.Failure(e.toAiError())

        }
    }

    suspend fun explainOpenRouterAi(messages: List<ChatMessage>): AiResult<String> {

        return try {
            val request = AiChatRequest(messages)

            val response = openRouterEngine.generate(request)

            if (response.text.isBlank()){
                AiResult.Failure(AiError.EmptyResponse)
            }else{
                AiResult.Success(response.text)
            }


        }catch (e: CancellationException){
          throw e
        }
        catch (e: AiProviderException) {
            AiResult.Failure(e.error)
        }
        catch (e: Exception) {
            AiResult.Failure(e.toAiError())

        }
    }

}


