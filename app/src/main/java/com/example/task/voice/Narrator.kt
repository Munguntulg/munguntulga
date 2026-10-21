package com.example.task.voice

import android.content.Context
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import java.io.File
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Reads text aloud. Prefers a VoiceStudio backend (cloned or designed voices,
 * 646 languages) and falls back to the device's built-in text-to-speech when
 * the backend is offline.
 */
class Narrator(
    context: Context,
    private val client: VoiceStudioClient,
    private val voice: String,
) {
    enum class Engine { VoiceStudio, Device }

    private val cacheFile = File(context.cacheDir, "narration.mp3")
    private var player: MediaPlayer? = null
    private var deviceReady = false
    private var pendingDeviceText: String? = null
    private val deviceTts = TextToSpeech(context.applicationContext) { status ->
        deviceReady = status == TextToSpeech.SUCCESS
        pendingDeviceText?.let { if (deviceReady) speakOnDevice(it) }
        pendingDeviceText = null
    }

    /** Speaks [text] and reports which engine was used. Call from the main thread. */
    suspend fun speak(text: String, language: String? = null): Engine {
        stop()
        val audio = withContext(Dispatchers.IO) {
            try {
                client.synthesize(text, voice, language).also { cacheFile.writeBytes(it) }
            } catch (e: IOException) {
                null
            }
        }
        if (audio != null && play(cacheFile)) return Engine.VoiceStudio

        if (deviceReady) speakOnDevice(text) else pendingDeviceText = text
        return Engine.Device
    }

    fun stop() {
        player?.release()
        player = null
        if (deviceReady) deviceTts.stop()
    }

    fun release() {
        stop()
        deviceTts.shutdown()
    }

    private fun play(file: File): Boolean = try {
        player = MediaPlayer().apply {
            setDataSource(file.path)
            setOnCompletionListener { stop() }
            prepare()
            start()
        }
        true
    } catch (e: IOException) {
        stop()
        false
    }

    private fun speakOnDevice(text: String) {
        deviceTts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "narration")
    }
}
