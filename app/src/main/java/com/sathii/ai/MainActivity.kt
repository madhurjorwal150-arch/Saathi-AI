package com.sathii.ai

import android.Manifest
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.sathii.ai.model.ActionResult
import com.sathii.ai.model.IntentType

class MainActivity : ComponentActivity() {

    private lateinit var fullExecutor: FullActionExecutor
    private lateinit var intentClassifier: SathiIntentClassifier
    private lateinit var ttsManager: TtsManager
    private val notesList = mutableListOf<NoteItem>()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        if (audioGranted) {
            checkOverlayAndBatteryOptimizations()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fullExecutor = FullActionExecutor(this)
        intentClassifier = SathiIntentClassifier()
        ttsManager = TtsManager(this)

        setupLockscreenFlags()
        requestSystemPermissions()
        handleVoiceIntent(intent)

        setContent {
            SathiApp(
                onTriggerVoice = {
                    startListeningService()
                },
                onExecuteCommand = { command ->
                    executePipeline(command)
                },
                savedNotes = notesList
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleVoiceIntent(intent)
    }

    private fun setupLockscreenFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }

    private fun handleVoiceIntent(intent: Intent?) {
        val voiceCommand = intent?.getStringExtra("voice_command")
        if (!voiceCommand.isNullOrBlank()) {
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && keyguardManager.isKeyguardLocked) {
                keyguardManager.requestDismissKeyguard(this, object : KeyguardManager.KeyguardDismissCallback() {
                    override fun onDismissSucceeded() {
                        super.onDismissSucceeded()
                        executePipeline(voiceCommand)
                    }
                    override fun onDismissError() {
                        super.onDismissError()
                        executePipeline(voiceCommand)
                    }
                })
            } else {
                executePipeline(voiceCommand)
            }
        }
    }

    private fun executePipeline(command: String): ActionResult {
        val parsedIntent = intentClassifier.classify(command, "Madhur")

        // Agar note intent hai toh note save karein
        if (parsedIntent.type == IntentType.CREATE_NOTE) {
            notesList.add(NoteItem(System.currentTimeMillis(), "Voice Note", parsedIntent.target))
        }

        // Action execute karein
        val result = fullExecutor.execute(parsedIntent)

        // Bol kar batayein
        ttsManager.speak(result.message)

        return result
    }

    private fun requestSystemPermissions() {
        val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            checkOverlayAndBatteryOptimizations()
        } else {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun checkOverlayAndBatteryOptimizations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }
        startListeningService()
    }

    private fun startListeningService() {
        val serviceIntent = Intent(this, VoiceListeningService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsManager.shutdown()
    }
}
