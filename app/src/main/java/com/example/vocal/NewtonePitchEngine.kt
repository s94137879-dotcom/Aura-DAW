package com.example.vocal

import com.example.audio.MusicalScale
import com.example.audio.NewtoneSlice
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin

/**
 * Precision vocal pitch correction, formant modification, and time-stretch engine
 * inspired by FL Studio's Newtone.
 */
class NewtonePitchEngine {

  /**
   * Snaps a float or integer pitch to the selected musical scale.
   */
  fun snapToScale(pitch: Float, rootNote: Int = 0, scale: MusicalScale = MusicalScale.NATURAL_MINOR): Int {
    val roundPitch = pitch.toInt()
    val noteInOctave = (roundPitch % 12 + 12) % 12
    val octave = roundPitch / 12

    if (scale.intervals.contains((noteInOctave - rootNote + 12) % 12)) {
      return roundPitch
    }

    // Find closest allowed note in the scale
    var minDistance = 999
    var bestNote = roundPitch
    for (interval in scale.intervals) {
      val candidateInOctave = (rootNote + interval) % 12
      val diff1 = candidateInOctave - noteInOctave
      val diff2 = diff1 + if (diff1 < 0) 12 else -12
      val effectiveDiff = if (kotlin.math.abs(diff1) < kotlin.math.abs(diff2)) diff1 else diff2
      if (kotlin.math.abs(effectiveDiff) < minDistance) {
        minDistance = kotlin.math.abs(effectiveDiff)
        bestNote = roundPitch + effectiveDiff
      }
    }
    return bestNote
  }

  /**
   * Renders real-time audio PCM buffer for an auditioned vocal slice with pitch correction,
   * formant shifting, and time stretching applied.
   */
  fun renderVocalSlicePcm(
    slice: NewtoneSlice,
    sampleRate: Int = 44100
  ): ShortArray {
    // Effective duration stretched by timeStretch factor
    val baseDurationMs = (slice.durationTicks * 125f) // 16th tick @ 120bpm approx 125ms
    val stretchedDurationMs = (baseDurationMs * slice.timeStretch).coerceIn(80f, 6000f)
    val totalSamples = ((stretchedDurationMs / 1000f) * sampleRate).toInt().coerceAtLeast(256)
    val buffer = ShortArray(totalSamples * 2) // Stereo

    // Calculate effective pitch: target pitch blended with original by centerPitchAmount + pitchShift
    val targetMidi = slice.targetPitch + slice.pitchShift
    val originalMidi = slice.originalPitch + slice.pitchShift
    val effectiveMidi = originalMidi + (targetMidi - originalMidi) * slice.centerPitchAmount
    val baseFreq = (440.0 * 2.0.pow((effectiveMidi - 69.0) / 12.0)).toFloat()

    // Formant filter center frequencies modified by formantShift
    val formantMultiplier = 2.0.pow(slice.formantShift / 12.0).toFloat()
    val f1 = 800f * formantMultiplier
    val f2 = 2400f * formantMultiplier

    val attackSamples = (sampleRate * 0.02f).toInt()
    val releaseSamples = (sampleRate * 0.04f).toInt()

    for (n in 0 until totalSamples) {
      val progress = n.toFloat() / totalSamples.toFloat()

      // Micro-pitch envelope & vibrato
      val vibrato = if (slice.vibratoDepth > 0.01f) {
        sin(2.0 * PI * 5.5 * (n.toDouble() / sampleRate)).toFloat() * (slice.vibratoDepth * 0.4f)
      } else {
        0.0f
      }

      val instantFreq = baseFreq * (1.0f + vibrato * 0.03f)

      // Envelope fade in/out to prevent clicks
      val env = when {
        n < attackSamples -> n.toFloat() / attackSamples
        n > totalSamples - releaseSamples -> (totalSamples - n).toFloat() / releaseSamples
        else -> 1.0f
      }.coerceIn(0f, 1f)

      // Vocal harmonic synthesis with formant shaping
      var sampleLeft = 0.0f
      for (h in 1..24) {
        val harmonicFreq = instantFreq * h
        if (harmonicFreq >= sampleRate / 2) break

        // Vocal Formant resonator filter (glottal pulse + vocal tract simulation)
        val formantWeight1 = exp(-((harmonicFreq - f1) / (f1 * 0.4f)).pow(2))
        val formantWeight2 = exp(-((harmonicFreq - f2) / (f2 * 0.35f)).pow(2))
        val formantGain = (0.15f + 0.85f * (formantWeight1 + formantWeight2 * 0.7f)) / h.toFloat().pow(0.6f)

        val phase = 2.0 * PI * harmonicFreq * (n.toDouble() / sampleRate)
        val s = sin(phase).toFloat() * formantGain
        sampleLeft += s
      }

      val outSample = (sampleLeft * env * 0.32f).coerceIn(-0.95f, 0.95f)
      val sampleShort = (outSample * 32767f).toInt().toShort()

      val idx = n * 2
      buffer[idx] = sampleShort
      buffer[idx + 1] = sampleShort // Centered stereo
    }

    return buffer
  }

