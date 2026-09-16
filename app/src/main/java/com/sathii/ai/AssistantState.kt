package com.sathii.ai

enum class AssistantState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING
}

data class ChatMessage(
    val id: Long = System.currentTimeMillis(),
    val sender: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
