package com.myraa.ultimate

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Base64
import androidx.core.content.ContextCompat
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

/**
 * Minimal client for Google's Gemini Live API (real-time voice-to-voice).
 * Streams 16kHz mic audio up, plays 24kHz model audio down, and routes any
 * "device_command" tool calls from Gemini into the existing CommandEngine so
 * all of CAPTAIN's device controls (apps, SMS, WhatsApp, alarms, flashlight,
 * etc.) work through natural conversation instead of fixed phrases.
 */
class GeminiLiveClient(
    private val context: Context,
    private val apiKey: String,
    private val commandEngine: CommandEngine,
    private val systemPersona: String,
    private val onState: (String) -> Unit
) {
    private var ws: WebSocket? = null
    private var recording = false
    private var recordThread: Thread? = null
    private var audioTrack: AudioTrack? = null

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .pingInterval(15, TimeUnit.SECONDS)
        .build()

    fun connect() {
        if (apiKey.isBlank()) {
            onState("ERROR: API key missing")
            return
        }
        onState("CONNECTING")
        val url = "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1beta.GenerativeService.BidiGenerateContent?key=$apiKey"
        val request = Request.Builder().url(url).build()
        ws = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                sendSetup(webSocket)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleServerMessage(text)
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                handleServerMessage(bytes.utf8())
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                onState("ERROR: ${t.message}")
                stopRecording()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                onState("DISCONNECTED")
                stopRecording()
            }
        })
    }

    fun disconnect() {
        stopRecording()
        try { ws?.close(1000, "user stopped") } catch (_: Exception) {}
        ws = null
        try { audioTrack?.stop(); audioTrack?.release() } catch (_: Exception) {}
        audioTrack = null
        onState("DISCONNECTED")
    }

    private fun sendSetup(webSocket: WebSocket) {
        val tool = JSONObject().apply {
            put("functionDeclarations", JSONArray().put(JSONObject().apply {
                put("name", "device_command")
                put(
                    "description",
                    "Execute a device control command on the user's Android phone: opening an app, " +
                        "sending SMS or WhatsApp, making a call, setting an alarm or reminder, controlling " +
                        "flashlight, volume, brightness, screen timeout, Do Not Disturb, WiFi/Bluetooth panel, " +
                        "media playback, checking battery or location, playing a video, or writing a note. " +
                        "Pass the user's request as a short natural-language instruction in Hindi/Urdu/English " +
                        "mix, written exactly the way the user would say it, e.g. 'flashlight on', " +
                        "'SMS bhejo Ali ko main aa raha hoon', 'PLAYit kholo', 'alarm 7 baje lagao'."
                )
                put("parameters", JSONObject().apply {
                    put("type", "OBJECT")
                    put("properties", JSONObject().apply {
                        put("command", JSONObject().apply {
                            put("type", "STRING")
                            put("description", "The natural language device command to execute.")
                        })
                    })
                    put("required", JSONArray().put("command"))
                })
            }))
        }
        val setup = JSONObject().apply {
            put("setup", JSONObject().apply {
                put("model", "models/gemini-2.5-flash-native-audio-preview-12-2025")
                put("generationConfig", JSONObject().apply {
                    put("responseModalities", JSONArray().put("AUDIO"))
                    put("speechConfig", JSONObject().apply {
                        put("voiceConfig", JSONObject().apply {
                            put("prebuiltVoiceConfig", JSONObject().apply {
                                put("voiceName", "Charon")
                            })
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().apply { put("text", systemPersona) }))
                })
                put("tools", JSONArray().put(tool))
            })
        }
        webSocket.send(setup.toString())
    }

    private fun handleServerMessage(text: String) {
        try {
            val json = JSONObject(text)

            if (json.has("setupComplete")) {
                onState("LISTENING")
                startRecording()
                return
            }

            json.optJSONObject("serverContent")?.let { sc ->
                sc.optJSONObject("modelTurn")?.optJSONArray("parts")?.let { parts ->
                    for (i in 0 until parts.length()) {
                        val part = parts.optJSONObject(i) ?: continue
                        val inline = part.optJSONObject("inlineData")
                        val data = inline?.optString("data")
                        if (!data.isNullOrBlank()) {
                            onState("SPEAKING")
                            playAudioChunk(data)
                        }
                    }
                }
                if (sc.optBoolean("interrupted", false)) {
                    try { audioTrack?.pause(); audioTrack?.flush(); audioTrack?.play() } catch (_: Exception) {}
                }
                if (sc.optBoolean("turnComplete", false)) {
                    onState("LISTENING")
                }
            }

            json.optJSONObject("toolCall")?.optJSONArray("functionCalls")?.let { calls ->
                val responses = JSONArray()
                for (i in 0 until calls.length()) {
                    val call = calls.optJSONObject(i) ?: continue
                    val name = call.optString("name")
                    val id = call.optString("id")
                    val args = call.optJSONObject("args")
                    val commandText = args?.optString("command") ?: ""
                    val result = if (name == "device_command" && commandText.isNotBlank())
                        commandEngine.execute(commandText)
                    else "Command samajh nahi aaya."
                    responses.put(JSONObject().apply {
                        put("id", id)
                        put("name", name)
                        put("response", JSONObject().apply { put("result", result) })
                    })
                }
                val toolResponse = JSONObject().apply {
                    put("toolResponse", JSONObject().apply { put("functionResponses", responses) })
                }
                ws?.send(toolResponse.toString())
            }
        } catch (_: Exception) {
            // Malformed or unrecognised frame from the server — ignore rather than crash.
        }
    }

    private fun playAudioChunk(base64Data: String) {
        try {
            if (audioTrack == null) {
                val bufSize = AudioTrack.getMinBufferSize(24000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
                audioTrack = AudioTrack(
                    AudioManager.STREAM_MUSIC, 24000, AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, bufSize.coerceAtLeast(4096), AudioTrack.MODE_STREAM
                )
                audioTrack?.play()
            }
            val bytes = Base64.decode(base64Data, Base64.NO_WRAP)
            audioTrack?.write(bytes, 0, bytes.size)
        } catch (_: Exception) {
        }
    }

    private fun startRecording() {
        if (recording) return
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            onState("ERROR: mic permission missing")
            return
        }
        recording = true
        recordThread = thread(name = "captain-live-mic") {
            val bufSize = AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
                .coerceAtLeast(4096)
            val recorder = try {
                AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, 16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufSize)
            } catch (e: Exception) {
                onState("ERROR: mic init failed")
                recording = false
                return@thread
            }
            try {
                recorder.startRecording()
                val buffer = ByteArray(bufSize)
                while (recording) {
                    val read = recorder.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        val chunk = if (read == buffer.size) buffer else buffer.copyOf(read)
                        val b64 = Base64.encodeToString(chunk, Base64.NO_WRAP)
                        val msg = JSONObject().apply {
                            put("realtimeInput", JSONObject().apply {
                                put("mediaChunks", JSONArray().put(JSONObject().apply {
                                    put("mimeType", "audio/pcm;rate=16000")
                                    put("data", b64)
                                }))
                            })
                        }
                        ws?.send(msg.toString())
                    }
                }
            } catch (_: Exception) {
            } finally {
                try { recorder.stop() } catch (_: Exception) {}
                recorder.release()
            }
        }
    }

    private fun stopRecording() {
        recording = false
        try { recordThread?.join(500) } catch (_: Exception) {}
        recordThread = null
    }
}
