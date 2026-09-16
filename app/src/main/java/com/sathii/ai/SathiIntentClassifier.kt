package com.sathii.ai

class SathiIntentClassifier {

    fun classify(rawInput: String): ParsedIntent {
        val text = rawInput.trim().lowercase()

        // 1. CONVERSATION MODE: Casual/Emotional/Greeting queries
        if (text.matches(Regex(".*\\b(hi|hello|hey|kaise ho|kya hal|who are you|kon ho|tum kon ho|i love you|love you|kya kar rahi ho|madad|help)\\b.*"))) {
            return ParsedIntent(IntentType.CONVERSATION, target = rawInput)
        }

        // 2. ACTION MODE: Hardware & system triggers
        if (text.contains("torch on") || text.contains("flashlight on") || text.contains("torch jalao")) {
            return ParsedIntent(IntentType.TORCH_ON)
        }
        if (text.contains("torch off") || text.contains("flashlight off") || text.contains("torch band")) {
            return ParsedIntent(IntentType.TORCH_OFF)
        }
        if (text.contains("volume up") || text.contains("awaz badhao") || text.contains("volume badhao")) {
            return ParsedIntent(IntentType.VOLUME_UP)
        }
        if (text.contains("volume down") || text.contains("awaz kam") || text.contains("volume kam")) {
            return ParsedIntent(IntentType.VOLUME_DOWN)
        }

        // 3. ACTION MODE: Notes
        if (text.startsWith("note") || text.contains("likho") || text.contains("note banao")) {
            val content = text.replace("note banao", "").replace("likho", "").replace("note", "").trim()
            return ParsedIntent(IntentType.NOTE_CREATE, target = if (content.isBlank()) "Quick Note" else content)
        }

        // 4. ACTION MODE: Timer & Alarm
        if (text.contains("timer")) {
            val digits = Regex("\\d+").find(text)?.value ?: "5"
            return ParsedIntent(IntentType.SET_TIMER, target = digits)
        }
        if (text.contains("alarm")) {
            val digits = Regex("\\d+").find(text)?.value ?: "6"
            return ParsedIntent(IntentType.SET_ALARM, target = digits)
        }

        // 5. ACTION MODE: Media (Spotify / YouTube)
        if (text.contains("spotify")) {
            val query = text.replace("spotify", "").replace("play", "").replace("chalao", "").replace("search", "").trim()
            return if (text.contains("play") || text.contains("chalao")) {
                ParsedIntent(IntentType.SPOTIFY_PLAY, target = query)
            } else {
                ParsedIntent(IntentType.SPOTIFY_SEARCH, target = query)
            }
        }

        if (text.contains("youtube")) {
            val query = text.replace("youtube", "").replace("play", "").replace("chalao", "").replace("search", "").trim()
            return if (text.contains("play") || text.contains("chalao")) {
                ParsedIntent(IntentType.YOUTUBE_PLAY, target = query)
            } else {
                ParsedIntent(IntentType.YOUTUBE_SEARCH, target = query)
            }
        }

        // 6. ACTION MODE: Open App
        if (text.startsWith("open") || text.contains("kholo")) {
            val app = text.replace("open", "").replace("kholo", "").replace("app", "").trim()
            return ParsedIntent(IntentType.OPEN_APP, target = app)
        }

        // 7. INFORMATION MODE: Explicit Search
        if (text.startsWith("search") || text.startsWith("google") || text.contains("kya hai") || text.contains("kaise kare")) {
            val query = text.replace("search", "").replace("google", "").trim()
            return ParsedIntent(IntentType.WEB_SEARCH, target = query)
        }

        // Default Conversation fallback
        return ParsedIntent(IntentType.CONVERSATION, target = rawInput)
    }
}
