package com.example.audio

enum class TrackType {
  AUDIO,
  MIDI,
  HARMOR_SYNTH,
  NEWTONE_VOCAL,
  DRUMS,
  AUTOMATION
}

enum class HarmonicWaveform {
  SAW,
  SQUARE,
  PLUCK,
  ORGAN,
  VOCAL_FORMANT,
  CUSTOM_PARTIALS
}

enum class FilterType {
  LOW_PASS,
  BAND_PASS,
  HIGH_PASS,
  PHASER
}

enum class MusicalScale(val displayName: String, val intervals: List<Int>) {
  CHROMATIC("Chromatic (All Notes)", listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)),
  MAJOR("Major / Ionian", listOf(0, 2, 4, 5, 7, 9, 11)),
  NATURAL_MINOR("Natural Minor / Aeolian", listOf(0, 2, 3, 5, 7, 8, 10)),
  HARMONIC_MINOR("Harmonic Minor", listOf(0, 2, 3, 5, 7, 8, 11)),
  DORIAN("Dorian (Chill / Funk)", listOf(0, 2, 3, 5, 7, 9, 10)),
  PENTATONIC_MAJOR("Pentatonic Major", listOf(0, 2, 4, 7, 9)),
  PENTATONIC_MINOR("Pentatonic Minor", listOf(0, 3, 5, 7, 10)),
  BLUES("Blues Scale", listOf(0, 3, 5, 6, 7, 10))
}

data class MidiNote(
  val id: String,
  val pitch: Int, // 24 to 96 (C1 to C7)
  val startTick: Float, // In 16th beats
  val durationTicks: Float,
  val velocity: Float = 0.8f,
  val isSlide: Boolean = false, // FL Studio Slide Note!
  val slideTargetPitch: Int = pitch,
  val pan: Float = 0.0f
)

data class TrackClip(
  val id: String,
  val name: String,
  val startBar: Float, // 1.0, 2.0, 3.5
  val lengthBars: Float,
  val isAudio: Boolean,
  val waveformAmplitudes: List<Float> = emptyList(),
  val notes: List<MidiNote> = emptyList(),
  val color: Long
)

data class HarmorParams(
  val presetName: String = "Init Harmor",
  val waveform: HarmonicWaveform = HarmonicWaveform.SAW,
  val partials: List<Float> = List(16) { index -> 1.0f / (index + 1) },
  val brightness: Float = 0.75f,
  val prism: Float = 0.15f, // Inharmonic detuning of partials
  val blur: Float = 0.20f, // Time & harmonic smear
  val pluckDecay: Float = 0.40f, // Pluck dampening speed
  val filterCutoff: Float = 2800f, // 20Hz - 20kHz
  val filterResonance: Float = 0.35f,
  val filterType: FilterType = FilterType.LOW_PASS,
  val unisonVoices: Int = 4,
  val unisonDetune: Float = 0.25f,
  val unisonPanSpread: Float = 0.60f,
  val attackMs: Float = 15f,
  val decayMs: Float = 350f,
  val sustainLevel: Float = 0.65f,
  val releaseMs: Float = 400f,
  val phaserMix: Float = 0.20f,
  val phaserSpeed: Float = 0.5f
)

data class NewtoneSlice(
  val id: String,
  val wordSyllable: String,
  val startTick: Float,
  val durationTicks: Float,
  val originalPitch: Float, // Exact float pitch including detune (e.g., 61.2)
  val targetPitch: Int, // Quantized snapped MIDI note (e.g., 61 = C#4)
  val pitchCurve: List<Float>, // Micro-tuning pitch envelope across note duration
  val formantShift: Float = 0.0f, // -12 to +12 semitones
  val pitchShift: Float = 0.0f, // -24 to +24 semitones
  val timeStretch: Float = 1.0f, // Time Screat: 0.5x to 2.0x stretch
  val centerPitchAmount: Float = 0.85f, // Tuning snap strength
  val vibratoDepth: Float = 0.30f
)

