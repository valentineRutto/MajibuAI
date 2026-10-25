package com.valentinerutto.majibuai.data.ai

import com.valentinerutto.majibuai.data.ai.models.AiChatRequest
import com.valentinerutto.majibuai.data.ai.models.AiError
import com.valentinerutto.majibuai.data.ai.models.AiProviderException
import com.valentinerutto.majibuai.data.ai.models.AiResult
import com.valentinerutto.majibuai.data.ai.models.ChatMessage
import com.valentinerutto.majibuai.data.ai.models.toAiError
import com.valentinerutto.majibuai.data.ai.openrouter.OpenRouterEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlin.coroutines.cancellation.CancellationException

class AiRepository(private val firebaseEngine: FirebaseGeminiAIEngine, private val openRouterEngine: OpenRouterEngine) {



    suspend fun explain(messages: List<ChatMessage>, provider: AiProvider): Flow<AiResult<String>> =
        when (provider) {
            AiProvider.FIREBASE_AI -> explainStreamFirebaseAi(messages)
            AiProvider.OPENROUTER -> explainOpenRouterAi(messages)
            AiProvider.ON_DEVICE -> AiResult.Failure(AiError.ProviderNotConfigured)
        } as Flow<AiResult<String>>

    suspend fun explainStreamFirebaseAi(messages: List<ChatMessage>): Flow<AiResult<String>> = flow{

            val request = AiChatRequest(messages)
        var hasContent = false

             firebaseEngine.streamChat(request).collect { response ->
                 if (response.text.isNotBlank()){
                     emit(AiResult.Success(response.text))
                     hasContent = true
                 }else{
                     emit(AiResult.Failure(AiError.EmptyResponse))
                 }
             }


        if (!hasContent) {
            emit(AiResult.Failure(AiError.EmptyResponse))
        }


        }.catch{e ->
        if (e is CancellationException) throw e
        emit(AiResult.Failure(AiError.Unknown(e.message.orEmpty())))

    }


    }

    suspend fun explainOpenRouterAi(messages: List<ChatMessage>):Flow<AiResult<String>> = flow {
emit(AiResult.Success("OpenRouterAI"))
//        return try {
//            val request = AiChatRequest(messages)
//
//            val response = openRouterEngine.generate(request)
//
//            if (response.text.isBlank()){
//                AiResult.Failure(AiError.EmptyResponse)
//            }else{
//                AiResult.Success(response.text)
//            }
//
//
//        }catch (e: CancellationException){
//          throw e
//        }
//        catch (e: AiProviderException) {
//            AiResult.Failure(e.error)
//        }
//        catch (e: Exception) {
//            AiResult.Failure(e.toAiError())
//
//        }
//    }

}


