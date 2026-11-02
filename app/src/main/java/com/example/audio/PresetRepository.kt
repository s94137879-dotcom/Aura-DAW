package com.example.audio

import com.example.synth.HarmorPresets

object PresetRepository {

  val mixingPresets: List<DawPreset> = listOf(
    DawPreset(
      id = "mix_1",
      name = "Punchy Kick-Bass Sidechain",
      category = "Mixing Chain",
      description = "Fast attack (8ms) with 120ms release to create an infectious pumping groove between kick and sub-bass.",
      sidechainParams = SidechainCompressorParams(
        enabled = true,
        sourceTrackId = "trk_1",
        thresholdDb = -22.0f,
        ratio = 6.0f,
        attackMs = 8.0f,
        releaseMs = 120.0f,
        kneeDb = 4.0f,
        makeupGainDb = 1.5f
      )
    ),
    DawPreset(
      id = "mix_2",
      name = "Vocal Ducking on Reverb",
      category = "Mixing Chain",
      description = "Ducks the reverb send whenever the lead vocal sings, then swells reverb in vocal pauses.",
      sidechainParams = SidechainCompressorParams(
        enabled = true,
        sourceTrackId = "trk_4",
        thresholdDb = -26.0f,
        ratio = 4.5f,
        attackMs = 15.0f,
        releaseMs = 280.0f,
        kneeDb = 6.0f,
        makeupGainDb = 0.0f
      )
    ),
    DawPreset(
      id = "mix_3",
      name = "Heavy Glue Bus Compressor",
      category = "Mixing Chain",
      description = "Gentle 2:1 ratio glue compressor across all instrumental busses for cohesive analog warmth.",
      sidechainParams = SidechainCompressorParams(
        enabled = true,
        sourceTrackId = "trk_master",
        thresholdDb = -16.0f,
        ratio = 2.0f,
        attackMs = 30.0f,
        releaseMs = 100.0f,
        kneeDb = 8.0f,
        makeupGainDb = 2.0f
      )
    )
  )

  val masteringPresets: List<DawPreset> = listOf(
    DawPreset(
      id = "mast_1",
      name = "Streaming Ready (-14 LUFS)",
      category = "Mastering",
      description = "Targeted for Spotify, Apple Music & YouTube with -0.3 dB True Peak ceiling to prevent inter-sample clipping.",
      masteringData = MasteringSuiteData(
        presetName = "Streaming Ready (-14 LUFS)",
        targetLufs = -14.0f,
        integratedLufs = -14.1f,
        shortTermLufs = -13.7f,
        momentaryLufs = -12.8f,
        truePeakDb = -0.3f,
        ceilingDb = -0.3f,
        thresholdDb = -4.5f,
        stereoWidth = 1.20f,
        bassWarmthDb = +1.0f,
        airSparkleDb = +1.8f
      )
    ),
    DawPreset(
      id = "mast_2",
      name = "Club / Festival Bang (-8 LUFS)",
      category = "Mastering",
      description = "High-energy commercial electronic master with hard transient punch and saturated low-end slam.",
      masteringData = MasteringSuiteData(
        presetName = "Club / Festival Bang (-8 LUFS)",
        targetLufs = -8.5f,
        integratedLufs = -8.6f,
        shortTermLufs = -8.1f,
        momentaryLufs = -7.5f,
        truePeakDb = -0.1f,
        ceilingDb = -0.1f,
        thresholdDb = -8.0f,
        stereoWidth = 1.40f,
        bassWarmthDb = +2.5f,
        airSparkleDb = +2.2f
      )
    ),
    DawPreset(
      id = "mast_3",
      name = "Analog Tape & Vinyl (-12 LUFS)",
      category = "Mastering",
      description = "Organic harmonic tape saturation with mono-compatible bass under 120Hz and silky rolled-off highs.",
      masteringData = MasteringSuiteData(
        presetName = "Analog Tape & Vinyl (-12 LUFS)",
        targetLufs = -12.0f,
        integratedLufs = -12.2f,
        shortTermLufs = -11.9f,
        momentaryLufs = -10.8f,
        truePeakDb = -0.5f,
        ceilingDb = -0.5f,
        thresholdDb = -5.5f,
        stereoWidth = 1.05f,
        bassWarmthDb = +2.0f,
        airSparkleDb = +0.8f
      )
    )
  )

  fun getAllPresets(): List<DawPreset> {
    val harmorConverted = HarmorPresets.list.mapIndexed { idx, hp ->
      DawPreset(
        id = "harmor_preset_$idx",
        name = hp.presetName,
        category = "Harmor Synth",
        description = "Additive synth preset with ${hp.waveform.name} harmonics, prism ${hp.prism}, blur ${hp.blur}.",
        harmorParams = hp
      )
    }
    return harmorConverted + mixingPresets + masteringPresets
  }
}
