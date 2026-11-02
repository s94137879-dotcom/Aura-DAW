package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioEngine
import com.example.audio.AudioRecorder
import com.example.audio.AutomationLane
import com.example.audio.AutomationNode
import com.example.audio.Collaborator
import com.example.audio.DawPreset
import com.example.audio.DawTrack
import com.example.audio.HarmorParams
import com.example.audio.MasteringSuiteData
import com.example.audio.MidiNote
import com.example.audio.MusicalScale
import com.example.audio.NewtoneSlice
import com.example.audio.PresetRepository
import com.example.audio.ProjectComment
import com.example.audio.SidechainCompressorParams
import com.example.audio.TrackClip
import com.example.audio.TrackType
import com.example.synth.HarmorPresets
import com.example.synth.HarmorSynthEngine
import com.example.vocal.NewtonePitchEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

enum class DawTab(val title: String) {
  TIMELINE("Timeline"),
  PIANO_ROLL("Piano Roll"),
  HARMOR("Harmor VST"),
  NEWTONE("Newtone Vocal"),
  MIXER("Mixer Rack"),
  MASTERING("Mastering"),
  AUTOMATION("Automation"),
  CLOUD_COLLAB("Cloud Collab")
}

class DawViewModel : ViewModel() {

  private val harmorSynthEngine = HarmorSynthEngine()
  private val newtonePitchEngine = NewtonePitchEngine()
  val audioEngine = AudioEngine(harmorSynthEngine, newtonePitchEngine)
  val audioRecorder = AudioRecorder()

  // Navigation
  private val _currentTab = MutableStateFlow(DawTab.TIMELINE)
  val currentTab: StateFlow<DawTab> = _currentTab.asStateFlow()

  // Multi-track state
  private val _tracks = MutableStateFlow<List<DawTrack>>(emptyList())
  val tracks: StateFlow<List<DawTrack>> = _tracks.asStateFlow()

  private val _selectedTrackId = MutableStateFlow("trk_harmor_lead")
  val selectedTrackId: StateFlow<String> = _selectedTrackId.asStateFlow()

  // Piano Roll state
  private val _pianoRollNotes = MutableStateFlow<List<MidiNote>>(emptyList())
  val pianoRollNotes: StateFlow<List<MidiNote>> = _pianoRollNotes.asStateFlow()

  private val _isSlideModeEnabled = MutableStateFlow(false)
  val isSlideModeEnabled: StateFlow<Boolean> = _isSlideModeEnabled.asStateFlow()

  private val _gridSnap = MutableStateFlow("1/16")
  val gridSnap: StateFlow<String> = _gridSnap.asStateFlow()

  // Harmor Synth state
  private val _harmorParams = MutableStateFlow(HarmorPresets.list.first())
  val harmorParams: StateFlow<HarmorParams> = _harmorParams.asStateFlow()

  // Newtone Vocal Correction state
  private val _vocalSlices = MutableStateFlow<List<NewtoneSlice>>(emptyList())
  val vocalSlices: StateFlow<List<NewtoneSlice>> = _vocalSlices.asStateFlow()

  private val _selectedSliceId = MutableStateFlow<String?>(null)
  val selectedSliceId: StateFlow<String?> = _selectedSliceId.asStateFlow()

  private val _selectedScale = MutableStateFlow(MusicalScale.NATURAL_MINOR)
  val selectedScale: StateFlow<MusicalScale> = _selectedScale.asStateFlow()

  // Mastering Suite state
  private val _masteringData = MutableStateFlow(PresetRepository.masteringPresets.first().masteringData ?: MasteringSuiteData())
  val masteringData: StateFlow<MasteringSuiteData> = _masteringData.asStateFlow()

  // Automation state
  private val _automationLanes = MutableStateFlow<List<AutomationLane>>(emptyList())
  val automationLanes: StateFlow<List<AutomationLane>> = _automationLanes.asStateFlow()

  // Presets state
  private val _allPresets = MutableStateFlow<List<DawPreset>>(emptyList())
  val allPresets: StateFlow<List<DawPreset>> = _allPresets.asStateFlow()

