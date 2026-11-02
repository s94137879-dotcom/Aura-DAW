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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.GraphicEq
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
import androidx.compose.ui.geometry.Offset
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
import com.example.audio.DawTrack
import com.example.ui.components.GainReductionMeter
import com.example.ui.components.RotaryKnob
import com.example.ui.components.VuPeakMeter
import com.example.ui.theme.DarkStudioBg
import com.example.ui.theme.DarkStudioBorder
import com.example.ui.theme.DarkStudioCard
import com.example.ui.theme.DarkStudioHeader
import com.example.ui.theme.MeterRed
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NewtonePurple
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.DawViewModel

@Composable
fun MixerRackView(
  viewModel: DawViewModel,
  modifier: Modifier = Modifier
) {
  val tracks by viewModel.tracks.collectAsState()
  val selectedTrackId by viewModel.selectedTrackId.collectAsState()
  val gainReduction by viewModel.audioEngine.currentGainReduction.collectAsState()

  val selectedTrack = tracks.find { it.id == selectedTrackId } ?: tracks.firstOrNull()

  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkStudioBg)
      .verticalScroll(scrollState)
      .padding(10.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // Top Section: Horizontal Mixer Channel Strips
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .background(DarkStudioCard)
        .border(1.dp, DarkStudioBorder, RoundedCornerShape(8.dp))
        .padding(8.dp)
    ) {
      Text("MIXER CHANNELS & BUSSES", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
      Spacer(modifier = Modifier.height(8.dp))

      val channelScrollState = rememberScrollState()
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(channelScrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        tracks.forEach { track ->
          val isSelected = track.id == selectedTrackId
          MixerChannelStrip(
            track = track,
            isSelected = isSelected,
            onSelect = { viewModel.selectTrack(track.id) },
            onVolume = { viewModel.updateTrackVolume(track.id, it) },
            onPan = { viewModel.updateTrackPan(track.id, it) },
            onMute = { viewModel.toggleTrackMute(track.id) },
            onSolo = { viewModel.toggleTrackSolo(track.id) }
          )
        }
      }
    }

    // Bottom Section: VST Plugin Rack for Selected Track
    if (selectedTrack != null) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(DarkStudioCard)
          .border(1.dp, DarkStudioBorder, RoundedCornerShape(8.dp))
          .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Plugin Rack Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
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
              text = "VST RACK: ${selectedTrack.name.uppercase()}",
              color = Color.White,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Text(
            text = "3 Plugins Active",
            color = NeonCyan,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
          )
        }

        // Plugin Slot 1: Sidechain Compressor (Pro Dynamics)
        val sc = selectedTrack.sidechainCompressor
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkStudioHeader)
            .border(1.dp, if (sc.enabled) NeonAmber.copy(alpha = 0.5f) else DarkStudioBorder, RoundedCornerShape(8.dp))
            .padding(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Compress, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("SIDECHAIN COMPRESSOR (VCA Pumping)", color = NeonAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            // Sidechain Source Selector
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text("TRIGGER: ", fontSize = 9.sp, color = TextSecondary)
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(DarkStudioCard)
                  .clickable {
                    // Toggle source between Kick and Vocal
                    val nextSource = if (sc.sourceTrackId == "trk_drums") "trk_vocal_lead" else "trk_drums"
                    viewModel.updateSidechainParams(selectedTrack.id) { it.copy(sourceTrackId = nextSource) }
                  }
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = if (sc.sourceTrackId == "trk_drums") "Kick Drum (Trk 1)" else "Vocal Lead (Trk 4)",
                  color = NeonCyan,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Live Gain Reduction Meter
          GainReductionMeter(
            gainReductionDb = if (sc.enabled) gainReduction else 0.0f,
            modifier = Modifier.fillMaxWidth().height(20.dp)
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Sidechain Knobs: Threshold, Ratio, Attack, Release, Knee, Makeup
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            // Threshold (-60 to 0 dB)
            RotaryKnob(
              value = ((sc.thresholdDb + 60f) / 60f).coerceIn(0f, 1f),
              onValueChange = { v ->
                val db = (v * 60f) - 60f
                viewModel.updateSidechainParams(selectedTrack.id) { it.copy(thresholdDb = db) }
              },
              label = "THRESHOLD",
              displayValue = "${sc.thresholdDb.toInt()} dB",
              tintColor = NeonAmber,
              testTag = "sc_threshold"
            )

            // Ratio (1:1 to 20:1)
            RotaryKnob(
              value = ((sc.ratio - 1f) / 19f).coerceIn(0f, 1f),
              onValueChange = { v ->
                val r = 1f + v * 19f
                viewModel.updateSidechainParams(selectedTrack.id) { it.copy(ratio = r) }
              },
              label = "RATIO",
              displayValue = "${"%.1f".format(sc.ratio)}:1",
              tintColor = NeonAmber,
              testTag = "sc_ratio"
            )

            // Attack (0.1ms to 100ms)
            RotaryKnob(
              value = (sc.attackMs / 100f).coerceIn(0f, 1f),
              onValueChange = { v ->
                viewModel.updateSidechainParams(selectedTrack.id) { it.copy(attackMs = v * 100f) }
              },
              label = "ATTACK",
              displayValue = "${sc.attackMs.toInt()}ms",
              tintColor = TextPrimary
            )

            // Release (10ms to 800ms)
            RotaryKnob(
              value = ((sc.releaseMs - 10f) / 790f).coerceIn(0f, 1f),
              onValueChange = { v ->
                viewModel.updateSidechainParams(selectedTrack.id) { it.copy(releaseMs = 10f + v * 790f) }
              },
              label = "RELEASE",
              displayValue = "${sc.releaseMs.toInt()}ms",
              tintColor = TextPrimary
            )

            // Makeup Gain
            RotaryKnob(
              value = (sc.makeupGainDb / 18f).coerceIn(0f, 1f),
              onValueChange = { v ->
                viewModel.updateSidechainParams(selectedTrack.id) { it.copy(makeupGainDb = v * 18f) }
              },
              label = "MAKEUP",
              displayValue = "+${"%.1f".format(sc.makeupGainDb)}dB",
              tintColor = NeonCyan
            )
          }
        }

        // Plugin Slot 2: 7-Band Parametric EQ
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkStudioHeader)
            .border(1.dp, DarkStudioBorder, RoundedCornerShape(8.dp))
            .padding(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.GraphicEq, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("PARAMETRIC EQ (7-Band Linear Phase)", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Text("20Hz - 20kHz", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
          }

          Spacer(modifier = Modifier.height(8.dp))

          // EQ Curve Canvas
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(90.dp)
              .clip(RoundedCornerShape(6.dp))
              .background(DarkStudioBg)
              .padding(4.dp)
          ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
              val w = size.width
              val h = size.height
              val midY = h / 2f

              // Zero dB line
              drawLine(
                color = DarkStudioBorder,
                start = Offset(0f, midY),
                end = Offset(w, midY),
                strokeWidth = 1f
              )

              // Draw smooth spline curve for 7 bands
              val eqPath = Path()
              eqPath.moveTo(0f, midY)

              val bands = selectedTrack.eqBands
              for (i in bands.indices) {
                val b = bands[i]
                val x = (i.toFloat() / (bands.size - 1)) * w
                val y = midY - (b.gainDb * 5f)
                if (i == 0) eqPath.moveTo(x, y) else eqPath.lineTo(x, y)
                // Draw frequency node dot
                drawCircle(
                  color = NeonCyan,
                  radius = 4f,
                  center = Offset(x, y)
                )
              }

              drawPath(
                path = eqPath,
                color = NeonCyan,
                style = Stroke(width = 2.5f, cap = StrokeCap.Round)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun MixerChannelStrip(
  track: DawTrack,
  isSelected: Boolean,
  onSelect: () -> Unit,
  onVolume: (Float) -> Unit,
  onPan: (Float) -> Unit,
  onMute: () -> Unit,
  onSolo: () -> Unit
) {
  Column(
    modifier = Modifier
      .width(80.dp)
      .clip(RoundedCornerShape(6.dp))
      .background(if (isSelected) DarkStudioHeader else DarkStudioBg)
      .border(
        width = if (isSelected) 1.5.dp else 0.5.dp,
        color = if (isSelected) NeonCyan else DarkStudioBorder,
        shape = RoundedCornerShape(6.dp)
      )
      .clickable { onSelect() }
      .padding(6.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Track color indicator & Name
    Box(
      modifier = Modifier
        .size(8.dp)
        .clip(CircleShape)
        .background(Color(track.color))
    )
    Spacer(modifier = Modifier.height(3.dp))
    Text(
      text = track.name,
      color = Color.White,
      fontSize = 9.sp,
      fontWeight = FontWeight.Bold,
      maxLines = 1
    )

    Spacer(modifier = Modifier.height(4.dp))

    // Pan Mini Knob
    RotaryKnob(
      value = ((track.pan + 1f) / 2f).coerceIn(0f, 1f),
      onValueChange = { v -> onPan((v * 2f) - 1f) },
      label = "PAN",
      displayValue = if (kotlin.math.abs(track.pan) < 0.05f) "C" else if (track.pan < 0) "L${(-track.pan * 100).toInt()}" else "R${(track.pan * 100).toInt()}",
      knobSize = 36.dp,
      tintColor = NeonCyan
    )

    Spacer(modifier = Modifier.height(4.dp))

    // Vertical Fader
    Row(
      modifier = Modifier
        .height(110.dp)
        .fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Slider(
        value = track.volume,
        onValueChange = onVolume,
        valueRange = 0f..1.2f,
        colors = SliderDefaults.colors(
          thumbColor = Color(track.color),
          activeTrackColor = Color(track.color),
          inactiveTrackColor = DarkStudioBorder
        ),
        modifier = Modifier.width(100.dp)
      )
    }

    Text(
      text = "${(track.volume * 100).toInt()}%",
      fontSize = 9.sp,
      color = TextSecondary,
      fontFamily = FontFamily.Monospace
    )

    Spacer(modifier = Modifier.height(6.dp))

    // M / S buttons
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
      Box(
        modifier = Modifier
          .size(22.dp)
          .clip(RoundedCornerShape(3.dp))
          .background(if (track.isMuted) MeterRed else DarkStudioCard)
          .clickable { onMute() },
        contentAlignment = Alignment.Center
      ) {
        Text("M", color = if (track.isMuted) Color.White else TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
      }
      Box(
        modifier = Modifier
          .size(22.dp)
          .clip(RoundedCornerShape(3.dp))
          .background(if (track.isSoloed) NeonAmber else DarkStudioCard)
          .clickable { onSolo() },
        contentAlignment = Alignment.Center
      ) {
        Text("S", color = if (track.isSoloed) Color.Black else TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
      }
    }
  }
}
