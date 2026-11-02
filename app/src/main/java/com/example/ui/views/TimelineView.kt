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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Piano
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.DawTrack
import com.example.audio.TrackType
import com.example.ui.components.GainReductionMeter
import com.example.ui.components.WaveformCanvas
import com.example.ui.theme.DarkStudioBg
import com.example.ui.theme.DarkStudioBorder
import com.example.ui.theme.DarkStudioCard
import com.example.ui.theme.MeterRed
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.DawTab
import com.example.viewmodel.DawViewModel

@Composable
fun TimelineView(
  viewModel: DawViewModel,
  modifier: Modifier = Modifier
) {
  val tracks by viewModel.tracks.collectAsState()
  val selectedTrackId by viewModel.selectedTrackId.collectAsState()
  val currentBar by viewModel.audioEngine.currentBar.collectAsState()
  val gainReduction by viewModel.audioEngine.currentGainReduction.collectAsState()

  val timelineScrollState = rememberScrollState()
  val barWidth = 90.dp
  val totalBars = 12

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkStudioBg)
  ) {
    // Timeline Header Bar: Bar numbers & Playhead
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(32.dp)
        .background(DarkStudioCard)
        .border(0.5.dp, DarkStudioBorder)
    ) {
      // Left track header spacing
      Box(
        modifier = Modifier
          .width(160.dp)
          .fillMaxHeight()
          .background(DarkStudioCard)
          .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
      ) {
        Text("TRACKS / BUSSES", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
      }

      // Bar ruler numbers with horizontal sync
      Row(
        modifier = Modifier
          .weight(1f)
          .horizontalScroll(timelineScrollState)
      ) {
        for (b in 1..totalBars) {
          Box(
            modifier = Modifier
              .width(barWidth)
              .fillMaxHeight()
              .border(0.5.dp, DarkStudioBorder.copy(alpha = 0.5f))
              .clickable { viewModel.audioEngine.seekTo(b.toFloat()) }
              .padding(start = 4.dp, top = 6.dp)
          ) {
            Text(
              text = "Bar $b",
              color = if (currentBar.toInt() == b) NeonCyan else TextSecondary,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = if (currentBar.toInt() == b) FontWeight.Bold else FontWeight.Normal
            )
          }
        }
      }
    }

    // Multi-track list
    LazyColumn(
      modifier = Modifier.weight(1f)
    ) {
      items(tracks, key = { it.id }) { track ->
        val isSelected = track.id == selectedTrackId

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .border(0.5.dp, if (isSelected) NeonCyan.copy(alpha = 0.4f) else DarkStudioBorder)
            .background(if (isSelected) DarkStudioCard.copy(alpha = 0.8f) else DarkStudioBg)
            .clickable { viewModel.selectTrack(track.id) }
            .testTag("track_${track.id}")
        ) {
          // Track Header Pane (Name, Type Icon, M/S/R buttons, Vol slider)
          TrackHeaderPane(
            track = track,
            isSelected = isSelected,
            onMute = { viewModel.toggleTrackMute(track.id) },
            onSolo = { viewModel.toggleTrackSolo(track.id) },
            onArm = { viewModel.toggleTrackArm(track.id) },
            onVolume = { viewModel.updateTrackVolume(track.id, it) },
            onOpenPlugin = {
              when (track.type) {
                TrackType.HARMOR_SYNTH -> viewModel.selectTab(DawTab.HARMOR)
                TrackType.NEWTONE_VOCAL -> viewModel.selectTab(DawTab.NEWTONE)
                TrackType.MIDI -> viewModel.selectTab(DawTab.PIANO_ROLL)
                else -> viewModel.selectTab(DawTab.MIXER)
              }
            },
            modifier = Modifier
              .width(160.dp)
              .fillMaxHeight()
          )

          // Track Lanes & Clips
          Box(
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight()
              .horizontalScroll(timelineScrollState)
          ) {
            // Background Grid Lines
            Canvas(modifier = Modifier.width(barWidth * totalBars).fillMaxHeight()) {
              val bwPx = barWidth.toPx()
              for (i in 0..totalBars) {
                val x = i * bwPx
                drawLine(
                  color = DarkStudioBorder.copy(alpha = 0.4f),
                  start = Offset(x, 0f),
                  end = Offset(x, size.height),
                  strokeWidth = 1f
                )
                // Sub-beats (quarter notes)
                for (sub in 1..3) {
                  val subX = x + (bwPx / 4f) * sub
                  drawLine(
                    color = DarkStudioBorder.copy(alpha = 0.15f),
                    start = Offset(subX, 0f),
                    end = Offset(subX, size.height),
                    strokeWidth = 0.5f
                  )
                }
              }

              // Moving Playhead Line
              val playheadX = (currentBar - 1.0f) * bwPx
              if (playheadX in 0f..size.width) {
                drawLine(
                  color = NeonCyan,
                  start = Offset(playheadX, 0f),
                  end = Offset(playheadX, size.height),
                  strokeWidth = 2.5f
                )
              }
            }

            // Clips on lane
            Row(modifier = Modifier.width(barWidth * totalBars).fillMaxHeight()) {
              track.clips.forEach { clip ->
                val clipWidth = barWidth * clip.lengthBars
                Box(
                  modifier = Modifier
                    .width(clipWidth)
                    .fillMaxHeight()
                    .padding(vertical = 4.dp, horizontal = 2.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(track.color).copy(alpha = 0.25f))
                    .border(1.dp, Color(track.color).copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                    .clickable {
                      viewModel.selectTrack(track.id)
                      if (track.type == TrackType.HARMOR_SYNTH || track.type == TrackType.MIDI) {
                        viewModel.selectTab(DawTab.PIANO_ROLL)
                      } else if (track.type == TrackType.NEWTONE_VOCAL) {
                        viewModel.selectTab(DawTab.NEWTONE)
                      }
                    }
                    .padding(6.dp)
                ) {
                  Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                      Text(
                        text = clip.name,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                      )
                      Text(
                        text = "${clip.lengthBars} Bars",
                        color = TextSecondary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                      )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Waveform or MIDI Note preview inside clip
                    if (clip.isAudio && clip.waveformAmplitudes.isNotEmpty()) {
                      WaveformCanvas(
                        amplitudes = clip.waveformAmplitudes,
                        tint = Color(track.color),
                        modifier = Modifier.fillMaxWidth().height(36.dp)
                      )
                    } else {
                      // MIDI note miniature dashes
                      Row(
                        modifier = Modifier.fillMaxWidth().height(28.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        for (i in 0..12) {
                          val h = (12 + (i * 7) % 18).dp
                          Box(
                            modifier = Modifier
                              .width(8.dp)
                              .height(h)
                              .clip(RoundedCornerShape(2.dp))
                              .background(Color(track.color))
                          )
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    // Bottom Quick Precision PitchShift & Time Screat (Time Stretch) Bar
    val selectedTrack = tracks.find { it.id == selectedTrackId }
    if (selectedTrack != null) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(DarkStudioCard)
          .border(1.dp, DarkStudioBorder)
          .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(CircleShape)
              .background(Color(selectedTrack.color))
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = selectedTrack.name,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
        }

        // Sidechain routing label
        if (selectedTrack.sidechainCompressor.enabled) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text("SIDECHAIN: ", fontSize = 10.sp, color = NeonAmber, fontWeight = FontWeight.Bold)
            Text("Ducked by Kick", fontSize = 10.sp, color = TextSecondary)
            Spacer(modifier = Modifier.width(8.dp))
            GainReductionMeter(
              gainReductionDb = gainReduction,
              modifier = Modifier.width(70.dp)
            )
          }
        }

        // Pitch Shift & Time Stretch (Time Screat)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text("PITCH: ", fontSize = 10.sp, color = TextSecondary)
            Text(
              "${if (selectedTrack.pitchShiftSemitones >= 0) "+" else ""}${"%.1f".format(selectedTrack.pitchShiftSemitones)} st",
              color = NeonCyan,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Text("TIME SCREAT: ", fontSize = 10.sp, color = TextSecondary)
            Text(
              "${"%.2f".format(selectedTrack.timeStretchFactor)}x",
              color = NeonAmber,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold
            )
          }

          FilledTonalButton(
            onClick = {
              when (selectedTrack.type) {
                TrackType.HARMOR_SYNTH -> viewModel.selectTab(DawTab.HARMOR)
                TrackType.NEWTONE_VOCAL -> viewModel.selectTab(DawTab.NEWTONE)
                else -> viewModel.selectTab(DawTab.PIANO_ROLL)
              }
            },
            colors = ButtonDefaults.filledTonalButtonColors(
              containerColor = NeonCyan.copy(alpha = 0.2f),
              contentColor = NeonCyan
            ),
            modifier = Modifier.height(28.dp)
          ) {
            Text("Edit Notes / VST", fontSize = 11.sp)
          }
        }
      }
    }
  }
}

@Composable
private fun TrackHeaderPane(
  track: DawTrack,
  isSelected: Boolean,
  onMute: () -> Unit,
  onSolo: () -> Unit,
  onArm: () -> Unit,
  onVolume: (Float) -> Unit,
  onOpenPlugin: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .background(DarkStudioCard)
      .border(0.5.dp, DarkStudioBorder)
  ) {
    // Left color strip
    Box(
      modifier = Modifier
        .width(4.dp)
        .fillMaxHeight()
        .background(Color(track.color))
    )

    Column(
      modifier = Modifier
        .weight(1f)
        .fillMaxHeight()
        .padding(horizontal = 6.dp, vertical = 4.dp),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = track.name,
          color = Color.White,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
          modifier = Modifier.weight(1f)
        )
        // Plugin / Type Icon
        Icon(
          imageVector = when (track.type) {
            TrackType.HARMOR_SYNTH -> Icons.Default.Tune
            TrackType.NEWTONE_VOCAL -> Icons.Default.Mic
            TrackType.DRUMS -> Icons.Default.MusicNote
            else -> Icons.Default.Piano
          },
          contentDescription = null,
          tint = Color(track.color),
          modifier = Modifier
            .size(16.dp)
            .clickable { onOpenPlugin() }
        )
      }

      // Mute (M), Solo (S), Record Arm (R) Buttons
      Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Mute
        Box(
          modifier = Modifier
            .size(22.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(if (track.isMuted) MeterRed else DarkStudioBg)
            .clickable { onMute() },
          contentAlignment = Alignment.Center
        ) {
          Text("M", color = if (track.isMuted) Color.White else TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }

        // Solo
        Box(
          modifier = Modifier
            .size(22.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(if (track.isSoloed) NeonAmber else DarkStudioBg)
            .clickable { onSolo() },
          contentAlignment = Alignment.Center
        ) {
          Text("S", color = if (track.isSoloed) Color.Black else TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }

        // Record Arm
        Box(
          modifier = Modifier
            .size(22.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(if (track.isArmed) MeterRed else DarkStudioBg)
            .clickable { onArm() },
          contentAlignment = Alignment.Center
        ) {
          Text("R", color = if (track.isArmed) Color.White else TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.width(2.dp))
        Text(
          text = "${(track.volume * 100).toInt()}%",
          fontSize = 9.sp,
          color = TextSecondary,
          fontFamily = FontFamily.Monospace
        )
      }

      // Mini Volume Slider
      Slider(
        value = track.volume,
        onValueChange = onVolume,
        valueRange = 0f..1.2f,
        colors = SliderDefaults.colors(
          thumbColor = Color(track.color),
          activeTrackColor = Color(track.color),
          inactiveTrackColor = DarkStudioBorder
        ),
        modifier = Modifier.fillMaxWidth().height(16.dp)
      )
    }
  }
}