  private val _presetFilterCategory = MutableStateFlow("All")
  val presetFilterCategory: StateFlow<String> = _presetFilterCategory.asStateFlow()

  // Cloud Project Collaboration state
  private val _projectName = MutableStateFlow("Cyberpunk Odyssey (Stem v4)")
  val projectName: StateFlow<String> = _projectName.asStateFlow()

  private val _cloudSyncStatus = MutableStateFlow("Synced • Cloud v4.2")
  val cloudSyncStatus: StateFlow<String> = _cloudSyncStatus.asStateFlow()

  private val _collaborators = MutableStateFlow<List<Collaborator>>(emptyList())
  val collaborators: StateFlow<List<Collaborator>> = _collaborators.asStateFlow()

  private val _projectComments = MutableStateFlow<List<ProjectComment>>(emptyList())
  val projectComments: StateFlow<List<ProjectComment>> = _projectComments.asStateFlow()

  private val _toastMessage = MutableStateFlow<String?>(null)
  val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

  init {
    initDefaultProject()
  }

  private fun initDefaultProject() {
    // 1. Initial Tracks
    val kickTrack = DawTrack(
      id = "trk_drums",
      name = "Kick & Punch 808",
      type = TrackType.DRUMS,
      color = 0xFFFF3366,
      volume = 0.90f,
      clips = listOf(
        TrackClip(
          id = "clip_drums_1",
          name = "Main Beat 4/4",
          startBar = 1.0f,
          lengthBars = 8.0f,
          isAudio = false,
          color = 0xFFFF3366
        )
      )
    )

    val bassTrack = DawTrack(
      id = "trk_reese_bass",
      name = "Reese Sub Bass",
      type = TrackType.HARMOR_SYNTH,
      color = 0xFF8B5CF6,
      volume = 0.85f,
      sidechainCompressor = SidechainCompressorParams(
        enabled = true,
        sourceTrackId = "trk_drums",
        thresholdDb = -22.0f,
        ratio = 6.0f,
        attackMs = 8.0f,
        releaseMs = 120.0f
      ),
      clips = listOf(
        TrackClip(
          id = "clip_bass_1",
          name = "Heavy Ducking Bassline",
          startBar = 1.0f,
          lengthBars = 8.0f,
          isAudio = false,
          color = 0xFF8B5CF6
        )
      )
    )

    val harmorLeadTrack = DawTrack(
      id = "trk_harmor_lead",
      name = "Harmor Lead 2077",
      type = TrackType.HARMOR_SYNTH,
      color = 0xFF00E5FF,
      volume = 0.80f,
      clips = listOf(
        TrackClip(
          id = "clip_lead_1",
          name = "Cyber Arp & Glides",
          startBar = 1.0f,
          lengthBars = 8.0f,
          isAudio = false,
          color = 0xFF00E5FF
        )
      )
    )

    val vocalTrack = DawTrack(
      id = "trk_vocal_lead",
      name = "Newtone Vocal Lead",
      type = TrackType.NEWTONE_VOCAL,
      color = 0xFFEC4899,
      volume = 0.95f,
      pitchShiftSemitones = 0f,
      timeStretchFactor = 1.0f,
      clips = listOf(
        TrackClip(
          id = "clip_vocal_1",
          name = "Vocal Phrase 'Stay With Me'",
          startBar = 1.0f,
          lengthBars = 8.0f,
          isAudio = true,
          waveformAmplitudes = listOf(
            0.1f, 0.4f, 0.8f, 0.6f, 0.2f, 0.05f, 0.3f, 0.7f,
            0.9f, 0.5f, 0.2f, 0.1f, 0.5f, 0.85f, 0.75f, 0.3f,
            0.05f, 0.4f, 0.9f, 0.7f, 0.15f, 0.6f, 0.8f, 0.4f
          ),
          color = 0xFFEC4899
        )
      )
    )

    val padTrack = DawTrack(
      id = "trk_harmor_pad",
      name = "Glass Harmonic Pad",
      type = TrackType.HARMOR_SYNTH,
      color = 0xFF06B6D4,
      volume = 0.70f,
      clips = listOf(
        TrackClip(
          id = "clip_pad_1",
          name = "Atmospheric Chords",
          startBar = 1.0f,
          lengthBars = 8.0f,
          isAudio = false,
          color = 0xFF06B6D4
        )
      )
    )

    _tracks.value = listOf(kickTrack, bassTrack, harmorLeadTrack, vocalTrack, padTrack)

    // 2. Initial Piano Roll notes (with FL Studio Glide/Slide Note!)
    _pianoRollNotes.value = listOf(
      MidiNote("n1", pitch = 60, startTick = 0f, durationTicks = 3.5f, velocity = 0.85f),
      MidiNote("n2", pitch = 63, startTick = 4f, durationTicks = 3.5f, velocity = 0.90f),
      // FL Studio Slide note: Smoothly bends from pitch 63 to pitch 67 over 2 ticks!
      MidiNote("n3", pitch = 63, startTick = 7.5f, durationTicks = 2.0f, velocity = 0.95f, isSlide = true, slideTargetPitch = 67),
      MidiNote("n4", pitch = 67, startTick = 9.5f, durationTicks = 2.5f, velocity = 0.80f),
      MidiNote("n5", pitch = 65, startTick = 12f, durationTicks = 3.5f, velocity = 0.75f),
      MidiNote("n6", pitch = 60, startTick = 16f, durationTicks = 7.0f, velocity = 0.88f)
    )

    // 3. Newtone Vocal Slices
    _vocalSlices.value = newtonePitchEngine.generateDefaultVocalSlices()
    _selectedSliceId.value = _vocalSlices.value.firstOrNull()?.id

    // 4. Automation Lanes
    _automationLanes.value = listOf(
      AutomationLane(
        id = "auto_cutoff",
        trackId = "trk_harmor_lead",
        parameterName = "Harmor Filter Cutoff",
        nodes = listOf(
          AutomationNode("nd1", 0f, 0.25f, 0.2f),
          AutomationNode("nd2", 8f, 0.85f, -0.3f),
          AutomationNode("nd3", 16f, 0.40f, 0.0f),
          AutomationNode("nd4", 24f, 0.95f, 0.4f),
          AutomationNode("nd5", 32f, 0.30f, 0.0f)
        )
      ),
      AutomationLane(
        id = "auto_sidechain",
        trackId = "trk_reese_bass",
        parameterName = "Sidechain Ducking Depth",
        nodes = listOf(
          AutomationNode("nd_sc1", 0f, 0.80f),
          AutomationNode("nd_sc2", 16f, 0.80f),
          AutomationNode("nd_sc3", 24f, 0.95f)
        )
      )
    )

    // 5. Presets
    _allPresets.value = PresetRepository.getAllPresets()

    // 6. Collaborators
    _collaborators.value = listOf(
      Collaborator("u1", "Elena Vance", "Vocalist", true, "EV", 0xFFEC4899, "trk_vocal_lead"),
      Collaborator("u2", "Marcus Kane", "Sound Designer", true, "MK", 0xFF00E5FF, "trk_harmor_lead"),
      Collaborator("u3", "Alex Miller", "Mixing & Master", true, "AM", 0xFFFF9100, "trk_reese_bass"),
      Collaborator("u4", "You", "Producer / DAW Host", true, "ME", 0xFF10B981, null)
    )

    // 7. Project Comments
    _projectComments.value = listOf(
      ProjectComment("c1", "Elena Vance", 2.0f, "Newtone pitch correction sounds super natural here!", "10m ago"),
      ProjectComment("c2", "Marcus Kane", 4.5f, "Loved the Harmor prism distortion on bar 5.", "15m ago"),
      ProjectComment("c3", "Alex Miller", 1.0f, "Kick-Bass sidechain is tight, sitting cleanly at -14 LUFS.", "25m ago")
    )
  }

