package com.sathii.ai

import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class TtsManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    companion object {
        const val ACTION_TTS_STATE_CHANGED = "com.sathii.ai.TTS_STATE_CHANGED"
        const val EXTRA_IS_SPEAKING = "is_speaking"
    }

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("hi", "IN"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.language = Locale.ENGLISH
            }
            tts?.setSpeechRate(1.0f)
            tts?.setPitch(1.0f)

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    broadcastTtsState(true)
                }

                override fun onDone(utteranceId: String?) {
                    broadcastTtsState(false)
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    broadcastTtsState(false)
                }
            })

            isInitialized = true
        }
    }

    private fun broadcastTtsState(isSpeaking: Boolean) {
        val intent = Intent(ACTION_TTS_STATE_CHANGED).apply {
            putExtra(EXTRA_IS_SPEAKING, isSpeaking)
            setPackage(context.packageName)
        }
        context.sendBroadcast(intent)
    }

    fun speak(text: String) {
        if (!isInitialized) return
        val utteranceId = "SathiUtterance_${System.currentTimeMillis()}"
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        tts?.stop()
        broadcastTtsState(false)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