  /**
   * Generates default realistic vocal slices for project start.
   */
  fun generateDefaultVocalSlices(): List<NewtoneSlice> {
    return listOf(
      NewtoneSlice(
        id = "voc_1",
        wordSyllable = "Stay",
        startTick = 0f,
        durationTicks = 4f,
        originalPitch = 60.3f, // slightly sharp C4
        targetPitch = 60, // C4
        pitchCurve = listOf(60.1f, 60.3f, 60.2f, 60.0f),
        formantShift = 0f,
        pitchShift = 0f,
        timeStretch = 1.0f,
        centerPitchAmount = 0.85f,
        vibratoDepth = 0.25f
      ),
      NewtoneSlice(
        id = "voc_2",
        wordSyllable = "with",
        startTick = 4f,
        durationTicks = 2f,
        originalPitch = 61.9f, // flat D4
        targetPitch = 62, // D4
        pitchCurve = listOf(61.8f, 61.9f, 62.0f),
        formantShift = 0f,
        pitchShift = 0f,
        timeStretch = 1.0f,
        centerPitchAmount = 0.90f,
        vibratoDepth = 0.15f
      ),
      NewtoneSlice(
        id = "voc_3",
        wordSyllable = "me",
        startTick = 6f,
        durationTicks = 6f,
        originalPitch = 63.7f, // flat E4
        targetPitch = 64, // E4
        pitchCurve = listOf(63.7f, 63.8f, 63.9f, 64.0f, 64.1f),
        formantShift = 0f,
        pitchShift = 0f,
        timeStretch = 1.0f,
        centerPitchAmount = 0.95f,
        vibratoDepth = 0.40f
      ),
      NewtoneSlice(
        id = "voc_4",
        wordSyllable = "to-",
        startTick = 12f,
        durationTicks = 2f,
        originalPitch = 65.2f,
        targetPitch = 65, // F4
        pitchCurve = listOf(65.1f, 65.2f),
        formantShift = 0f,
        pitchShift = 0f,
        timeStretch = 1.0f,
        centerPitchAmount = 0.80f,
        vibratoDepth = 0.10f
      ),
      NewtoneSlice(
        id = "voc_5",
        wordSyllable = "night",
        startTick = 14f,
        durationTicks = 8f,
        originalPitch = 67.4f, // G4
        targetPitch = 67, // G4
        pitchCurve = listOf(67.2f, 67.4f, 67.5f, 67.3f, 67.1f),
        formantShift = 0f,
        pitchShift = 0f,
        timeStretch = 1.0f,
        centerPitchAmount = 0.95f,
        vibratoDepth = 0.50f
      )
    )
  }
}