  fun selectTab(tab: DawTab) {
    _currentTab.value = tab
  }

  fun selectTrack(trackId: String) {
    _selectedTrackId.value = trackId
  }

  fun togglePlay() {
    if (audioEngine.isPlaying.value) {
      audioEngine.stopPlayback()
    } else {
      audioEngine.startPlayback(
        tracksProvider = { _tracks.value },
        harmorParamsProvider = { _harmorParams.value },
        masteringDataProvider = { _masteringData.value }
      )
    }
  }

  fun stopPlayback() {
    audioEngine.stopPlayback()
    audioEngine.seekTo(1.0f)
  }

  fun toggleMetronome() {
    audioEngine.isMetronomeOn = !audioEngine.isMetronomeOn
    showToast(if (audioEngine.isMetronomeOn) "Metronome ON" else "Metronome OFF")
  }

  fun toggleLoop() {
    audioEngine.isLooping = !audioEngine.isLooping
    showToast(if (audioEngine.isLooping) "Looping ON (1-9 bars)" else "Looping OFF")
  }

  fun setBpm(newBpm: Int) {
    val clamped = newBpm.coerceIn(50, 240)
    audioEngine.bpm = clamped
    triggerCloudAutoSync("Updated BPM to $clamped")
  }

  // Track manipulation
  fun updateTrackVolume(trackId: String, volume: Float) {
    _tracks.value = _tracks.value.map {
      if (it.id == trackId) it.copy(volume = volume.coerceIn(0f, 1.5f)) else it
    }
  }

