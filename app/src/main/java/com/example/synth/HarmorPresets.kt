package com.example.synth

import com.example.audio.FilterType
import com.example.audio.HarmonicWaveform
import com.example.audio.HarmorParams

object HarmorPresets {
  val list = listOf(
    HarmorParams(
      presetName = "Cyberpunk Lead 2077",
      waveform = HarmonicWaveform.SAW,
      partials = List(24) { i -> 1f / (i + 1) },
      brightness = 0.85f,
      prism = 0.18f,
      blur = 0.05f,
      pluckDecay = 0.2f,
      filterCutoff = 4800f,
      filterResonance = 0.45f,
      filterType = FilterType.LOW_PASS,
      unisonVoices = 5,
      unisonDetune = 0.32f,
      unisonPanSpread = 0.75f,
      attackMs = 12f,
      decayMs = 380f,
      sustainLevel = 0.75f,
      releaseMs = 320f,
      phaserMix = 0.25f,
      phaserSpeed = 0.8f
    ),
    HarmorParams(
      presetName = "Heavy Reese Bass",
      waveform = HarmonicWaveform.SAW,
      partials = List(16) { i -> 1f / ((i + 1).toFloat() * 0.8f) },
      brightness = 0.65f,
      prism = 0.05f,
      blur = 0.0f,
      pluckDecay = 0.1f,
      filterCutoff = 1100f,
      filterResonance = 0.55f,
      filterType = FilterType.LOW_PASS,
      unisonVoices = 7,
      unisonDetune = 0.45f,
      unisonPanSpread = 0.90f,
      attackMs = 25f,
      decayMs = 600f,
      sustainLevel = 0.9f,
      releaseMs = 250f,
      phaserMix = 0.15f,
      phaserSpeed = 0.3f
    ),
    HarmorParams(
      presetName = "Harmonic Glass Pad",
      waveform = HarmonicWaveform.ORGAN,
      partials = List(32) { i -> if (i % 2 == 0) 0.8f / (i + 1) else 0.4f / (i + 1) },
      brightness = 0.90f,
      prism = 0.25f,
      blur = 0.75f, // Lush harmonic smear
      pluckDecay = 0.0f,
      filterCutoff = 6500f,
      filterResonance = 0.20f,
      filterType = FilterType.BAND_PASS,
      unisonVoices = 6,
      unisonDetune = 0.28f,
      unisonPanSpread = 0.85f,
      attackMs = 280f,
      decayMs = 800f,
      sustainLevel = 0.85f,
      releaseMs = 950f,
      phaserMix = 0.35f,
      phaserSpeed = 0.4f
    ),
    HarmorParams(
      presetName = "Pluck of Gods",
      waveform = HarmonicWaveform.PLUCK,
      partials = List(20) { i -> 1f / ((i + 1).toFloat() * 1.5f) },
      brightness = 0.95f,
      prism = 0.10f,
      blur = 0.08f,
      pluckDecay = 0.75f, // Fast pluck decay
      filterCutoff = 7200f,
      filterResonance = 0.30f,
      filterType = FilterType.LOW_PASS,
      unisonVoices = 4,
      unisonDetune = 0.20f,
      unisonPanSpread = 0.65f,
      attackMs = 5f,
      decayMs = 240f,
      sustainLevel = 0.20f,
      releaseMs = 180f,
      phaserMix = 0.10f,
      phaserSpeed = 1.2f
    ),
    HarmorParams(
      presetName = "Vocaloid Formant Synth",
      waveform = HarmonicWaveform.VOCAL_FORMANT,
      partials = List(28) { i -> 1f / (i + 1) },
      brightness = 0.80f,
      prism = 0.08f,
      blur = 0.30f,
      pluckDecay = 0.15f,
      filterCutoff = 3600f,
      filterResonance = 0.60f,
      filterType = FilterType.PHASER,
      unisonVoices = 3,
      unisonDetune = 0.15f,
      unisonPanSpread = 0.50f,
      attackMs = 40f,
      decayMs = 450f,
      sustainLevel = 0.70f,
      releaseMs = 380f,
      phaserMix = 0.40f,
      phaserSpeed = 0.6f
    )
  )
}