data class SidechainCompressorParams(
  val enabled: Boolean = true,
  val sourceTrackId: String = "trk_drums", // Sidechain trigger (e.g. Kick)
  val thresholdDb: Float = -24.0f, // -60dB to 0dB
  val ratio: Float = 4.0f, // 1:1 to 20:1
  val attackMs: Float = 8.0f, // 0.1ms to 100ms
  val releaseMs: Float = 120.0f, // 10ms to 800ms
  val kneeDb: Float = 3.0f,
  val makeupGainDb: Float = 2.0f,
  val currentGainReductionDb: Float = 0.0f // Live meter
)

data class ParametricBand(
  val id: Int,
  val name: String,
  val frequencyHz: Float,
  val gainDb: Float,
  val qFactor: Float,
  val isEnabled: Boolean = true
)

data class MasteringSuiteData(
  val presetName: String = "Streaming Standard (-14 LUFS)",
  val targetLufs: Float = -14.0f,
  val integratedLufs: Float = -14.2f,
  val shortTermLufs: Float = -13.8f,
  val momentaryLufs: Float = -12.9f,
  val truePeakDb: Float = -0.5f,
  val ceilingDb: Float = -0.3f,
  val thresholdDb: Float = -4.0f,
  val stereoWidth: Float = 1.25f, // 0.0 (mono) to 2.0 (super-wide)
  val correlationPhase: Float = 0.82f, // -1.0 to +1.0 phase correlation
  val bassWarmthDb: Float = +1.5f,
  val airSparkleDb: Float = +2.0f,
  val lookaheadMs: Float = 5.0f,
  val isLimiterEngaged: Boolean = true
)

data class AutomationNode(
  val id: String,
  val tick: Float,
  val value: Float, // 0.0 to 1.0
  val tension: Float = 0.0f // Bezier tension (-1.0 to 1.0)
)

data class AutomationLane(
  val id: String,
  val trackId: String,
  val parameterName: String, // "Volume", "Pan", "Filter Cutoff", "Resonance", "Reverb Send"
  val isEnabled: Boolean = true,
  val nodes: List<AutomationNode> = emptyList()
)

data class DawTrack(
  val id: String,
  val name: String,
  val type: TrackType,
  val color: Long,
  val volume: Float = 0.85f, // 0.0 to 1.5
  val pan: Float = 0.0f, // -1.0 to 1.0
  val isMuted: Boolean = false,
  val isSoloed: Boolean = false,
  val isArmed: Boolean = false,
  val clips: List<TrackClip> = emptyList(),
  val sidechainCompressor: SidechainCompressorParams = SidechainCompressorParams(),
  val eqBands: List<ParametricBand> = listOf(
    ParametricBand(1, "Low Cut", 35f, 0f, 0.7f),
    ParametricBand(2, "Sub / Bass", 90f, 1.2f, 1.0f),
    ParametricBand(3, "Low Mid", 280f, -1.0f, 1.2f),
    ParametricBand(4, "Mid", 1000f, 0f, 1.0f),
    ParametricBand(5, "High Mid", 3200f, 1.5f, 1.4f),
    ParametricBand(6, "Presence", 7500f, 0.8f, 1.0f),
    ParametricBand(7, "Air Sparkle", 14000f, 2.0f, 0.7f)
  ),
  val reverbMix: Float = 0.25f,
  val delayMix: Float = 0.15f,
  val pitchShiftSemitones: Float = 0f,
  val timeStretchFactor: Float = 1.0f,
  val currentVuPeak: Float = 0.0f // Live VU peak meter (0.0 to 1.2)
)

data class Collaborator(
  val id: String,
  val name: String,
  val role: String, // "Producer", "Vocalist", "Mastering Eng"
  val isOnline: Boolean,
  val avatarInitials: String,
  val avatarColor: Long,
  val activeTrackId: String? = null
)

data class ProjectComment(
  val id: String,
  val authorName: String,
  val barPosition: Float,
  val text: String,
  val timestamp: String
)

data class DawPreset(
  val id: String,
  val name: String,
  val category: String, // "Harmor Synth", "Vocal Master", "Mixing Chain", "Sidechain Bass"
  val description: String,
  val author: String = "AuraDAW Team",
  val harmorParams: HarmorParams? = null,
  val sidechainParams: SidechainCompressorParams? = null,
  val masteringData: MasteringSuiteData? = null
)