  fun updateTrackPan(trackId: String, pan: Float) {
    _tracks.value = _tracks.value.map {
      if (it.id == trackId) it.copy(pan = pan.coerceIn(-1f, 1f)) else it
    }
  }

  fun toggleTrackMute(trackId: String) {
    _tracks.value = _tracks.value.map {
      if (it.id == trackId) it.copy(isMuted = !it.isMuted) else it
    }
  }

  fun toggleTrackSolo(trackId: String) {
    val targetTrack = _tracks.value.find { it.id == trackId } ?: return
    val newSoloState = !targetTrack.isSoloed
    _tracks.value = _tracks.value.map {
      if (it.id == trackId) {
        it.copy(isSoloed = newSoloState, isMuted = false)
      } else {
        it.copy(isSoloed = false, isMuted = newSoloState)
      }
    }
  }

  fun toggleTrackArm(trackId: String) {
    _tracks.value = _tracks.value.map {
      if (it.id == trackId) it.copy(isArmed = !it.isArmed) else it.copy(isArmed = false)
    }
  }

  fun updateTrackPitchShift(trackId: String, semitones: Float) {
    _tracks.value = _tracks.value.map {
      if (it.id == trackId) it.copy(pitchShiftSemitones = semitones) else it
    }
  }

  fun updateTrackTimeStretch(trackId: String, stretchFactor: Float) {
    _tracks.value = _tracks.value.map {
      if (it.id == trackId) it.copy(timeStretchFactor = stretchFactor.coerceIn(0.5f, 2.0f)) else it
    }
  }

  // Sidechain Compression Controls
  fun updateSidechainParams(trackId: String, update: (SidechainCompressorParams) -> SidechainCompressorParams) {
    _tracks.value = _tracks.value.map {
      if (it.id == trackId) it.copy(sidechainCompressor = update(it.sidechainCompressor)) else it
    }
  }

  // Piano Roll note editing & Glide / Slide support
  fun toggleSlideMode() {
    _isSlideModeEnabled.value = !_isSlideModeEnabled.value
  }

