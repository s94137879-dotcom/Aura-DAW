package com.example.synth

import com.example.audio.FilterType
import com.example.audio.HarmonicWaveform
import com.example.audio.HarmorParams
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin

/**
 * Real-time additive synthesis engine modeled after FL Studio's Harmor.
 * Renders audio PCM samples on the fly for note auditioning and timeline playback.
 */
class HarmorSynthEngine {

  /**
   * Generates a stereo 16-bit PCM buffer for a given note frequency and duration.
   */
  fun renderHarmorNotePcm(
    params: HarmorParams,
    baseFreq: Float,
    targetFreq: Float,
    durationMs: Float,
    sampleRate: Int = 44100,
    velocity: Float = 0.85f,
    isSlide: Boolean = false
  ): ShortArray {
    val totalSamples = ((durationMs / 1000f) * sampleRate).toInt().coerceAtLeast(128)
    val buffer = ShortArray(totalSamples * 2) // Stereo interleaved: [L0, R0, L1, R1, ...]

    val numPartials = params.partials.size.coerceIn(8, 64)
    val unisonVoices = params.unisonVoices.coerceIn(1, 9)
    val detuneAmount = params.unisonDetune * 0.05f

    // Precalculate partial amplitude weights
    val partialAmps = FloatArray(numPartials) { i ->
      val raw = when (params.waveform) {
        HarmonicWaveform.SAW -> 1.0f / (i + 1)
        HarmonicWaveform.SQUARE -> if (i % 2 == 0) 1.0f / (i + 1) else 0.0f
        HarmonicWaveform.PLUCK -> (1.0f / (i + 1).toFloat().pow(1.5f))
        HarmonicWaveform.ORGAN -> when (i) {
          0 -> 1.0f
          1 -> 0.7f
          2 -> 0.5f
          3 -> 0.6f
          5 -> 0.4f
          7 -> 0.3f
          else -> 0.05f
        }
        HarmonicWaveform.VOCAL_FORMANT -> {
          // Formant peaks around 800Hz and 2500Hz
          val freq = baseFreq * (i + 1)
          val f1 = exp(-((freq - 750f) / 250f).pow(2))
          val f2 = exp(-((freq - 2300f) / 400f).pow(2))
          (0.2f + 0.8f * (f1 + f2)).toFloat().coerceIn(0.01f, 1.0f)
        }
        HarmonicWaveform.CUSTOM_PARTIALS -> params.partials.getOrElse(i) { 1.0f / (i + 1) }
      }
      raw * params.brightness
    }

    // Apply Harmor Blur (smoothing harmonic distribution)
    if (params.blur > 0.01f) {
      val blurWeight = params.blur * 0.5f
      for (i in 1 until numPartials - 1) {
        partialAmps[i] = (1f - blurWeight) * partialAmps[i] + blurWeight * 0.5f * (partialAmps[i - 1] + partialAmps[i + 1])
      }
    }

    val attackSamples = ((params.attackMs / 1000f) * sampleRate).toInt().coerceAtLeast(1)
    val decaySamples = ((params.decayMs / 1000f) * sampleRate).toInt().coerceAtLeast(1)
    val releaseSamples = ((params.releaseMs / 1000f) * sampleRate).toInt().coerceAtLeast(1)

    // Phase accumulators for each unison voice
    val voicePhases = FloatArray(unisonVoices) { v -> (v * 0.23f) }

    for (n in 0 until totalSamples) {
      val progress = n.toFloat() / totalSamples.toFloat()

      // Calculate current pitch with glide/slide automation support
      val currentFreq = if (isSlide) {
        // Smooth S-curve glide from baseFreq to targetFreq
        val t = progress.coerceIn(0f, 1f)
        val smoothT = t * t * (3f - 2f * t)
        baseFreq + (targetFreq - baseFreq) * smoothT
      } else {
        baseFreq
      }

      // ADSR Amplitude Envelope
      val envAmp = when {
        n < attackSamples -> n.toFloat() / attackSamples
        n < attackSamples + decaySamples -> {
          val decayProg = (n - attackSamples).toFloat() / decaySamples
          1.0f - decayProg * (1.0f - params.sustainLevel)
        }
        n >= totalSamples - releaseSamples -> {
          val relProg = (n - (totalSamples - releaseSamples)).toFloat() / releaseSamples
          params.sustainLevel * (1.0f - relProg).coerceAtLeast(0f)
        }
        else -> params.sustainLevel
      }

      // Pluck high-harmonic exponential dampening
      val pluckFactor = exp(-progress * (params.pluckDecay * 8.0f))

      var sampleLeft = 0.0f
      var sampleRight = 0.0f

      // Unison voice calculation
      for (v in 0 until unisonVoices) {
        val voiceDetune = if (unisonVoices > 1) {
          1.0f + (v - (unisonVoices - 1) / 2.0f) * detuneAmount
        } else {
          1.0f
        }
        val voicePan = if (unisonVoices > 1) {
          ((v.toFloat() / (unisonVoices - 1)) * 2f - 1f) * params.unisonPanSpread
        } else {
          0.0f
        }

        val panLeft = ((1.0f - voicePan) * 0.5f).coerceIn(0f, 1f)
        val panRight = ((1.0f + voicePan) * 0.5f).coerceIn(0f, 1f)

        // Sum Harmor Additive Partials
        var voiceSum = 0.0f
        for (p in 0 until numPartials) {
          val harmonicIndex = p + 1

          // Harmor Harmonic Prism (inharmonic detuning for metallic / hybrid tones)
          val prismMultiplier = 1.0f + (params.prism * (harmonicIndex - 1) * 0.08f)
          val partialFreq = currentFreq * harmonicIndex * voiceDetune * prismMultiplier

          // Filter Cutoff check
          val filterAtten = calculateFilterGain(partialFreq, params.filterCutoff, params.filterResonance, params.filterType)

          // Pluck dampening applied more severely to higher harmonics
          val harmonicPluck = if (harmonicIndex > 1) pluckFactor.pow((harmonicIndex * 0.4f).toFloat()) else 1.0f

          val amp = partialAmps[p] * filterAtten * harmonicPluck

          if (amp > 0.001f) {
            val phase = (voicePhases[v] + 2.0 * PI * partialFreq * (n.toDouble() / sampleRate))
            val harmonicSample = sin(phase).toFloat() * amp
            voiceSum += harmonicSample
          }
        }

        sampleLeft += voiceSum * panLeft
        sampleRight += voiceSum * panRight
      }

      // Phaser modulation (Harmonic phaser)
      if (params.phaserMix > 0.05f) {
        val lfo = (sin(2.0 * PI * params.phaserSpeed * (n.toDouble() / sampleRate))).toFloat()
        val phaserL = sampleLeft * (1.0f - params.phaserMix) + (sampleLeft * cos(lfo * 1.5f)) * params.phaserMix
        val phaserR = sampleRight * (1.0f - params.phaserMix) + (sampleRight * sin(lfo * 1.5f)) * params.phaserMix
        sampleLeft = phaserL
        sampleRight = phaserR
      }

      // Normalization, master gain & soft clipper
      val masterGain = (velocity * envAmp * 0.45f) / unisonVoices.toFloat().pow(0.5f)
      val outL = softClip(sampleLeft * masterGain)
      val outR = softClip(sampleRight * masterGain)

      val idx = n * 2
      buffer[idx] = (outL * 32767f).toInt().coerceIn(-32768, 32767).toShort()
      buffer[idx + 1] = (outR * 32767f).toInt().coerceIn(-32768, 32767).toShort()
    }

    return buffer
  }

  private fun calculateFilterGain(freq: Float, cutoff: Float, res: Float, type: FilterType): Float {
    val ratio = (freq / cutoff.coerceAtLeast(20f))
    return when (type) {
      FilterType.LOW_PASS -> {
        val base = 1.0f / (1.0f + ratio.pow(4f))
        if (ratio in 0.85f..1.15f) base * (1.0f + res * 2.5f) else base
      }
      FilterType.HIGH_PASS -> {
        val base = ratio.pow(4f) / (1.0f + ratio.pow(4f))
        if (ratio in 0.85f..1.15f) base * (1.0f + res * 2.5f) else base
      }
      FilterType.BAND_PASS -> {
        val dist = kotlin.math.abs(freq - cutoff) / cutoff
        (1.0f / (1.0f + dist * 6f)) * (1.0f + res * 2.0f)
      }
      FilterType.PHASER -> {
        0.5f + 0.5f * cos(ratio * PI).toFloat()
      }
    }.coerceIn(0.0f, 2.5f)
  }

  private fun softClip(x: Float): Float {
    return when {
      x > 1.2f -> 0.95f
      x < -1.2f -> -0.95f
      else -> x - (x.pow(3f) / 3f)
    }
  }
}
