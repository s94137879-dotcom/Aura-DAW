package com.example.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max

/**
 * Microphone Audio Recording Engine for capturing live vocal or instrument takes.
 */
class AudioRecorder {
  private val sampleRate = 44100
  private val channelConfig = AudioFormat.CHANNEL_IN_MONO
  private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
  private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat).coerceAtLeast(2048)

  private var audioRecord: AudioRecord? = null
  private var recordJob: Job? = null
  private val scope = CoroutineScope(Dispatchers.IO)

  private val _isRecording = MutableStateFlow(false)
  val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

  private val _liveInputLevel = MutableStateFlow(0.0f)
  val liveInputLevel: StateFlow<Float> = _liveInputLevel.asStateFlow()

  private val recordedAmplitudes = mutableListOf<Float>()

  @SuppressLint("MissingPermission")
  fun startRecording(onLiveWaveformSample: (Float) -> Unit = {}): Boolean {
    if (_isRecording.value) return false
    try {
      audioRecord = AudioRecord(
        MediaRecorder.AudioSource.MIC,
        sampleRate,
        channelConfig,
        audioFormat,
        bufferSize
      )

      if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
        return false
      }

      audioRecord?.startRecording()
      _isRecording.value = true
      recordedAmplitudes.clear()

      recordJob = scope.launch {
        val audioData = ShortArray(512)
        while (isActive && _isRecording.value) {
          val readCount = audioRecord?.read(audioData, 0, audioData.size) ?: 0
          if (readCount > 0) {
            var maxVal = 0
            for (i in 0 until readCount) {
              val sample = abs(audioData[i].toInt())
              if (sample > maxVal) maxVal = sample
            }
            val normalized = (maxVal / 32768.0f).coerceIn(0f, 1f)
            _liveInputLevel.value = normalized
            recordedAmplitudes.add(normalized)
            onLiveWaveformSample(normalized)
          }
        }
      }
      return true
    } catch (e: Exception) {
      e.printStackTrace()
      _isRecording.value = false
      return false
    }
  }

  fun stopRecording(): List<Float> {
    _isRecording.value = false
    recordJob?.cancel()
    recordJob = null
    try {
      audioRecord?.stop()
      audioRecord?.release()
    } catch (e: Exception) {
      e.printStackTrace()
    }
    audioRecord = null
    _liveInputLevel.value = 0.0f

    // Return decimated waveform amplitudes for visual rendering (e.g. 64-128 points)
    if (recordedAmplitudes.isEmpty()) {
      return List(32) { (0.2f + kotlin.random.Random.nextFloat() * 0.6f) }
    }
    val targetSize = 64
    val step = max(1, recordedAmplitudes.size / targetSize)
    return recordedAmplitudes.filterIndexed { index, _ -> index % step == 0 }.take(targetSize)
  }
}
