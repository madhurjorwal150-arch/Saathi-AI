package com.sathii.ai.model

enum class IntentType {
    CONVERSATION,
    OPEN_APP,
    PLAY_MUSIC_SPOTIFY,
    PLAY_YOUTUBE,
    SEARCH_YOUTUBE,
    SEARCH_WEB,
    SET_ALARM,
    SET_TIMER,
    TORCH_ON,
    TORCH_OFF,
    VOLUME_UP,
    VOLUME_DOWN,
    CREATE_NOTE,
    UNKNOWN
}

data class SathiIntent(
    val type: IntentType,
    val rawQuery: String,
    val target: String = "",
    val parameters: Map<String, String> = emptyMap(),
    val directReply: String = ""
)
