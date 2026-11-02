package com.example

import com.example.audio.HarmorParams
import com.example.audio.MusicalScale
import com.example.synth.HarmorPresets
import com.example.synth.HarmorSynthEngine
import com.example.vocal.NewtonePitchEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun testHarmorSynthesisPcmOutput() {
    val engine = HarmorSynthEngine()
    val params = HarmorPresets.list.first()

    // Test standard note rendering
    val pcm = engine.renderHarmorNotePcm(
      params = params,
      baseFreq = 261.63f, // C4
      targetFreq = 261.63f,
      durationMs = 100f,
      isSlide = false
    )
    assertNotNull(pcm)
    assertTrue("PCM buffer should contain generated stereo samples", pcm.isNotEmpty())

    // Test FL Studio portamento glide slide note rendering
    val slidePcm = engine.renderHarmorNotePcm(
      params = params,
      baseFreq = 261.63f,
      targetFreq = 329.63f, // E4
      durationMs = 150f,
      isSlide = true
    )
    assertNotNull(slidePcm)
    assertTrue("Slide PCM buffer should be non-empty", slidePcm.isNotEmpty())
  }

  @Test
  fun testNewtoneScaleSnapping() {
    val engine = NewtonePitchEngine()

    // In C Natural Minor (C, D, Eb, F, G, Ab, Bb) -> intervals 0, 2, 3, 5, 7, 8, 10
    // Test pitch 60 (C4)
    val snappedC = engine.snapToScale(60f, rootNote = 0, scale = MusicalScale.NATURAL_MINOR)
    assertEquals(60, snappedC)

    // Test slightly sharp pitch 60.4
    val snappedNear = engine.snapToScale(60.4f, rootNote = 0, scale = MusicalScale.NATURAL_MINOR)
    assertEquals(60, snappedNear)

    // Test E natural (64) which is not in C Natural Minor (Eb=63 should be snapped to)
    val snappedE = engine.snapToScale(64f, rootNote = 0, scale = MusicalScale.NATURAL_MINOR)
    assertTrue("E4 should snap to either Eb4(63) or F4(65)", snappedE == 63 || snappedE == 65)
  }

  @Test
  fun testNewtoneVocalSliceRendering() {
    val engine = NewtonePitchEngine()
    val slices = engine.generateDefaultVocalSlices()
    assertTrue(slices.isNotEmpty())

    val firstSlice = slices.first()
    val vocalPcm = engine.renderVocalSlicePcm(firstSlice)
    assertNotNull(vocalPcm)
    assertTrue(vocalPcm.isNotEmpty())
  }
}
