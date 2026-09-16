package com.sathii.ai

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.sathii.ai.data.NoteEntity
import com.sathii.ai.data.SathiDatabase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var ttsManager: TtsManager
    private lateinit var actionExecutor: FullActionExecutor
    private lateinit var intentClassifier: SathiIntentClassifier
    private lateinit var database: SathiDatabase

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val recordAudioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        if (recordAudioGranted) {
            startVoiceService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupLockscreenFlags()

        database = SathiDatabase.getDatabase(this)
        ttsManager = TtsManager(this)
        actionExecutor = FullActionExecutor(this)
        intentClassifier = SathiIntentClassifier()

        val notesDao = database.noteDao()

        setContent {
            SathiUI(
                onVoiceTrigger = { startVoiceService() },
                onExecuteText = { text -> handleCommand(text) },
                notesDao = notesDao,
                onSaveNote = { title, content ->
                    lifecycleScope.launch {
                        notesDao.insertNote(NoteEntity(title = title, content = content))
                    }
                },
                onDeleteNote = { note ->
                    lifecycleScope.launch {
                        notesDao.deleteNote(note)
                    }
                }
            )
        }

        checkAndRequestPermissions()
        handleIncomingIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleIncomingIntent(it) }
    }

    private fun handleIncomingIntent(intent: Intent) {
        val voiceCommand = intent.getStringExtra("voice_command")
        if (!voiceCommand.isNullOrBlank()) {
            handleCommand(voiceCommand)
        }
    }

    private fun handleCommand(command: String) {
        val intentResult = intentClassifier.classify(command)
        
        when (intentResult.type) {
            IntentType.NOTE_CREATE -> {
                lifecycleScope.launch {
                    database.noteDao().insertNote(
                        NoteEntity(
                            title = "Voice Note",
                            content = intentResult.target
                        )
                    )
                }
                ttsManager.speak("Note save kar liya hai: ${intentResult.target}")
            }
            else -> {
                val actionResult = actionExecutor.execute(intentResult)
                ttsManager.speak(actionResult.message)
            }
        }
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
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            permissionLauncher.launch(missing.toTypedArray())
        } else {
            startVoiceService()
        }
    }

    private fun startVoiceService() {
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
