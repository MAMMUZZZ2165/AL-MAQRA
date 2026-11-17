package com.example.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sqrt

class LiveMicAudioEngine {
  private val _amplitude = MutableStateFlow(0f)
  val amplitude: StateFlow<Float> = _amplitude.asStateFlow()

  private val _isCapturing = MutableStateFlow(false)
  val isCapturing: StateFlow<Boolean> = _isCapturing.asStateFlow()

  private var audioRecord: AudioRecord? = null
  private var captureJob: Job? = null

  @SuppressLint("MissingPermission")
  fun startCapture(scope: CoroutineScope) {
    if (_isCapturing.value) return

    val sampleRate = 16000
    val minBuffer = AudioRecord.getMinBufferSize(
      sampleRate,
      AudioFormat.CHANNEL_IN_MONO,
      AudioFormat.ENCODING_PCM_16BIT,
    )
    if (minBuffer <= 0) {
      Log.w("LiveMicAudio", "AudioRecord unavailable on this hardware configuration")
      _isCapturing.value = true
      return
    }

    try {
      val recorder = AudioRecord(
        MediaRecorder.AudioSource.MIC,
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT,
        minBuffer * 2,
      )
      if (recorder.state != AudioRecord.STATE_INITIALIZED) {
        recorder.release()
        Log.w("LiveMicAudio", "AudioRecord failed to initialize")
        return
      }
      audioRecord = recorder
      recorder.startRecording()
      _isCapturing.value = true

      captureJob = scope.launch(Dispatchers.IO) {
        val buffer = ShortArray(minBuffer / 2)
        while (isActive && _isCapturing.value) {
          val read = recorder.read(buffer, 0, buffer.size)
          if (read > 0) {
            var sumSq = 0.0
            for (i in 0 until read) {
              val sample = buffer[i].toDouble()
              sumSq += sample * sample
            }
            val rms = sqrt(sumSq / read)
            val normalized = (rms / 6500.0).toFloat().coerceIn(0.05f, 1f)
            _amplitude.value = normalized
          }
        }
      }
    } catch (e: Exception) {
      Log.w("LiveMicAudio", "Could not start mic capture: ${e.message}")
      stopCapture()
    }
  }

  fun stopCapture() {
    _isCapturing.value = false
    captureJob?.cancel()
    captureJob = null
    try {
      audioRecord?.stop()
    } catch (_: Exception) {
    }
    try {
      audioRecord?.release()
    } catch (_: Exception) {
    }
    audioRecord = null
    _amplitude.value = 0f
  }
}
