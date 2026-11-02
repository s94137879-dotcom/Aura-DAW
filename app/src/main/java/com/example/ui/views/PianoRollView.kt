package com.example.ui.views

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChangeHistory
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.MidiNote
import com.example.ui.theme.DarkStudioBg
import com.example.ui.theme.DarkStudioBorder
import com.example.ui.theme.DarkStudioCard
import com.example.ui.theme.DarkStudioHeader
import com.example.ui.theme.HarmorOrange
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.DawViewModel

private val NOTE_NAMES = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

@Composable
fun PianoRollView(
  viewModel: DawViewModel,
  modifier: Modifier = Modifier
) {
  val notes by viewModel.pianoRollNotes.collectAsState()
  val isSlideMode by viewModel.isSlideModeEnabled.collectAsState()
  val currentBar by viewModel.audioEngine.currentBar.collectAsState()
  val harmorParams by viewModel.harmorParams.collectAsState()

  // Pitch range: C2 (36) to B5 (83) -> 48 keys
  val pitches = (83 downTo 36).toList()
  val totalTicks = 32 // 2 full bars of 16th steps
  val stepWidth = 32.dp
  val keyHeight = 22.dp

  val horizontalScrollState = rememberScrollState()
  val verticalScrollState = rememberLazyListState(initialFirstVisibleItemIndex = 14) // Around C4

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkStudioBg)
  ) {
    // Piano Roll Toolbar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(DarkStudioHeader)
        .border(1.dp, DarkStudioBorder)
        .padding(horizontal = 8.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("PIANO ROLL", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)

        // FL Studio Slide Note Mode Toggle Button
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSlideMode) NeonAmber.copy(alpha = 0.25f) else DarkStudioCard)
            .border(1.dp, if (isSlideMode) NeonAmber else DarkStudioBorder, RoundedCornerShape(6.dp))
            .clickable { viewModel.toggleSlideMode() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("slide_note_toggle")
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.ChangeHistory,
              contentDescription = "Slide Mode",
              tint = if (isSlideMode) NeonAmber else TextSecondary,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (isSlideMode) "SLIDE ON (Glide)" else "SLIDE (Glide)",
              color = if (isSlideMode) NeonAmber else TextSecondary,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        // Snap selector
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(DarkStudioCard)
            .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
          Text("Snap: 1/16", color = TextSecondary, fontSize = 10.sp)
        }
      }

      // Quick Tools: Transpose Octave Up/Down, Audition
      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        FilledTonalButton(
          onClick = {
            notes.firstOrNull()?.let {
              viewModel.auditionHarmorKey(it.pitch, it.isSlide)
            }
          },
          colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = DarkStudioCard,
            contentColor = NeonCyan
          ),
          modifier = Modifier.height(28.dp)
        ) {
          Icon(Icons.Default.PlayArrow, contentDescription = "Audition", modifier = Modifier.size(12.dp))
          Spacer(modifier = Modifier.width(2.dp))
          Text("Audition", fontSize = 10.sp)
        }
      }
    }

    // Main Interactive Grid & Piano Keyboard
    Row(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
    ) {
      // Left: Piano Keyboard Keys (audition note on tap)
      LazyColumn(
        state = verticalScrollState,
        modifier = Modifier
          .width(54.dp)
          .fillMaxHeight()
          .background(DarkStudioHeader)
          .border(0.5.dp, DarkStudioBorder)
      ) {
        items(pitches, key = { it }) { pitch ->
          val noteIndex = pitch % 12
          val octave = (pitch / 12) - 1
          val noteName = "${NOTE_NAMES[noteIndex]}$octave"
          val isBlack = NOTE_NAMES[noteIndex].contains("#")

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(keyHeight)
              .background(if (isBlack) Color(0xFF1E212B) else Color(0xFFE2E8F0))
              .border(0.5.dp, DarkStudioBorder)
              .clickable {
                viewModel.auditionHarmorKey(pitch, isSlideMode)
              }
              .padding(start = 4.dp),
            contentAlignment = Alignment.CenterStart
          ) {
            Text(
              text = noteName,
              color = if (isBlack) TextSecondary else Color.Black,
              fontSize = 9.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = if (noteName.startsWith("C") && !isBlack) FontWeight.Bold else FontWeight.Normal
            )
          }
        }
      }

      // Right: Grid Canvas with tap gestures to add/remove notes
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
          .horizontalScroll(horizontalScrollState)
      ) {
        val totalGridWidth = stepWidth * totalTicks

        LazyColumn(
          state = verticalScrollState,
          modifier = Modifier
            .width(totalGridWidth)
            .fillMaxHeight()
        ) {
          items(pitches, key = { it }) { pitch ->
            val isBlackKeyRow = NOTE_NAMES[pitch % 12].contains("#")

            Box(
              modifier = Modifier
                .width(totalGridWidth)
                .height(keyHeight)
                .background(if (isBlackKeyRow) Color(0xFF10121A) else Color(0xFF141722))
                .border(0.25.dp, DarkStudioBorder.copy(alpha = 0.35f))
                .pointerInput(pitch) {
                  detectTapGestures { offset ->
                    val clickedTick = (offset.x / stepWidth.toPx()).toInt().toFloat()
                    viewModel.addOrToggleNote(pitch = pitch, startTick = clickedTick, durationTicks = 2f)
                  }
                }
            ) {
              // Draw grid lines
              Canvas(modifier = Modifier.fillMaxSize()) {
                val stepPx = stepWidth.toPx()
                for (t in 0..totalTicks) {
                  val x = t * stepPx
                  val isBeat = t % 4 == 0
                  val isBar = t % 16 == 0
                  drawLine(
                    color = if (isBar) DarkStudioBorder.copy(alpha = 0.8f)
                    else if (isBeat) DarkStudioBorder.copy(alpha = 0.4f)
                    else DarkStudioBorder.copy(alpha = 0.15f),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = if (isBar) 1.5f else 0.5f
                  )
                }

                // Render notes on this pitch row
                val rowNotes = notes.filter { it.pitch == pitch }
                rowNotes.forEach { note ->
                  val noteX = note.startTick * stepPx
                  val noteW = (note.durationTicks * stepPx).coerceAtLeast(stepPx * 0.8f)

                  val noteColor = if (note.isSlide) NeonAmber else NeonCyan

                  // Note pill
                  drawRoundRect(
                    color = noteColor.copy(alpha = 0.85f),
                    topLeft = Offset(noteX + 1f, 2f),
                    size = Size(noteW - 2f, size.height - 4f),
                    cornerRadius = CornerRadius(4f, 4f)
                  )

                  // FL Studio Slide Note marker (triangle glide badge)
                  if (note.isSlide) {
                    val triPath = Path().apply {
                      moveTo(noteX + noteW - 14f, size.height - 4f)
                      lineTo(noteX + noteW - 2f, size.height - 4f)
                      lineTo(noteX + noteW - 8f, 4f)
                      close()
                    }
                    drawPath(triPath, color = Color.White)
                  }
                }
              }
            }
          }
        }
      }
    }

    // Bottom Velocity & Glide Automation Lane
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(54.dp)
        .background(DarkStudioCard)
        .border(1.dp, DarkStudioBorder)
    ) {
      Box(
        modifier = Modifier
          .width(54.dp)
          .fillMaxHeight()
          .background(DarkStudioHeader)
          .padding(4.dp),
        contentAlignment = Alignment.Center
      ) {
        Text("VELOCITY / GLIDE", color = TextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
      }

      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
          .horizontalScroll(horizontalScrollState)
      ) {
        Canvas(modifier = Modifier.width(stepWidth * totalTicks).fillMaxHeight()) {
          val stepPx = stepWidth.toPx()
          notes.forEach { note ->
            val noteX = note.startTick * stepPx + stepPx * 0.4f
            val velHeight = note.velocity * (size.height - 10f)

            drawLine(
              color = if (note.isSlide) NeonAmber else NeonCyan,
              start = Offset(noteX, size.height),
              end = Offset(noteX, size.height - velHeight),
              strokeWidth = 3f
            )

            drawCircle(
              color = Color.White,
              radius = 3f,
              center = Offset(noteX, size.height - velHeight)
            )
          }
        }
      }
    }
  }
}
