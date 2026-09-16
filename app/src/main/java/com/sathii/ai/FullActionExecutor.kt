package com.sathii.ai

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.provider.AlarmClock

class FullActionExecutor(private val context: Context) {

    fun execute(intent: ParsedIntent): ActionResult {
        return when (intent.type) {
            IntentType.CONVERSATION -> handleConversation(intent.target)
            IntentType.TORCH_ON -> toggleTorch(true)
            IntentType.TORCH_OFF -> toggleTorch(false)
            IntentType.VOLUME_UP -> adjustVolume(AudioManager.ADJUST_RAISE)
            IntentType.VOLUME_DOWN -> adjustVolume(AudioManager.ADJUST_LOWER)
            IntentType.OPEN_APP -> openApplication(intent.target)
            IntentType.SET_TIMER -> setTimer(intent.target.toIntOrNull() ?: 5)
            IntentType.SET_ALARM -> setAlarm(intent.target.toIntOrNull() ?: 6)
            IntentType.YOUTUBE_SEARCH -> searchYouTube(intent.target)
            IntentType.YOUTUBE_PLAY -> playYouTube(intent.target)
            IntentType.SPOTIFY_SEARCH -> searchSpotify(intent.target)
            IntentType.SPOTIFY_PLAY -> playSpotify(intent.target)
            IntentType.WEB_SEARCH -> executeWebSearch(intent.target)
            IntentType.NOTE_CREATE,
            IntentType.OPEN_WEBSITE -> ActionResult(true, "NOOP", "")
        }
    }

    private fun handleConversation(input: String): ActionResult {
        val lower = input.lowercase()
        val reply = when {
            lower.contains("love you") -> "Main hamesha aapke sath hoon, Madhur!"
            lower.contains("kaise ho") -> "Main bilkul badiya hoon! Aap batayein main kya madad karu?"
            lower.contains("who are you") || lower.contains("kon ho") -> "Main Sathi hoon, aapka native personal AI assistant."
            else -> "Ji, main sun raha hoon. Hukum kijiye kya karna hai?"
        }
        return ActionResult(true, "CONVERSATION", reply)
    }

    private fun toggleTorch(enable: Boolean): ActionResult {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, enable)
            ActionResult(true, "TORCH", if (enable) "Torch on kar di hai" else "Torch band kar di hai")
        } catch (e: Exception) {
            ActionResult(false, "TORCH", "Torch access nahi ho payi: ${e.message}")
        }
    }

    private fun adjustVolume(direction: Int): ActionResult {
        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI)
            ActionResult(true, "VOLUME", "Volume adjust kar diya hai")
        } catch (e: Exception) {
            ActionResult(false, "VOLUME", "Volume control me error aaya")
        }
    }

    private fun openApplication(appName: String): ActionResult {
        val pm = context.packageManager
        val packages = pm.getInstalledPackages(PackageManager.GET_META_DATA)
        for (pkg in packages) {
            val label = pkg.applicationInfo.loadLabel(pm).toString().lowercase()
            if (label.contains(appName.lowercase())) {
                val launchIntent = pm.getLaunchIntentForPackage(pkg.packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    return ActionResult(true, "OPEN_APP", "$label app khol diya hai")
                }
            }
        }
        return ActionResult(false, "OPEN_APP", "$appName app device me nahi mila")
    }

    private fun setTimer(minutes: Int): ActionResult {
        val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
            putExtra(AlarmClock.EXTRA_LENGTH, minutes * 60)
            putExtra(AlarmClock.EXTRA_MESSAGE, "Sathi Timer")
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
            ActionResult(true, "TIMER", "$minutes minute ka timer set karne clock app open kiya")
        } else {
            ActionResult(false, "TIMER", "Device par clock app support nahi mila")
        }
    }

    private fun setAlarm(hour: Int): ActionResult {
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, 0)
            putExtra(AlarmClock.EXTRA_MESSAGE, "Sathi Alarm")
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
            ActionResult(true, "ALARM", "$hour baje ka alarm set karne clock open kiya")
        } else {
            ActionResult(false, "ALARM", "Alarm support uplabdh nahi hai")
        }
    }

    private fun searchYouTube(query: String): ActionResult {
        val intent = Intent(Intent.ACTION_SEARCH).apply {
            setPackage("com.google.android.youtube")
            putExtra("query", query)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
            ActionResult(true, "YOUTUBE_SEARCH", "YouTube par $query search kiya")
        } else {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=$query")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webIntent)
            ActionResult(true, "YOUTUBE_SEARCH", "Browser me YouTube search khol diya")
        }
    }

    private fun playYouTube(query: String): ActionResult {
        searchYouTube(query)
        return ActionResult(true, "YOUTUBE_PLAY", "YouTube khol diya hai, video play karein")
    }

    private fun searchSpotify(query: String): ActionResult {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("spotify:search:$query")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            ActionResult(true, "SPOTIFY_SEARCH", "Spotify par $query dhoondh rahe hain")
        } catch (e: Exception) {
            ActionResult(false, "SPOTIFY_SEARCH", "Spotify app phone me installed nahi hai")
        }
    }

    private fun playSpotify(query: String): ActionResult {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("spotify:search:$query")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            ActionResult(true, "SPOTIFY_PLAY", "Spotify me $query khol diya hai")
        } catch (e: Exception) {
            ActionResult(false, "SPOTIFY_PLAY", "Spotify app nahi mila")
        }
    }

    private fun executeWebSearch(query: String): ActionResult {
        val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
            putExtra(SearchManager.QUERY, query)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            ActionResult(true, "WEB_SEARCH", "$query ke liye search open kiya")
        } catch (e: Exception) {
            ActionResult(false, "WEB_SEARCH", "Search open nahi ho paya")
        }
    }
}
