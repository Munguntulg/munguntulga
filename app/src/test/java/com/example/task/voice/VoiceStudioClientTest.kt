package com.example.task.voice

import com.sun.net.httpserver.HttpServer
import java.io.IOException
import java.net.InetSocketAddress
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class VoiceStudioClientTest {
    private lateinit var server: HttpServer
    private var lastPath = ""
    private var lastBody = ""
    private var lastAuth: String? = null
    private var status = 200
    private var response = byteArrayOf(1, 2, 3)

    @Before
    fun startServer() {
        server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { exchange ->
            lastPath = exchange.requestURI.path
            lastAuth = exchange.requestHeaders.getFirst("Authorization")
            lastBody = exchange.requestBody.bufferedReader().readText()
            exchange.sendResponseHeaders(status, response.size.toLong())
            exchange.responseBody.use { it.write(response) }
        }
        server.start()
    }

    @After
    fun stopServer() = server.stop(0)

    private fun client(apiKey: String = "") =
        VoiceStudioClient("http://127.0.0.1:${server.address.port}/", apiKey)

    @Test
    fun synthesize_postsOpenAiSpeechRequest_andReturnsAudio() {
        val audio = client().synthesize("Nice work!", voice = "narrator", language = "en")

        assertArrayEquals(byteArrayOf(1, 2, 3), audio)
        assertEquals("/v1/audio/speech", lastPath)
        assertEquals(
            """{"model":"omnivoice","input":"Nice work!","voice":"narrator","language":"en","response_format":"mp3"}""",
            lastBody,
        )
        assertEquals(null, lastAuth)
    }

    @Test
    fun synthesize_sendsBearerKey_whenConfigured() {
        client(apiKey = "secret").synthesize("hi")

        assertEquals("Bearer secret", lastAuth)
    }

    @Test(expected = VoiceStudioException::class)
    fun synthesize_throws_onHttpError() {
        status = 422
        response = """{"detail":"bad voice"}""".toByteArray()

        client().synthesize("hi")
    }

    @Test(expected = IOException::class)
    fun synthesize_throws_whenBackendIsDown() {
        val port = server.address.port
        server.stop(0)

        VoiceStudioClient("http://127.0.0.1:$port", connectTimeoutMs = 500).synthesize("hi")
    }

    @Test(expected = IllegalArgumentException::class)
    fun synthesize_rejectsTextOverLimit() {
        client().synthesize("a".repeat(VoiceStudioClient.MAX_INPUT_LENGTH + 1))
    }

    @Test
    fun isAvailable_reflectsHealthEndpoint() {
        assertTrue(client().isAvailable())
        assertEquals("/health", lastPath)

        status = 503
        assertFalse(client().isAvailable())
    }

    @Test
    fun speechRequestJson_omitsLanguage_whenNull() {
        assertEquals(
            """{"model":"omnivoice","input":"x","voice":"default","response_format":"wav"}""",
            speechRequestJson("x", "default", null, "wav"),
        )
    }

    @Test
    fun jsonString_escapesSpecialCharacters() {
        assertEquals("\"a\\\"b\\\\c\\nd\\u0001\"", jsonString("a\"b\\c\nd\u0001"))
        assertEquals("\"Сайн байна уу\"", jsonString("Сайн байна уу"))
    }
}