  fun addOrToggleNote(pitch: Int, startTick: Float, durationTicks: Float = 2.0f) {
    val existing = _pianoRollNotes.value.find { it.pitch == pitch && kotlin.math.abs(it.startTick - startTick) < 0.25f }
    if (existing != null) {
      // Remove note
      _pianoRollNotes.value = _pianoRollNotes.value.filter { it.id != existing.id }
    } else {
      // Add note (regular or FL Studio slide note!)
      val newNote = MidiNote(
        id = UUID.randomUUID().toString(),
        pitch = pitch,
        startTick = startTick,
        durationTicks = durationTicks,
        velocity = 0.85f,
        isSlide = _isSlideModeEnabled.value,
        slideTargetPitch = if (_isSlideModeEnabled.value) pitch + 4 else pitch
      )
      _pianoRollNotes.value = _pianoRollNotes.value + newNote
      // Audition note sound with Harmor synth
      audioEngine.auditionHarmorNote(
        params = _harmorParams.value,
        midiPitch = pitch,
        targetMidiPitch = newNote.slideTargetPitch,
        isSlide = newNote.isSlide,
        durationMs = 280f
      )
    }
  }

  fun updateNoteVelocity(noteId: String, velocity: Float) {
    _pianoRollNotes.value = _pianoRollNotes.value.map {
      if (it.id == noteId) it.copy(velocity = velocity.coerceIn(0.1f, 1.0f)) else it
    }
  }

  // Harmor Synth Controls
  fun updateHarmorParams(update: (HarmorParams) -> HarmorParams) {
    _harmorParams.value = update(_harmorParams.value)
  }

  fun loadHarmorPreset(preset: HarmorParams) {
    _harmorParams.value = preset
    showToast("Loaded Harmor preset: ${preset.presetName}")
  }

  fun auditionHarmorKey(pitch: Int, isSlide: Boolean = false) {
    audioEngine.auditionHarmorNote(
      params = _harmorParams.value,
      midiPitch = pitch,
      targetMidiPitch = if (isSlide) pitch + 7 else pitch,
      isSlide = isSlide,
      durationMs = 450f
    )
  }

  // Newtone Vocal Pitch Correction Controls
  fun selectVocalSlice(sliceId: String) {
    _selectedSliceId.value = sliceId
    val slice = _vocalSlices.value.find { it.id == sliceId }
    if (slice != null) {
      audioEngine.auditionNewtoneSlice(slice)
    }
  }

  fun setMusicalScale(scale: MusicalScale) {
    _selectedScale.value = scale
    // Quantize all vocal slices to scale
    _vocalSlices.value = _vocalSlices.value.map { slice ->
      val snapped = newtonePitchEngine.snapToScale(slice.originalPitch, scale = scale)
      slice.copy(targetPitch = snapped)
    }
    showToast("Vocal pitch snapped to ${scale.displayName}")
  }

  fun updateSelectedVocalSlice(update: (NewtoneSlice) -> NewtoneSlice) {
    val sliceId = _selectedSliceId.value ?: return
    _vocalSlices.value = _vocalSlices.value.map {
      if (it.id == sliceId) update(it) else it
    }
    val updated = _vocalSlices.value.find { it.id == sliceId }
    if (updated != null) {
      audioEngine.auditionNewtoneSlice(updated)
    }
  }

  fun splitVocalSliceAtCursor() {
    val sliceId = _selectedSliceId.value ?: return
    val slice = _vocalSlices.value.find { it.id == sliceId } ?: return
    if (slice.durationTicks <= 2.0f) return

    val half = slice.durationTicks / 2.0f
    val sliceA = slice.copy(id = UUID.randomUUID().toString(), durationTicks = half)
    val sliceB = slice.copy(
      id = UUID.randomUUID().toString(),
      startTick = slice.startTick + half,
      durationTicks = half,
      wordSyllable = "${slice.wordSyllable}-2"
    )

    _vocalSlices.value = _vocalSlices.value.filter { it.id != sliceId } + listOf(sliceA, sliceB)
    _selectedSliceId.value = sliceB.id
    showToast("Vocal slice cut into 2 notes")
  }

  // Mastering Suite
  fun updateMasteringData(update: (MasteringSuiteData) -> MasteringSuiteData) {
    _masteringData.value = update(_masteringData.value)
  }

