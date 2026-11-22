package com.example.data

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sqrt

/**
 * Captures real microphone RMS amplitude when RECORD_AUDIO permission is granted
 * to drive live speaking wave indicators in Voice Rooms and Private Voice Calls.
 */
class VoiceAudioEngine {
    private val _amplitudeLevel = MutableStateFlow(0f)
    val amplitudeLevel: StateFlow<Float> = _amplitudeLevel.asStateFlow()

    private val _isCapturing = MutableStateFlow(false)
    val isCapturing: StateFlow<Boolean> = _isCapturing.asStateFlow()

    private var captureJob: Job? = null

    @SuppressLint("MissingPermission")
    fun startCapture(scope: CoroutineScope, hasPermission: Boolean) {
        stopCapture()
        if (!hasPermission) {
            _isCapturing.value = false
            _amplitudeLevel.value = 0f
            return
        }
        captureJob = scope.launch(Dispatchers.IO) {
            val sampleRate = 16000
            val minBuffer = AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            if (minBuffer <= 0) {
                _isCapturing.value = false
                return@launch
            }
            var recorder: AudioRecord? = null
            try {
                recorder = AudioRecord(
                    MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBuffer
                )
                if (recorder.state != AudioRecord.STATE_INITIALIZED) {
                    recorder.release()
                    _isCapturing.value = false
                    return@launch
                }
                recorder.startRecording()
                _isCapturing.value = true
                val buffer = ShortArray(minBuffer / 2)
                while (isActive) {
                    val read = recorder.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        var sum = 0.0
                        for (i in 0 until read) {
                            val s = buffer[i].toDouble()
                            sum += s * s
                        }
                        val rms = sqrt(sum / read)
                        val normalized = (rms / 6000.0).coerceIn(0.0, 1.0).toFloat()
                        _amplitudeLevel.value = normalized
                    }
                    delay(80L)
                }
            } catch (e: Exception) {
                Log.w("VoiceAudioEngine", "AudioRecord unavailable: ${e.message}")
            } finally {
                _isCapturing.value = false
                _amplitudeLevel.value = 0f
                try {
                    recorder?.stop()
                } catch (_: Exception) {
                }
                try {
                    recorder?.release()
                } catch (_: Exception) {
                }
            }
        }
    }

    fun stopCapture() {
        captureJob?.cancel()
        captureJob = null
        _isCapturing.value = false
        _amplitudeLevel.value = 0f
    }
}
