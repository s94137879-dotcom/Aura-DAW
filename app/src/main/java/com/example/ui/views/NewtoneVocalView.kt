package com.example.ui.views

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.MusicalScale
import com.example.ui.components.RotaryKnob
import com.example.ui.theme.DarkStudioBg
import com.example.ui.theme.DarkStudioBorder
import com.example.ui.theme.DarkStudioCard
import com.example.ui.theme.DarkStudioHeader
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NewtonePurple
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.DawViewModel

private val NOTE_NAMES = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

@Composable
fun NewtoneVocalView(
  viewModel: DawViewModel,
  modifier: Modifier = Modifier
) {
  val slices by viewModel.vocalSlices.collectAsState()
  val selectedSliceId by viewModel.selectedSliceId.collectAsState()
  val selectedScale by viewModel.selectedScale.collectAsState()

  val selectedSlice = slices.find { it.id == selectedSliceId } ?: slices.firstOrNull()

  // Pitch range for vocals: G3 (55) to E5 (76)
  val pitches = (76 downTo 55).toList()
  val pitchRowHeight = 22.dp
  val tickStepWidth = 30.dp
  val totalTicks = 24

  val gridHorizontalScroll = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkStudioBg)
  ) {
    // Top Newtone Header & Scale Snap Picker
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(DarkStudioHeader)
        .border(1.dp, DarkStudioBorder)
        .padding(horizontal = 8.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Mic, contentDescription = null, tint = NewtonePurple, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "NEWTONE",
          color = NewtonePurple,
          fontWeight = FontWeight.Black,
          fontSize = 14.sp,
          letterSpacing = 1.sp
        )
        Text(
          text = " • Pitch Correction Editor",
          color = TextSecondary,
          fontSize = 11.sp
        )
      }

      // Scale Snapper selector
      val scaleScrollState = rememberScrollState()
      Row(
        modifier = Modifier.horizontalScroll(scaleScrollState),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        MusicalScale.entries.take(4).forEach { scale ->
          val isSel = selectedScale == scale
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(if (isSel) NewtonePurple.copy(alpha = 0.3f) else DarkStudioCard)
              .clickable { viewModel.setMusicalScale(scale) }
              .padding(horizontal = 6.dp, vertical = 4.dp)
          ) {
            Text(
              text = scale.name.replace("_", " "),
              color = if (isSel) NewtonePurple else TextSecondary,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    // Main Newtone Pitch Grid & Vocal Slices
    Row(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
    ) {
      // Left: Pitch Track Labels (e.g. C4, D4, E4)
      LazyColumn(
        modifier = Modifier
          .width(52.dp)
          .fillMaxHeight()
          .background(DarkStudioHeader)
          .border(0.5.dp, DarkStudioBorder)
      ) {
        items(pitches, key = { it }) { pitch ->
          val noteIndex = pitch % 12
          val octave = (pitch / 12) - 1
          val noteName = "${NOTE_NAMES[noteIndex]}$octave"
          val isBlack = noteName.contains("#")

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(pitchRowHeight)
              .background(if (isBlack) Color(0xFF13151D) else Color(0xFF1A1D27))
              .border(0.25.dp, DarkStudioBorder)
              .padding(start = 4.dp),
            contentAlignment = Alignment.CenterStart
          ) {
            Text(
              text = noteName,
              color = if (isBlack) TextSecondary else TextPrimary,
              fontSize = 9.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = if (noteName.startsWith("C")) FontWeight.Bold else FontWeight.Normal
            )
          }
        }
      }

      // Right: Sliced Vocal Notes on Pitch Grid with Pitch Curve Overlay
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
          .horizontalScroll(gridHorizontalScroll)
      ) {
        val gridWidth = tickStepWidth * totalTicks

        LazyColumn(
          modifier = Modifier
            .width(gridWidth)
            .fillMaxHeight()
        ) {
          items(pitches, key = { it }) { pitch ->
            val isBlackRow = NOTE_NAMES[pitch % 12].contains("#")

            Box(
              modifier = Modifier
                .width(gridWidth)
                .height(pitchRowHeight)
                .background(if (isBlackRow) Color(0xFF0F1118) else Color(0xFF131620))
                .border(0.25.dp, DarkStudioBorder.copy(alpha = 0.35f))
            )
          }
        }

        // Overlay: Vocal Note Blocks & Micro-Pitch Curve
        Canvas(modifier = Modifier.width(gridWidth).fillMaxHeight()) {
          val topPitch = pitches.first()
          val rowHeightPx = pitchRowHeight.toPx()
          val stepPx = tickStepWidth.toPx()

          slices.forEach { slice ->
            val isSelected = slice.id == selectedSliceId

            // Effective pitch height
            val pitchOffsetFromTop = (topPitch - slice.targetPitch).toFloat()
            val noteY = pitchOffsetFromTop * rowHeightPx
            val noteX = slice.startTick * stepPx
            val noteW = slice.durationTicks * stepPx * slice.timeStretch

            // Draw Note Slice Box
            val boxColor = if (isSelected) NewtonePurple else NewtonePurple.copy(alpha = 0.5f)
            drawRoundRect(
              color = boxColor,
              topLeft = Offset(noteX + 2f, noteY + 2f),
              size = Size(noteW - 4f, rowHeightPx - 4f),
              cornerRadius = CornerRadius(4f, 4f)
            )

            // Draw micro-pitch contour curve (Orange / Cyan trajectory)
            val path = Path()
            val points = slice.pitchCurve
            if (points.isNotEmpty()) {
              for (pIdx in points.indices) {
                val pt = points[pIdx]
                val ptY = (topPitch - pt) * rowHeightPx + (rowHeightPx / 2f)
                val ptX = noteX + (pIdx.toFloat() / (points.size - 1).coerceAtLeast(1)) * noteW
                if (pIdx == 0) path.moveTo(ptX, ptY) else path.lineTo(ptX, ptY)
              }
              drawPath(
                path = path,
                color = if (isSelected) NeonAmber else Color.White,
                style = Stroke(width = 2.5f, cap = StrokeCap.Round)
              )
            }
          }
        }

        // Clickable interactive note hitboxes
        slices.forEach { slice ->
          val topPitch = pitches.first()
          val noteY = (topPitch - slice.targetPitch) * 22
          val noteX = (slice.startTick * 30).toInt()
          val noteW = (slice.durationTicks * 30 * slice.timeStretch).toInt()

          Box(
            modifier = Modifier
              .padding(start = noteX.dp, top = noteY.dp)
              .size(width = noteW.dp, height = pitchRowHeight)
              .clickable { viewModel.selectVocalSlice(slice.id) }
              .padding(horizontal = 4.dp),
            contentAlignment = Alignment.CenterStart
          ) {
            Text(
              text = slice.wordSyllable,
              color = Color.White,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              maxLines = 1
            )
          }
        }
      }
    }

    // Bottom Detailed Precision Vocal Parameters:
    // Center Pitch, Formant Shift, Pitch Shift, Time Stretch (Time Screat), Vibrato, Cut Tool
    if (selectedSlice != null) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(DarkStudioCard)
          .border(1.dp, DarkStudioBorder)
          .padding(10.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "EDITING NOTE: ",
              color = TextSecondary,
              fontSize = 11.sp
            )
            Text(
              text = "\"${selectedSlice.wordSyllable}\" (MIDI ${selectedSlice.targetPitch})",
              color = NewtonePurple,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
          }

          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            // Cut / Slice Tool Button
            FilledTonalButton(
              onClick = { viewModel.splitVocalSliceAtCursor() },
              colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = DarkStudioHeader,
                contentColor = NeonAmber
              ),
              modifier = Modifier.height(28.dp).testTag("cut_vocal_slice")
            ) {
              Icon(Icons.Default.ContentCut, contentDescription = null, modifier = Modifier.size(12.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Cut Note", fontSize = 10.sp)
            }

            // Audition Note Button
            FilledTonalButton(
              onClick = { viewModel.audioEngine.auditionNewtoneSlice(selectedSlice) },
              colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = NewtonePurple.copy(alpha = 0.25f),
                contentColor = NewtonePurple
              ),
              modifier = Modifier.height(28.dp).testTag("audition_vocal_slice")
            ) {
              Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(12.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Audition", fontSize = 10.sp)
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Precision Knobs: Center Pitch, Formant, Pitch Shift, Time Screat, Vibrato
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceAround
        ) {
          // Center Pitch (Snap to Grid Auto-tune strength)
          RotaryKnob(
            value = selectedSlice.centerPitchAmount,
            onValueChange = { v ->
              viewModel.updateSelectedVocalSlice { it.copy(centerPitchAmount = v) }
            },
            label = "CENTER PITCH",
            displayValue = "${(selectedSlice.centerPitchAmount * 100).toInt()}%",
            tintColor = NewtonePurple,
            testTag = "center_pitch_knob"
          )

          // Formant Shift (-12 to +12 semitones)
          RotaryKnob(
            value = ((selectedSlice.formantShift + 12f) / 24f).coerceIn(0f, 1f),
            onValueChange = { v ->
              val semitones = (v * 24f) - 12f
              viewModel.updateSelectedVocalSlice { it.copy(formantShift = semitones) }
            },
            label = "FORMANT",
            displayValue = "${if (selectedSlice.formantShift >= 0) "+" else ""}${"%.1f".format(selectedSlice.formantShift)} st",
            tintColor = NeonCyan,
            testTag = "formant_shift_knob"
          )

          // Pitch Shift (Semitones -12 to +12)
          RotaryKnob(
            value = ((selectedSlice.pitchShift + 12f) / 24f).coerceIn(0f, 1f),
            onValueChange = { v ->
              val st = (v * 24f) - 12f
              viewModel.updateSelectedVocalSlice { it.copy(pitchShift = st) }
            },
            label = "PITCH SHIFT",
            displayValue = "${if (selectedSlice.pitchShift >= 0) "+" else ""}${"%.1f".format(selectedSlice.pitchShift)} st",
            tintColor = NeonAmber,
            testTag = "pitch_shift_knob"
          )

          // Time Screat / Time Stretch (0.5x to 2.0x)
          RotaryKnob(
            value = ((selectedSlice.timeStretch - 0.5f) / 1.5f).coerceIn(0f, 1f),
            onValueChange = { v ->
              val factor = 0.5f + v * 1.5f
              viewModel.updateSelectedVocalSlice { it.copy(timeStretch = factor) }
            },
            label = "TIME SCREAT",
            displayValue = "${"%.2f".format(selectedSlice.timeStretch)}x",
            tintColor = Color(0xFF10B981),
            testTag = "time_stretch_knob"
          )

          // Vibrato Depth
          RotaryKnob(
            value = selectedSlice.vibratoDepth,
            onValueChange = { v ->
              viewModel.updateSelectedVocalSlice { it.copy(vibratoDepth = v) }
            },
            label = "VIBRATO",
            displayValue = "${(selectedSlice.vibratoDepth * 100).toInt()}%",
            tintColor = TextPrimary
          )
        }
      }
    }
  }
}
