package com.sathii.ai

enum class IntentType {
    CONVERSATION,
    OPEN_APP,
    OPEN_WEBSITE,
    YOUTUBE_SEARCH,
    YOUTUBE_PLAY,
    SPOTIFY_SEARCH,
    SPOTIFY_PLAY,
    TORCH_ON,
    TORCH_OFF,
    VOLUME_UP,
    VOLUME_DOWN,
    SET_TIMER,
    SET_ALARM,
    NOTE_CREATE,
    WEB_SEARCH
}

data class ParsedIntent(
    val type: IntentType,
    val target: String = "",
    val extraData: String = ""
)
