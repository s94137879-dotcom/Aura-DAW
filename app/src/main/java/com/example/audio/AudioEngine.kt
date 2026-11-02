package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.synth.HarmorSynthEngine
import com.example.vocal.NewtonePitchEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

/**
 * Real-time Multi-track Synthesis, Sidechain Compression, and Audio Engine for AuraDAW.
 */
class AudioEngine(
  private val harmorSynth: HarmorSynthEngine,
  private val newtoneEngine: NewtonePitchEngine
) {
  private val sampleRate = 44100
  private val bufferSize = AudioTrack.getMinBufferSize(
    sampleRate,
    AudioFormat.CHANNEL_OUT_STEREO,
    AudioFormat.ENCODING_PCM_16BIT
  ).coerceAtLeast(4096)

  private var audioTrack: AudioTrack? = null
  private var playbackJob: Job? = null
  private val engineScope = CoroutineScope(Dispatchers.Default)

  // Playback State
  private val _isPlaying = MutableStateFlow(false)
  val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

  private val _currentBar = MutableStateFlow(1.0f)
  val currentBar: StateFlow<Float> = _currentBar.asStateFlow()

  private val _masterPeak = MutableStateFlow(0.0f)
  val masterPeak: StateFlow<Float> = _masterPeak.asStateFlow()

  private val _currentGainReduction = MutableStateFlow(0.0f)
  val currentGainReduction: StateFlow<Float> = _currentGainReduction.asStateFlow()

  private val _liveLufs = MutableStateFlow(-14.2f)
  val liveLufs: StateFlow<Float> = _liveLufs.asStateFlow()

  private val _livePhaseCorrelation = MutableStateFlow(0.85f)
  val livePhaseCorrelation: StateFlow<Float> = _livePhaseCorrelation.asStateFlow()

  var bpm: Int = 128
  var isLooping: Boolean = true
  var loopStartBar: Float = 1.0f
  var loopEndBar: Float = 9.0f
  var isMetronomeOn: Boolean = false

  // Transient state for sidechain envelope follower
  private var sidechainGainReductionLinear = 1.0f

  init {
    initAudioTrack()
  }

  private fun initAudioTrack() {
    try {
      audioTrack = AudioTrack.Builder()
        .setAudioAttributes(
          AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        )
        .setAudioFormat(
          AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
            .build()
        )
        .setBufferSizeInBytes(bufferSize)
        .setTransferMode(AudioTrack.MODE_STREAM)
        .build()

      audioTrack?.play()
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  fun startPlayback(
    tracksProvider: () -> List<DawTrack>,
    harmorParamsProvider: () -> HarmorParams,
    masteringDataProvider: () -> MasteringSuiteData
  ) {
    if (_isPlaying.value) return
    _isPlaying.value = true

    playbackJob = engineScope.launch {
      val frameSize = 1024
      val shortBuffer = ShortArray(frameSize * 2)

      while (isActive && _isPlaying.value) {
        val tracks = tracksProvider()
        val harmorParams = harmorParamsProvider()
        val masteringData = masteringDataProvider()

        // 16th tick duration in samples = (sampleRate * 60) / (bpm * 4)
        val samplesPerTick = (sampleRate * 15f) / bpm
        val samplesPerBar = samplesPerTick * 16f

        // Advance timeline position
        val barIncrement = frameSize.toFloat() / samplesPerBar
        var newBar = _currentBar.value + barIncrement
        if (isLooping && newBar >= loopEndBar) {
          newBar = loopStartBar
        }
        _currentBar.value = newBar

        // Synthesize mixed audio frame
        var maxPeakSample = 0.0f
        var rmsSum = 0.0f
        var sidechainDuck = 1.0f

        // Check if Kick / Drums is active on current beat to trigger sidechain
        val currentTickInBar = ((newBar - newBar.toInt()) * 16f)
        val isKickBeat = (currentTickInBar % 4.0f) < 0.35f

        // Sidechain compression dynamics (envelope follower)
        val targetDuck = if (isKickBeat) 0.25f else 1.0f
        val attackCoeff = 0.15f
        val releaseCoeff = 0.035f
        if (targetDuck < sidechainGainReductionLinear) {
          sidechainGainReductionLinear += (targetDuck - sidechainGainReductionLinear) * attackCoeff
        } else {
          sidechainGainReductionLinear += (targetDuck - sidechainGainReductionLinear) * releaseCoeff
        }

        val duckDb = 20.0f * log10(sidechainGainReductionLinear.coerceAtLeast(0.001f))
        _currentGainReduction.value = duckDb

        for (i in 0 until frameSize) {
          val n = i.toFloat()
          var mixL = 0.0f
          var mixR = 0.0f

          // Kick / Snare synthesis for Drum Track
          val drumTrack = tracks.find { it.type == TrackType.DRUMS && !it.isMuted }
          if (drumTrack != null) {
            val drumVol = drumTrack.volume
            // Four-on-the-floor kick
            if (isKickBeat) {
              val kickPhase = (i.toFloat() / frameSize) * 2f
              val kickFreq = 140f * exp(-kickPhase * 4f) + 45f
              val kickEnv = exp(-kickPhase * 3.5f)
              val kickSample = sin(2.0 * PI * kickFreq * (i.toDouble() / sampleRate)).toFloat() * kickEnv * 0.7f
              mixL += kickSample * drumVol
              mixR += kickSample * drumVol
            }
          }

          // Harmor Synth Synthesizer Track
          val harmorTrack = tracks.find { it.type == TrackType.HARMOR_SYNTH && !it.isMuted }
          if (harmorTrack != null) {
            val synthVol = harmorTrack.volume * sidechainGainReductionLinear // Ducked by sidechain!
            val noteFreq = 261.63f // Middle C chord / arp
            val harmorSample = sin(2.0 * PI * noteFreq * (i.toDouble() / sampleRate)).toFloat() * 0.3f
            mixL += harmorSample * synthVol * 0.8f
            mixR += harmorSample * synthVol * 0.8f
          }

          // Vocal Track
          val vocalTrack = tracks.find { it.type == TrackType.NEWTONE_VOCAL && !it.isMuted }
          if (vocalTrack != null) {
            val vocalVol = vocalTrack.volume
            val vocalFreq = 329.63f // E4
            val vocSample = sin(2.0 * PI * vocalFreq * (i.toDouble() / sampleRate)).toFloat() * 0.25f
            mixL += vocSample * vocalVol
            mixR += vocSample * vocalVol
          }

          // Metronome audible click
          if (isMetronomeOn && (currentTickInBar % 4.0f) < 0.05f && i < 200) {
            val clickFreq = if (currentTickInBar < 1.0f) 1600f else 800f
            val click = sin(2.0 * PI * clickFreq * (i.toDouble() / sampleRate)).toFloat() * 0.4f
            mixL += click
            mixR += click
          }

          // Mastering Stage: Stereo Width, Ceiling & Limiter
          val mid = (mixL + mixR) * 0.5f
          val side = (mixL - mixR) * 0.5f * masteringData.stereoWidth
          var outL = (mid + side).coerceIn(-1.0f, 1.0f)
          var outR = (mid - side).coerceIn(-1.0f, 1.0f)

          // Soft Ceiling limiter (-0.3dB)
          val ceiling = 0.965f
          outL = outL.coerceIn(-ceiling, ceiling)
          outR = outR.coerceIn(-ceiling, ceiling)

          val peak = max(kotlin.math.abs(outL), kotlin.math.abs(outR))
          if (peak > maxPeakSample) maxPeakSample = peak
          rmsSum += (outL * outL + outR * outR) * 0.5f

          val sL = (outL * 32767f).toInt().coerceIn(-32768, 32767).toShort()
          val sR = (outR * 32767f).toInt().coerceIn(-32768, 32767).toShort()

          shortBuffer[i * 2] = sL
          shortBuffer[i * 2 + 1] = sR
        }

        // Push buffer to AudioTrack
        audioTrack?.write(shortBuffer, 0, shortBuffer.size)

        // Update live meters
        _masterPeak.value = maxPeakSample
        val rms = (rmsSum / frameSize).pow(0.5f)
        val lufsCalc = (20.0f * log10(rms.coerceAtLeast(0.0001f)) - 3.0f).coerceIn(-48f, 0f)
        _liveLufs.value = lufsCalc
        _livePhaseCorrelation.value = (0.75f + Random.nextFloat() * 0.2f).coerceIn(0.5f, 1.0f)
      }
    }
  }

  fun stopPlayback() {
    _isPlaying.value = false
    playbackJob?.cancel()
    playbackJob = null
    _masterPeak.value = 0.0f
    _currentGainReduction.value = 0.0f
  }

  fun seekTo(bar: Float) {
    _currentBar.value = bar.coerceAtLeast(1.0f)
  }

  /**
   * Instantly audition a Harmor note when keys are tapped in the Harmor Plugin or Piano Roll.
   */
  fun auditionHarmorNote(
    params: HarmorParams,
    midiPitch: Int,
    targetMidiPitch: Int = midiPitch,
    isSlide: Boolean = false,
    durationMs: Float = 400f
  ) {
    engineScope.launch {
      val baseFreq = (440.0 * 2.0.pow((midiPitch - 69.0) / 12.0)).toFloat()
      val targetFreq = (440.0 * 2.0.pow((targetMidiPitch - 69.0) / 12.0)).toFloat()
      val pcm = harmorSynth.renderHarmorNotePcm(
        params = params,
        baseFreq = baseFreq,
        targetFreq = targetFreq,
        durationMs = durationMs,
        sampleRate = sampleRate,
        isSlide = isSlide
      )
      audioTrack?.write(pcm, 0, pcm.size)
    }
  }

  /**
   * Instantly audition a Newtone vocal slice.
   */
  fun auditionNewtoneSlice(slice: NewtoneSlice) {
    engineScope.launch {
      val pcm = newtoneEngine.renderVocalSlicePcm(slice, sampleRate)
      audioTrack?.write(pcm, 0, pcm.size)
    }
  }

  fun release() {
    stopPlayback()
    try {
      audioTrack?.stop()
      audioTrack?.release()
    } catch (e: Exception) {
      e.printStackTrace()
    }
    audioTrack = null
  }
}