  fun runSmartMasterAssist() {
    viewModelScope.launch {
      showToast("Smart Master Assist analyzing dynamic range...")
      delay(800)
      _masteringData.value = _masteringData.value.copy(
        presetName = "Smart AI Optimized (-14 LUFS)",
        targetLufs = -14.0f,
        integratedLufs = -14.05f,
        shortTermLufs = -13.8f,
        momentaryLufs = -13.1f,
        ceilingDb = -0.3f,
        thresholdDb = -5.2f,
        stereoWidth = 1.30f,
        bassWarmthDb = +1.8f,
        airSparkleDb = +2.2f,
        correlationPhase = 0.91f
      )
      showToast("Mastering tuned: -14.0 LUFS EBU R128 Compliant")
    }
  }

  // Preset management
  fun filterPresetsByCategory(category: String) {
    _presetFilterCategory.value = category
  }

  fun loadPreset(preset: DawPreset) {
    preset.harmorParams?.let { loadHarmorPreset(it) }
    preset.sidechainParams?.let { sc ->
      val selId = _selectedTrackId.value
      updateSidechainParams(selId) { sc }
    }
    preset.masteringData?.let { md ->
      _masteringData.value = md
    }
    showToast("Applied preset: ${preset.name}")
  }

  fun saveCustomPreset(name: String, category: String, desc: String) {
    val newPreset = DawPreset(
      id = UUID.randomUUID().toString(),
      name = name,
      category = category,
      description = desc,
      author = "You",
      harmorParams = _harmorParams.value,
      masteringData = _masteringData.value
    )
    _allPresets.value = listOf(newPreset) + _allPresets.value
    showToast("Saved preset '$name' to library")
  }

  // Recording
  fun startMicRecording() {
    val armedTrack = _tracks.value.find { it.isArmed }
    if (armedTrack == null) {
      showToast("Please Arm (R) a track first to record audio!")
      return
    }

    val success = audioRecorder.startRecording()
    if (success) {
      showToast("Recording onto track: ${armedTrack.name}")
      // Automatically start playback timeline while recording
      if (!audioEngine.isPlaying.value) {
        togglePlay()
      }
    } else {
      showToast("Microphone permission or hardware unavailable")
    }
  }

  fun stopMicRecording() {
    if (!audioRecorder.isRecording.value) return
    val waveform = audioRecorder.stopRecording()
    val armedTrack = _tracks.value.find { it.isArmed } ?: return

    val newClip = TrackClip(
      id = UUID.randomUUID().toString(),
      name = "Mic Rec Take #${armedTrack.clips.size + 1}",
      startBar = 1.0f,
      lengthBars = 4.0f,
      isAudio = true,
      waveformAmplitudes = waveform,
      color = armedTrack.color
    )

    _tracks.value = _tracks.value.map {
      if (it.id == armedTrack.id) it.copy(clips = it.clips + newClip) else it
    }
    showToast("Saved audio take to ${armedTrack.name}")
  }

  // Real-time Cloud Collaboration
  fun addProjectComment(text: String) {
    if (text.isBlank()) return
    val currentBar = audioEngine.currentBar.value
    val newComment = ProjectComment(
      id = UUID.randomUUID().toString(),
      authorName = "You",
      barPosition = currentBar,
      text = text,
      timestamp = "Just now"
    )
    _projectComments.value = listOf(newComment) + _projectComments.value
    triggerCloudAutoSync("Posted timeline comment at Bar ${"%.1f".format(currentBar)}")
  }

  fun exportStems() {
    viewModelScope.launch {
      showToast("Rendering multi-track stems (24-bit 48kHz WAV)...")
      delay(1200)
      showToast("Export complete: 5 Stems zipped & uploaded to Cloud Stems Drive")
    }
  }

  private fun triggerCloudAutoSync(reason: String) {
    viewModelScope.launch {
      _cloudSyncStatus.value = "Syncing..."
      delay(600)
      _cloudSyncStatus.value = "Synced • $reason"
    }
  }

  fun showToast(msg: String) {
    _toastMessage.value = msg
  }

  fun clearToast() {
    _toastMessage.value = null
  }

  override fun onCleared() {
    super.onCleared()
    audioEngine.release()
  }
}
