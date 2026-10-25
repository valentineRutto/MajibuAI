package com.valentinerutto.majibuai.data.ai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
data class ChatCompletionResponse(
    val id: String,
    val created: Long,
    val model: String,
    val `object`: String,          // "chat.completion"
    val choices: List<Choice>,
    @SerialName("system_fingerprint") val systemFingerprint: String? = null,
    val usage: Usage? = null,
    val provider: String? = null,
    @SerialName("openrouter_metadata")
    val openrouterMetadata: JsonObject? = null
)

@Serializable
data class Choice(
    val index: Int,
    val message: AssistantMessage,  // role + content + reasoning/tool_calls
    @SerialName("finish_reason") val finishReason: String? = null
)

@Serializable
data class AssistantMessage(
    val role: String,
    val content: String? = null,
    val reasoning: String? = null,
    @SerialName("reasoning_details") val reasoningDetails: JsonArray? = null,
    @SerialName("tool_calls") val toolCalls: JsonArray? = null
)
@Serializable
data class Usage(
    @SerialName("prompt_tokens") val promptTokens: Int,
    @SerialName("completion_tokens") val completionTokens: Int,  // required
    @SerialName("total_tokens") val totalTokens: Int,            // required
    @SerialName("prompt_tokens_details") val promptTokensDetails: PromptDetails? = null,
    @SerialName("completion_tokens_details") val completionTokensDetails: CompletionDetails? = null,
    val cost: Double? = null,                    // USD, nullable
    @SerialName("cost_details") val costDetails: CostDetails? = null,
    @SerialName("is_byok") val isByok: Boolean? = null,
    @SerialName("server_tool_use_details")
    val serverToolUseDetails: JsonElement? = null
)

@Serializable
data class PromptDetails(
    @SerialName("cached_tokens") val cachedTokens: Int? = null,
    @SerialName("cache_write_tokens") val cacheWriteTokens: Int? = null,
    @SerialName("audio_tokens") val audioTokens: Int? = null,
    @SerialName("video_tokens") val videoTokens: Int? = null
)


@Serializable
data class CompletionDetails(
    @SerialName("reasoning_tokens") val reasoningTokens: Int? = null,
    @SerialName("audio_tokens") val audioTokens: Int? = null,
    @SerialName("accepted_prediction_tokens") val acceptedPredictionTokens: Int? = null,
    @SerialName("rejected_prediction_tokens") val rejectedPredictionTokens: Int? = null
)

@Serializable
data class CostDetails(
    @SerialName("upstream_inference_cost") val upstreamInferenceCost: Double? = null,
    @SerialName("upstream_inference_prompt_cost") val upstreamInferencePromptCost: Double? = null,
    @SerialName("upstream_inference_completions_cost") val upstreamInferenceCompletionsCost: Double? = null,
    @SerialName("server_tool_cost") val serverToolCost: Double? = null
)
