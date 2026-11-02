package com.example.ui.views

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.VuPeakMeter
import com.example.ui.theme.DarkStudioBg
import com.example.ui.theme.DarkStudioBorder
import com.example.ui.theme.DarkStudioCard
import com.example.ui.theme.DarkStudioHeader
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.MeterRed
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.DawTab
import com.example.viewmodel.DawViewModel

@Composable
fun TopTransportBar(
  viewModel: DawViewModel,
  onOpenPresets: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isPlaying by viewModel.audioEngine.isPlaying.collectAsState()
  val currentBar by viewModel.audioEngine.currentBar.collectAsState()
  val masterPeak by viewModel.audioEngine.masterPeak.collectAsState()
  val isRecording by viewModel.audioRecorder.isRecording.collectAsState()
  val cloudStatus by viewModel.cloudSyncStatus.collectAsState()
  val currentTab by viewModel.currentTab.collectAsState()

  val barInt = currentBar.toInt()
  val beatInt = ((currentBar - barInt) * 4f).toInt() + 1
  val tickInt = (((currentBar - barInt) * 16f) % 4f * 24f).toInt()

  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(DarkStudioHeader)
      .border(1.dp, DarkStudioBorder)
  ) {
    // Upper Row: Title, Song position, Master VU, Transport & Cloud status
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 10.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // Song Position Counter (Bar : Beat : Tick)
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .clip(RoundedCornerShape(6.dp))
          .background(DarkStudioCard)
          .border(1.dp, DarkStudioBorder, RoundedCornerShape(6.dp))
          .padding(horizontal = 8.dp, vertical = 4.dp)
      ) {
        Text(
          text = "%02d".format(barInt),
          color = NeonCyan,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          fontSize = 15.sp
        )
        Text(":", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 2.dp))
        Text(
          text = "%02d".format(beatInt),
          color = NeonCyan,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          fontSize = 15.sp
        )
        Text(":", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 2.dp))
        Text(
          text = "%02d".format(tickInt),
          color = TextSecondary,
          fontFamily = FontFamily.Monospace,
          fontSize = 13.sp
        )
      }

      // Transport Buttons: Play, Stop, Rec, Loop, Metronome
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        // Play / Pause Button
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isPlaying) NeonCyan else DarkStudioCard)
            .clickable { viewModel.togglePlay() }
            .testTag("play_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Play/Pause",
            tint = if (isPlaying) DarkStudioHeader else Color.White,
            modifier = Modifier.size(24.dp)
          )
        }

        // Stop Button
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(DarkStudioCard)
            .clickable { viewModel.stopPlayback() }
            .testTag("stop_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Stop,
            contentDescription = "Stop",
            tint = TextPrimary,
            modifier = Modifier.size(20.dp)
          )
        }

        // Record Button (Mic Recording)
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isRecording) MeterRed else DarkStudioCard)
            .clickable {
              if (isRecording) viewModel.stopMicRecording() else viewModel.startMicRecording()
            }
            .testTag("record_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.FiberManualRecord,
            contentDescription = "Record",
            tint = if (isRecording) Color.White else MeterRed,
            modifier = Modifier.size(20.dp)
          )
        }

        // Loop Toggle
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (viewModel.audioEngine.isLooping) NeonAmber.copy(alpha = 0.2f) else DarkStudioCard)
            .clickable { viewModel.toggleLoop() },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Repeat,
            contentDescription = "Loop",
            tint = if (viewModel.audioEngine.isLooping) NeonAmber else TextSecondary,
            modifier = Modifier.size(18.dp)
          )
        }

        // Metronome Toggle
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (viewModel.audioEngine.isMetronomeOn) MatrixGreen.copy(alpha = 0.2f) else DarkStudioCard)
            .clickable { viewModel.toggleMetronome() },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.MusicNote,
            contentDescription = "Metronome",
            tint = if (viewModel.audioEngine.isMetronomeOn) MatrixGreen else TextSecondary,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      // Tempo BPM selector
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .clip(RoundedCornerShape(6.dp))
          .background(DarkStudioCard)
          .padding(horizontal = 6.dp, vertical = 4.dp)
      ) {
        Text(
          text = "${viewModel.audioEngine.bpm}",
          color = NeonAmber,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          fontSize = 14.sp
        )
        Text(" BPM", color = TextSecondary, fontSize = 10.sp)
      }

      // Master Output VU Peak Meter
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        VuPeakMeter(
          peakLevel = masterPeak,
          modifier = Modifier.width(54.dp).height(8.dp)
        )
        Text("MAIN", fontSize = 8.sp, color = TextSecondary)
      }

      // Presets & Cloud Status
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(NeonCyan.copy(alpha = 0.15f))
            .clickable { onOpenPresets() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("presets_button")
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Tune, contentDescription = "Presets", tint = NeonCyan, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Presets", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // Lower Row: Navigation Workspace Switcher
    val scrollState = rememberScrollState()
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(DarkStudioBg)
        .horizontalScroll(scrollState)
        .padding(horizontal = 6.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      DawTab.entries.forEach { tab ->
        val isSelected = currentTab == tab
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else Color.Transparent)
            .border(
              width = if (isSelected) 1.dp else 0.dp,
              color = if (isSelected) NeonCyan else Color.Transparent,
              shape = RoundedCornerShape(6.dp)
            )
            .clickable { viewModel.selectTab(tab) }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("tab_${tab.name.lowercase()}")
        ) {
          Text(
            text = tab.title,
            color = if (isSelected) NeonCyan else TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
          )
        }
      }
    }
  }
}
