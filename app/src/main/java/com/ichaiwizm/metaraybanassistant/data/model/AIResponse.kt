package com.ichaiwizm.metaraybanassistant.data.model

/**
 * Represents a response from Claude AI API
 */
data class AIResponse(
    val id: String,
    val content: String,
    val model: String,
    val stopReason: String? = null,
    val usage: Usage? = null
)

data class Usage(
    val inputTokens: Int,
    val outputTokens: Int
)

/**
 * Request body for Claude API
 */
data class AIRequest(
    val model: String,
    val messages: List<Message>,
    val maxTokens: Int = 1024,
    val temperature: Double = 1.0,
    val system: String? = null
)

data class Message(
    val role: String,  // "user" or "assistant"
    val content: List<Content>
)

sealed class Content {
    data class Text(val type: String = "text", val text: String) : Content()
    data class Image(
        val type: String = "image",
        val source: ImageSource
    ) : Content()
}

data class ImageSource(
    val type: String = "base64",
    val mediaType: String,  // e.g., "image/jpeg"
    val data: String  // base64 encoded image
)
