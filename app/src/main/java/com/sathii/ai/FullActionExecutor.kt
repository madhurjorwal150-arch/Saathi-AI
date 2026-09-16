package com.sathii.ai

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import com.sathii.ai.model.ActionResult
import com.sathii.ai.model.IntentType
import com.sathii.ai.model.SathiIntent

class FullActionExecutor(private val context: Context) {

    fun execute(intent: SathiIntent): ActionResult {
        return when (intent.type) {
            IntentType.TORCH_ON -> setTorch(true)
            IntentType.TORCH_OFF -> setTorch(false)
            IntentType.VOLUME_UP -> adjustVolume(true)
            IntentType.VOLUME_DOWN -> adjustVolume(false)
            IntentType.SET_TIMER -> setTimer(intent.parameters["minutes"]?.toIntOrNull() ?: 5)
            IntentType.SET_ALARM -> setAlarm(intent.parameters["hour"]?.toIntOrNull() ?: 7, intent.parameters["minutes"]?.toIntOrNull() ?: 0)
            IntentType.PLAY_MUSIC_SPOTIFY -> playSpotify(intent.target)
            IntentType.PLAY_YOUTUBE -> playYouTube(intent.target)
            IntentType.SEARCH_YOUTUBE -> searchYouTube(intent.target)
            IntentType.OPEN_APP -> openApp(intent.target)
            IntentType.SEARCH_WEB -> searchWeb(intent.target)
            IntentType.CONVERSATION, IntentType.CREATE_NOTE -> ActionResult(true, intent.type.name, intent.directReply)
            IntentType.UNKNOWN -> ActionResult(false, "UNKNOWN", "Samajh nahi aaya bhai, kripya dubara kahein.")
        }
    }

    private fun setTorch(enable: Boolean): ActionResult {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, enable)
            ActionResult(true, "TORCH", if (enable) "Torch on ho gayi." else "Torch off ho gayi.")
        } catch (e: Exception) {
            ActionResult(false, "TORCH", "Torch toggle nahi ho payi: ${e.localizedMessage}")
        }
    }

    private fun adjustVolume(increase: Boolean): ActionResult {
        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val direction = if (increase) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI)
            ActionResult(true, "VOLUME", if (increase) "Volume badha diya." else "Volume kam kar diya.")
        } catch (e: Exception) {
            ActionResult(false, "VOLUME", "Volume adjust nahi ho paya.")
        }
    }

    private fun setTimer(minutes: Int): ActionResult {
        return try {
            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_LENGTH, minutes * 60)
                putExtra(AlarmClock.EXTRA_MESSAGE, "Sathi Timer")
                putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult(true, "TIMER", "$minutes minute ka timer start kar diya.")
        } catch (e: Exception) {
            ActionResult(false, "TIMER", "Timer set karne ka intent fail ho gaya.")
        }
    }

    private fun setAlarm(hour: Int, minutes: Int): ActionResult {
        return try {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minutes)
                putExtra(AlarmClock.EXTRA_MESSAGE, "Sathi Alarm")
                putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult(true, "ALARM", "$hour baje ka alarm set kar diya.")
        } catch (e: Exception) {
            ActionResult(false, "ALARM", "Alarm set karne me dikkat aayi.")
        }
    }

    private fun playSpotify(query: String): ActionResult {
        return try {
            val uri = Uri.parse("spotify:search:" + Uri.encode(query))
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.spotify.music")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult(true, "SPOTIFY", "Spotify par $query search & play request bhej di.")
        } catch (e: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://open.spotify.com/search/" + Uri.encode(query))).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
            ActionResult(true, "SPOTIFY", "Spotify app nahi mila, browser me open kiya.")
        }
    }

    private fun playYouTube(query: String): ActionResult {
        return try {
            val intent = Intent(Intent.ACTION_SEARCH).apply {
                setPackage("com.google.android.youtube")
                putExtra("query", query)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult(true, "YOUTUBE_PLAY", "YouTube par '$query' play karne ke liye open kar diya.")
        } catch (e: Exception) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=" + Uri.encode(query))).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult(true, "YOUTUBE_PLAY", "YouTube browser me open kar diya.")
        }
    }

    private fun searchYouTube(query: String): ActionResult {
        return playYouTube(query)
    }

    private fun openApp(appName: String): ActionResult {
        val clean = appName.lowercase().trim()
        val pm = context.packageManager

        if (clean.contains("camera")) {
            val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return ActionResult(true, "OPEN_APP", "Camera open ho gaya.")
        }

        if (clean.contains("settings")) {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return ActionResult(true, "OPEN_APP", "Settings open ho gayi.")
        }

        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        for (app in installedApps) {
            val label = pm.getApplicationLabel(app).toString().lowercase()
            if (label.contains(clean) || app.packageName.lowercase().contains(clean)) {
                val launchIntent = pm.getLaunchIntentForPackage(app.packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                    )
                    context.startActivity(launchIntent)
                    return ActionResult(true, "OPEN_APP", "$label app khol diya.")
                }
            }
        }
        return ActionResult(false, "OPEN_APP", "Aapke phone me '$appName' app nahi mila.")
    }

    private fun searchWeb(query: String): ActionResult {
        return try {
            val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra(SearchManager.QUERY, query)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult(true, "WEB_SEARCH", "Google par search kar diya: $query")
        } catch (e: Exception) {
            ActionResult(false, "WEB_SEARCH", "Browser open nahi ho saka.")
        }
    }
}
