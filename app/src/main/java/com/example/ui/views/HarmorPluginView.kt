package com.example.ui.views

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChangeHistory
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.FilterType
import com.example.audio.HarmonicWaveform
import com.example.synth.HarmorPresets
import com.example.ui.components.RotaryKnob
import com.example.ui.theme.CoralRed
import com.example.ui.theme.DarkStudioBg
import com.example.ui.theme.DarkStudioBorder
import com.example.ui.theme.DarkStudioCard
import com.example.ui.theme.DarkStudioHeader
import com.example.ui.theme.HarmorOrange
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.DawViewModel

@Composable
fun HarmorPluginView(
  viewModel: DawViewModel,
  modifier: Modifier = Modifier
) {
  val params by viewModel.harmorParams.collectAsState()
  val mainScrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkStudioBg)
      .verticalScroll(mainScrollState)
      .padding(12.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // Top Plugin Banner & Preset Switcher
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .background(
          Brush.horizontalGradient(
            listOf(HarmorOrange.copy(alpha = 0.25f), DarkStudioCard)
          )
        )
        .border(1.dp, HarmorOrange.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
        .padding(10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Tune, contentDescription = null, tint = HarmorOrange, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text(
            text = "HARMOR",
            color = HarmorOrange,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
          )
          Text(
            text = "Additive / Subtractive Synthesizer Engine",
            color = TextSecondary,
            fontSize = 10.sp
          )
        }
      }

      // Preset Selector Chips
      val presetScrollState = rememberScrollState()
      Row(
        modifier = Modifier.horizontalScroll(presetScrollState),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        HarmorPresets.list.forEach { p ->
          val isCurrent = p.presetName == params.presetName
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(if (isCurrent) HarmorOrange else DarkStudioBg)
              .clickable { viewModel.loadHarmorPreset(p) }
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = p.presetName,
              color = if (isCurrent) Color.Black else TextSecondary,
              fontSize = 10.sp,
              fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
            )
          }
        }
      }
    }

    // 1. Harmor Additive Partials Graph & Waveform Selector
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .background(DarkStudioCard)
        .border(1.dp, DarkStudioBorder, RoundedCornerShape(8.dp))
        .padding(10.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.GraphicEq, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("PARTIALS SPECTRUM (Harmonic Additive)", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        // Waveform Selector
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          HarmonicWaveform.entries.take(4).forEach { wf ->
            val isSel = params.waveform == wf
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(if (isSel) NeonCyan.copy(alpha = 0.2f) else DarkStudioBg)
                .clickable { viewModel.updateHarmorParams { it.copy(waveform = wf) } }
                .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
              Text(
                text = wf.name,
                color = if (isSel) NeonCyan else TextSecondary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Interactive Harmor Harmonic Partials Graph
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(90.dp)
          .clip(RoundedCornerShape(6.dp))
          .background(DarkStudioBg)
          .padding(6.dp)
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val numBars = params.partials.size.coerceAtLeast(16)
          val barW = (size.width / numBars) * 0.75f
          val spacing = (size.width / numBars)

          for (i in 0 until numBars) {
            val amp = params.partials.getOrElse(i) { 1f / (i + 1) } * params.brightness
            val barH = (amp.coerceIn(0.02f, 1f) * (size.height - 4f))
            val x = i * spacing + (spacing - barW) / 2f
            val y = size.height - barH

            val barColor = when {
              i == 0 -> HarmorOrange
              i % 2 == 0 -> NeonCyan
              else -> SkyBlue
            }

            drawRoundRect(
              color = barColor,
              topLeft = Offset(x, y),
              size = Size(barW, barH),
              cornerRadius = CornerRadius(2f, 2f)
            )
          }
        }
      }
    }

    // 2. Harmor Signature Modules: Prism, Blur, Pluck, Brightness
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .background(DarkStudioCard)
        .border(1.dp, DarkStudioBorder, RoundedCornerShape(8.dp))
        .padding(10.dp),
      horizontalArrangement = Arrangement.SpaceAround
    ) {
      // Harmonic Prism (detunes partials inharmonically)
      RotaryKnob(
        value = params.prism,
        onValueChange = { v -> viewModel.updateHarmorParams { it.copy(prism = v) } },
        label = "PRISM",
        displayValue = "${(params.prism * 100).toInt()}%",
        tintColor = HarmorOrange,
        testTag = "harmor_prism"
      )

      // Harmonic Blur (smears partials in time/freq)
      RotaryKnob(
        value = params.blur,
        onValueChange = { v -> viewModel.updateHarmorParams { it.copy(blur = v) } },
        label = "BLUR",
        displayValue = "${(params.blur * 100).toInt()}%",
        tintColor = NeonCyan,
        testTag = "harmor_blur"
      )

      // Pluck (exponential high harmonic dampening)
      RotaryKnob(
        value = params.pluckDecay,
        onValueChange = { v -> viewModel.updateHarmorParams { it.copy(pluckDecay = v) } },
        label = "PLUCK",
        displayValue = "${(params.pluckDecay * 100).toInt()}%",
        tintColor = NeonAmber,
        testTag = "harmor_pluck"
      )

      // Brightness
      RotaryKnob(
        value = params.brightness,
        onValueChange = { v -> viewModel.updateHarmorParams { it.copy(brightness = v) } },
        label = "BRIGHT",
        displayValue = "${(params.brightness * 100).toInt()}%",
        tintColor = Color.White
      )
    }

    // 3. Filter Section & Unison Section
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Filter Module
      Column(
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(8.dp))
          .background(DarkStudioCard)
          .border(1.dp, DarkStudioBorder, RoundedCornerShape(8.dp))
          .padding(10.dp)
      ) {
        Text("FILTER (State-Variable)", color = SkyBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceAround
        ) {
          RotaryKnob(
            value = (params.filterCutoff / 18000f).coerceIn(0f, 1f),
            onValueChange = { v -> viewModel.updateHarmorParams { it.copy(filterCutoff = 20f + v * 18000f) } },
            label = "CUTOFF",
            displayValue = "${params.filterCutoff.toInt()} Hz",
            tintColor = SkyBlue
          )
          RotaryKnob(
            value = params.filterResonance,
            onValueChange = { v -> viewModel.updateHarmorParams { it.copy(filterResonance = v) } },
            label = "RES",
            displayValue = "${(params.filterResonance * 100).toInt()}%",
            tintColor = CoralRed
          )
        }
      }

      // Unison Module
      Column(
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(8.dp))
          .background(DarkStudioCard)
          .border(1.dp, DarkStudioBorder, RoundedCornerShape(8.dp))
          .padding(10.dp)
      ) {
        Text("UNISON (${params.unisonVoices} Voices)", color = MatrixGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceAround
        ) {
          RotaryKnob(
            value = params.unisonDetune,
            onValueChange = { v -> viewModel.updateHarmorParams { it.copy(unisonDetune = v) } },
            label = "DETUNE",
            displayValue = "${(params.unisonDetune * 100).toInt()}%",
            tintColor = MatrixGreen
          )
          RotaryKnob(
            value = params.unisonPanSpread,
            onValueChange = { v -> viewModel.updateHarmorParams { it.copy(unisonPanSpread = v) } },
            label = "SPREAD",
            displayValue = "${(params.unisonPanSpread * 100).toInt()}%",
            tintColor = NeonAmber
          )
        }
      }
    }

    // 4. ADSR Amplitude Envelopes & Phaser
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .background(DarkStudioCard)
        .border(1.dp, DarkStudioBorder, RoundedCornerShape(8.dp))
        .padding(10.dp),
      horizontalArrangement = Arrangement.SpaceAround
    ) {
      RotaryKnob(
        value = (params.attackMs / 1000f).coerceIn(0f, 1f),
        onValueChange = { v -> viewModel.updateHarmorParams { it.copy(attackMs = v * 1000f) } },
        label = "ATTACK",
        displayValue = "${params.attackMs.toInt()}ms",
        tintColor = TextPrimary
      )
      RotaryKnob(
        value = (params.decayMs / 2000f).coerceIn(0f, 1f),
        onValueChange = { v -> viewModel.updateHarmorParams { it.copy(decayMs = v * 2000f) } },
        label = "DECAY",
        displayValue = "${params.decayMs.toInt()}ms",
        tintColor = TextPrimary
      )
      RotaryKnob(
        value = params.sustainLevel,
        onValueChange = { v -> viewModel.updateHarmorParams { it.copy(sustainLevel = v) } },
        label = "SUSTAIN",
        displayValue = "${(params.sustainLevel * 100).toInt()}%",
        tintColor = TextPrimary
      )
      RotaryKnob(
        value = (params.releaseMs / 2000f).coerceIn(0f, 1f),
        onValueChange = { v -> viewModel.updateHarmorParams { it.copy(releaseMs = v * 2000f) } },
        label = "RELEASE",
        displayValue = "${params.releaseMs.toInt()}ms",
        tintColor = TextPrimary
      )
      RotaryKnob(
        value = params.phaserMix,
        onValueChange = { v -> viewModel.updateHarmorParams { it.copy(phaserMix = v) } },
        label = "PHASER",
        displayValue = "${(params.phaserMix * 100).toInt()}%",
        tintColor = HarmorOrange
      )
    }

    // 5. Interactive Audition Keyboard Pad (Tap notes or slides to hear in real-time!)
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .background(DarkStudioCard)
        .border(1.dp, DarkStudioBorder, RoundedCornerShape(8.dp))
        .padding(10.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.VolumeUp, contentDescription = null, tint = HarmorOrange, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("INTERACTIVE AUDITION KEYBOARD (Real-Time Synthesis)", color = HarmorOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Text("Tap keys to test sound & glide slides", color = TextSecondary, fontSize = 10.sp)
      }

      Spacer(modifier = Modifier.height(8.dp))

      // 1-Octave White & Black Keys
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(68.dp)
          .clip(RoundedCornerShape(6.dp))
          .background(DarkStudioHeader)
          .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
      ) {
        val keys = listOf(
          Pair(60, "C4"), Pair(62, "D4"), Pair(64, "E4"), Pair(65, "F4"),
          Pair(67, "G4"), Pair(69, "A4"), Pair(71, "B4"), Pair(72, "C5")
        )
        keys.forEach { (pitch, name) ->
          Box(
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight()
              .clip(RoundedCornerShape(4.dp))
              .background(Color(0xFFE2E8F0))
              .clickable { viewModel.auditionHarmorKey(pitch) }
              .padding(bottom = 6.dp),
            contentAlignment = Alignment.BottomCenter
          ) {
            Text(text = name, color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Slide Audition Button
      FilledTonalButton(
        onClick = { viewModel.auditionHarmorKey(60, isSlide = true) },
        colors = ButtonDefaults.filledTonalButtonColors(
          containerColor = NeonAmber.copy(alpha = 0.25f),
          contentColor = NeonAmber
        ),
        modifier = Modifier.fillMaxWidth().height(36.dp)
      ) {
        Icon(Icons.Default.ChangeHistory, contentDescription = null, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Test FL Studio Portamento Glide (C4 -> G4)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
      }
    }
  }
}
