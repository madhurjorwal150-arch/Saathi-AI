package com.sathii.ai

import com.sathii.ai.model.IntentType
import com.sathii.ai.model.SathiIntent
import java.util.Locale

class SathiIntentClassifier {

    fun classify(text: String, userName: String = "Madhur"): SathiIntent {
        val clean = text.lowercase(Locale.ROOT).trim()

        // 1. Torch / Flashlight Control
        if (clean.contains("torch on") || clean.contains("light jalao") || clean.contains("flashlight on") || clean.contains("torch chalao")) {
            return SathiIntent(IntentType.TORCH_ON, text, directReply = "Torch on kar di hai.")
        }
        if (clean.contains("torch off") || clean.contains("light band") || clean.contains("flashlight off") || clean.contains("torch bujhao")) {
            return SathiIntent(IntentType.TORCH_OFF, text, directReply = "Torch band kar di hai.")
        }

        // 2. Volume Control
        if (clean.contains("volume badhao") || clean.contains("volume up") || clean.contains("aawaz badhao")) {
            return SathiIntent(IntentType.VOLUME_UP, text, directReply = "Volume badha diya hai.")
        }
        if (clean.contains("volume kam") || clean.contains("volume down") || clean.contains("aawaz kam")) {
            return SathiIntent(IntentType.VOLUME_DOWN, text, directReply = "Volume kam kar diya hai.")
        }

        // 3. Spotify Music Playback
        if (clean.contains("spotify") || (clean.startsWith("play") && !clean.contains("youtube"))) {
            val song = clean.replace("play", "")
                .replace("on spotify", "")
                .replace("spotify par", "")
                .replace("spotify", "")
                .replace("gana", "")
                .replace("song", "")
                .trim()
            return SathiIntent(IntentType.PLAY_MUSIC_SPOTIFY, text, target = song, directReply = "$song Spotify par play kiya ja raha hai.")
        }

        // 4. YouTube Intent (Play vs Search)
        if (clean.contains("youtube")) {
            val query = clean.replace("youtube", "")
                .replace("par", "")
                .replace("search", "")
                .replace("play", "")
                .replace("chalao", "")
                .replace("video", "")
                .trim()

            return if (clean.contains("play") || clean.contains("chalao")) {
                SathiIntent(IntentType.PLAY_YOUTUBE, text, target = query, directReply = "$query YouTube par chala raha hoon.")
            } else {
                SathiIntent(IntentType.SEARCH_YOUTUBE, text, target = query, directReply = "$query YouTube par search kar raha hoon.")
            }
        }

        // 5. Timer & Alarm
        if (clean.contains("timer")) {
            val minutes = Regex("(\\d+)").find(clean)?.value?.toIntOrNull() ?: 5
            return SathiIntent(
                IntentType.SET_TIMER,
                text,
                target = minutes.toString(),
                parameters = mapOf("minutes" to minutes.toString()),
                directReply = "$minutes minute ka timer laga diya hai."
            )
        }
        if (clean.contains("alarm")) {
            val hour = Regex("(\\d+)").find(clean)?.value?.toIntOrNull() ?: 7
            return SathiIntent(
                IntentType.SET_ALARM,
                text,
                target = hour.toString(),
                parameters = mapOf("hour" to hour.toString(), "minutes" to "0"),
                directReply = "$hour baje ka alarm set kar diya hai."
            )
        }

        // 6. Notes
        if (clean.contains("note") && (clean.contains("banao") || clean.contains("likho") || clean.contains("save"))) {
            val note = text.replace(Regex("(?i)(note banao|note likho|save note|isko note kar do)"), "").trim()
            return SathiIntent(IntentType.CREATE_NOTE, text, target = note, directReply = "Note save kar liya hai: $note")
        }

        // 7. Dynamic App Launching
        if (clean.startsWith("open") || clean.startsWith("kholo") || clean.startsWith("launch") || clean.contains("khol do")) {
            val app = clean.replace("open", "")
                .replace("kholo", "")
                .replace("launch", "")
                .replace("khol do", "")
                .replace("the", "")
                .trim()
            return SathiIntent(IntentType.OPEN_APP, text, target = app, directReply = "$app open kiya ja raha hai.")
        }

        // 8. Explicit Web Search (Sirf jab user bole)
        if (clean.startsWith("search") || clean.contains("google par search") || clean.startsWith("google ")) {
            val q = clean.replace("search", "").replace("google par", "").replace("google", "").trim()
            return SathiIntent(IntentType.SEARCH_WEB, text, target = q, directReply = "Google par search kar raha hoon: $q")
        }

        // 9. Pure Conversation (Never Trigger Google Search)
        val convReply = when {
            clean.contains("i love you") -> "Aww ❤️ Main hamesha tumhara Sathi hoon bhai!"
            clean.contains("kaise ho") || clean.contains("how are you") -> "Main bilkul badiya hoon! Aap batao aaj phone mein kya kaam karein?"
            clean.contains("kya kar rahe ho") || clean.contains("what are you doing") -> "Bas aapke agle command ka wait kar raha hoon."
            clean.contains("kaun ho") || clean.contains("who are you") -> "Main Sathi AI hoon — aapka apna personal phone assistant!"
            clean.contains("namaste") || clean.contains("hello") || clean.contains("hey") -> "Namaste $userName! Bataiye main aapki kya help kar sakta hoon?"
            clean.contains("thank") || clean.contains("shukriya") -> "Arey isme shukriya kaisa bhai, ye toh mera farz hai! 😄"
            else -> "Ji bhai, main sun raha hoon. Agar koi app kholna ho, gana chalana ho ya phone control karna ho toh batayein."
        }

        return SathiIntent(IntentType.CONVERSATION, text, directReply = convReply)
    }
}
