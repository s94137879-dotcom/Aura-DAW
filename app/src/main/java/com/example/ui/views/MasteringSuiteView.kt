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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SurroundSound
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.PresetRepository
import com.example.ui.components.RotaryKnob
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
import com.example.viewmodel.DawViewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MasteringSuiteView(
  viewModel: DawViewModel,
  modifier: Modifier = Modifier
) {
  val data by viewModel.masteringData.collectAsState()
  val liveLufs by viewModel.audioEngine.liveLufs.collectAsState()
  val masterPeak by viewModel.audioEngine.masterPeak.collectAsState()
  val correlation by viewModel.audioEngine.livePhaseCorrelation.collectAsState()

  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkStudioBg)
      .verticalScroll(scrollState)
      .padding(12.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // Top Mastering Suite Header & Smart Master Assist
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .background(
          Brush.horizontalGradient(
            listOf(MatrixGreen.copy(alpha = 0.2f), DarkStudioCard)
          )
        )
        .border(1.dp, MatrixGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Speed, contentDescription = null, tint = MatrixGreen, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text("MASTERING SUITE", color = MatrixGreen, fontWeight = FontWeight.Black, fontSize = 14.sp)
          Text("EBU R128 Loudness & True Peak Engine", color = TextSecondary, fontSize = 10.sp)
        }
      }

      // Smart Master Assist Button
      FilledTonalButton(
        onClick = { viewModel.runSmartMasterAssist() },
        colors = ButtonDefaults.filledTonalButtonColors(
          containerColor = MatrixGreen,
          contentColor = Color.Black
        ),
        modifier = Modifier.height(34.dp).testTag("smart_master_assist_button")
      ) {
        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Smart Master Assist", fontSize = 11.sp, fontWeight = FontWeight.Bold)
      }
    }

    // Mastering Presets Selector
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(6.dp))
        .background(DarkStudioCard)
        .padding(6.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      PresetRepository.masteringPresets.forEach { p ->
        val isCurrent = p.name == data.presetName
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(4.dp))
            .background(if (isCurrent) NeonCyan.copy(alpha = 0.25f) else DarkStudioBg)
            .border(1.dp, if (isCurrent) NeonCyan else DarkStudioBorder, RoundedCornerShape(4.dp))
            .clickable { viewModel.loadPreset(p) }
            .padding(vertical = 6.dp, horizontal = 4.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = p.name.split("(").first().trim(),
            color = if (isCurrent) NeonCyan else TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
          )
        }
      }
    }

    // 1. EBU R128 Loudness Metering Panel
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Integrated LUFS Display Box
      Column(
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(8.dp))
          .background(DarkStudioCard)
          .border(1.dp, DarkStudioBorder, RoundedCornerShape(8.dp))
          .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text("INTEGRATED LUFS", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "${"%.1f".format(liveLufs)}",
          color = MatrixGreen,
          fontSize = 24.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Black
        )
        Text("Target: ${data.targetLufs} LUFS", color = TextSecondary, fontSize = 9.sp)
      }

      // True Peak Display Box
      Column(
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(8.dp))
          .background(DarkStudioCard)
          .border(1.dp, DarkStudioBorder, RoundedCornerShape(8.dp))
          .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text("TRUE PEAK (dBTP)", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        val truePeakVal = (20f * kotlin.math.log10(masterPeak.coerceAtLeast(0.001f))).coerceIn(-30f, 1f)
        Text(
          text = "${"%.2f".format(truePeakVal)} dB",
          color = if (truePeakVal > -0.3f) MeterRed else NeonCyan,
          fontSize = 22.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Black
        )
        Text("Ceiling: ${data.ceilingDb} dBTP", color = TextSecondary, fontSize = 9.sp)
      }

      // Stereo Phase Correlation Box
      Column(
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(8.dp))
          .background(DarkStudioCard)
          .border(1.dp, DarkStudioBorder, RoundedCornerShape(8.dp))
          .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text("PHASE CORRELATION", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "+${"%.2f".format(correlation)}",
          color = NeonAmber,
          fontSize = 22.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Black
        )
        Text("Mono Compatible", color = MatrixGreen, fontSize = 9.sp)
      }
    }

    // 2. Stereo Vectorscope & Goniometer Visualizer
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
          Icon(Icons.Default.SurroundSound, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("STEREO IMAGER & GONIOMETER", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Text("Width: ${"%.2f".format(data.stereoWidth)}x", color = NeonCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Interactive Circular Vectorscope Canvas
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(110.dp)
          .clip(RoundedCornerShape(6.dp))
          .background(DarkStudioBg),
        contentAlignment = Alignment.Center
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val center = Offset(size.width / 2f, size.height / 2f)
          val radius = size.height * 0.42f

          // Center crosshair
          drawLine(color = DarkStudioBorder, start = Offset(center.x - radius, center.y), end = Offset(center.x + radius, center.y), strokeWidth = 1f)
          drawLine(color = DarkStudioBorder, start = Offset(center.x, center.y - radius), end = Offset(center.x, center.y + radius), strokeWidth = 1f)
          drawCircle(color = DarkStudioBorder, radius = radius, center = center, style = Stroke(1f))

          // Stereo cloud particles (Lissajous)
          val widthFactor = data.stereoWidth
          for (i in 0..36) {
            val angle = (i * 10f) * (kotlin.math.PI / 180f)
            val r = (radius * (0.3f + (i % 7) * 0.1f))
            val x = center.x + r * cos(angle).toFloat() * widthFactor
            val y = center.y + r * sin(angle).toFloat()
            drawCircle(
              color = NeonCyan.copy(alpha = 0.6f),
              radius = 2.5f,
              center = Offset(x, y)
            )
          }
        }
      }
    }

    // 3. Mastering Processing Knobs:
    // Stereo Width, Bass Warmth, Air Sparkle, Limiter Ceiling, Threshold
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .background(DarkStudioCard)
        .border(1.dp, DarkStudioBorder, RoundedCornerShape(8.dp))
        .padding(10.dp),
      horizontalArrangement = Arrangement.SpaceAround
    ) {
      // Stereo Width (0.0 to 2.0)
      RotaryKnob(
        value = (data.stereoWidth / 2.0f).coerceIn(0f, 1f),
        onValueChange = { v ->
          viewModel.updateMasteringData { it.copy(stereoWidth = v * 2.0f) }
        },
        label = "STEREO WIDTH",
        displayValue = "${"%.2f".format(data.stereoWidth)}x",
        tintColor = NeonCyan
      )

      // Bass Warmth (+0 to +6dB)
      RotaryKnob(
        value = (data.bassWarmthDb / 6.0f).coerceIn(0f, 1f),
        onValueChange = { v ->
          viewModel.updateMasteringData { it.copy(bassWarmthDb = v * 6.0f) }
        },
        label = "BASS WARMTH",
        displayValue = "+${"%.1f".format(data.bassWarmthDb)}dB",
        tintColor = NeonAmber
      )

      // Air Sparkle (+0 to +6dB)
      RotaryKnob(
        value = (data.airSparkleDb / 6.0f).coerceIn(0f, 1f),
        onValueChange = { v ->
          viewModel.updateMasteringData { it.copy(airSparkleDb = v * 6.0f) }
        },
        label = "AIR SPARKLE",
        displayValue = "+${"%.1f".format(data.airSparkleDb)}dB",
        tintColor = MatrixGreen
      )

      // Limiter Ceiling (-1.0 to 0.0 dB)
      RotaryKnob(
        value = ((data.ceilingDb + 1.0f) / 1.0f).coerceIn(0f, 1f),
        onValueChange = { v ->
          val c = (v * 1.0f) - 1.0f
          viewModel.updateMasteringData { it.copy(ceilingDb = c) }
        },
        label = "CEILING",
        displayValue = "${"%.2f".format(data.ceilingDb)} dBTP",
        tintColor = MeterRed
      )
    }
  }
}
