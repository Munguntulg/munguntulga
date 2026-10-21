package com.example.task.voice

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Minimal client for a VoiceStudio backend (https://github.com/debpalash/VoiceStudio).
 *
 * Uses its OpenAI-compatible `POST /v1/audio/speech` endpoint, so it also works
 * against any other server that speaks that API. Plain JVM code, no Android
 * dependencies, so it can be unit tested on the host.
 */
class VoiceStudioClient(
    baseUrl: String,
    private val apiKey: String = "",
    private val connectTimeoutMs: Int = 3_000,
    private val readTimeoutMs: Int = 120_000,
) {
    private val baseUrl = baseUrl.trimEnd('/')

    /** True when the backend answers `GET /health`. Never throws. */
    fun isAvailable(): Boolean = try {
        val connection = open("/health")
        try {
            connection.responseCode in 200..299
        } finally {
            connection.disconnect()
        }
    } catch (e: IOException) {
        false
    }

    /**
     * Synthesizes [text] and returns the encoded audio bytes.
     *
     * @param voice a VoiceStudio voice profile id or name, or an OpenAI voice alias.
     * @param language ISO 639-1 code; null lets the voice's saved language apply.
     * @throws IOException when the backend is unreachable or rejects the request.
     */
    fun synthesize(
        text: String,
        voice: String = "default",
        language: String? = null,
        format: String = "mp3",
    ): ByteArray {
        require(text.isNotBlank()) { "text must not be blank" }
        require(text.length <= MAX_INPUT_LENGTH) { "text exceeds $MAX_INPUT_LENGTH characters" }

        val connection = open("/v1/audio/speech")
        try {
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.outputStream.use {
                it.write(speechRequestJson(text, voice, language, format).toByteArray(Charsets.UTF_8))
            }

            val code = connection.responseCode
            if (code !in 200..299) {
                val detail = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                throw VoiceStudioException("VoiceStudio returned HTTP $code: ${detail.take(200)}")
            }
            return connection.inputStream.use { it.readBytes() }
        } finally {
            connection.disconnect()
        }
    }

    private fun open(path: String): HttpURLConnection =
        (URL(baseUrl + path).openConnection() as HttpURLConnection).apply {
            connectTimeout = connectTimeoutMs
            readTimeout = readTimeoutMs
            if (apiKey.isNotEmpty()) setRequestProperty("Authorization", "Bearer $apiKey")
        }

    companion object {
        /** VoiceStudio rejects longer `input` values. */
        const val MAX_INPUT_LENGTH = 4096
    }
}

class VoiceStudioException(message: String) : IOException(message)

internal fun speechRequestJson(text: String, voice: String, language: String?, format: String): String =
    buildString {
        append("{\"model\":\"omnivoice\"")
        append(",\"input\":").append(jsonString(text))
        append(",\"voice\":").append(jsonString(voice))
        if (language != null) append(",\"language\":").append(jsonString(language))
        append(",\"response_format\":").append(jsonString(format))
        append('}')
    }

internal fun jsonString(value: String): String = buildString {
    append('"')
    for (c in value) {
        when (c) {
            '"' -> append("\\\"")
            '\\' -> append("\\\\")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            else -> if (c < ' ') append("\\u%04x".format(c.code)) else append(c)
        }
    }
    append('"')
}
