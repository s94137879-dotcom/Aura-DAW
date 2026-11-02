package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkStudioBg
import com.example.ui.theme.DarkStudioBorder
import com.example.ui.theme.DarkStudioCard
import com.example.ui.theme.MeterGreen
import com.example.ui.theme.MeterRed
import com.example.ui.theme.MeterYellow
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Professional tactile rotary control knob with continuous drag gesture.
 */
@Composable
fun RotaryKnob(
  value: Float, // Normalized 0.0 to 1.0
  onValueChange: (Float) -> Unit,
  label: String,
  displayValue: String,
  modifier: Modifier = Modifier,
  knobSize: Dp = 56.dp,
  tintColor: Color = NeonCyan,
  testTag: String = "rotary_knob"
) {
  var accumulatedDelta by remember { mutableFloatStateOf(0f) }

  Column(
    modifier = modifier.testTag(testTag),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier = Modifier
        .size(knobSize)
        .clip(CircleShape)
        .background(DarkStudioCard)
        .pointerInput(Unit) {
          detectDragGestures(
            onDragStart = { accumulatedDelta = 0f },
            onDrag = { change, dragAmount ->
              change.consume()
              // Drag up to increase, down to decrease
              val sensitivity = 0.005f
              val delta = -dragAmount.y * sensitivity
              val newValue = (value + delta).coerceIn(0f, 1f)
              onValueChange(newValue)
            }
          )
        },
      contentAlignment = Alignment.Center
    ) {
      Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
        val strokeWidth = 3.5.dp.toPx()
        val radius = size.minDimension / 2f - strokeWidth

        // Arc starts at 135 deg to 405 deg (270 degrees travel)
        val startAngle = 135f
        val sweepAngle = 270f

        // Background track arc
        drawArc(
          color = DarkStudioBorder,
          startAngle = startAngle,
          sweepAngle = sweepAngle,
          useCenter = false,
          style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Active value arc
        drawArc(
          color = tintColor,
          startAngle = startAngle,
          sweepAngle = sweepAngle * value.coerceIn(0f, 1f),
          useCenter = false,
          style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Center pointer tick
        val angleRad = (startAngle + sweepAngle * value) * (PI / 180.0)
        val center = Offset(size.width / 2f, size.height / 2f)
        val innerR = radius * 0.35f
        val outerR = radius * 0.85f

        val startTick = Offset(
          center.x + innerR * cos(angleRad).toFloat(),
          center.y + innerR * sin(angleRad).toFloat()
        )
        val endTick = Offset(
          center.x + outerR * cos(angleRad).toFloat(),
          center.y + outerR * sin(angleRad).toFloat()
        )

        drawLine(
          color = Color.White,
          start = startTick,
          end = endTick,
          strokeWidth = 2.5.dp.toPx(),
          cap = StrokeCap.Round
        )
      }
    }

    Spacer(modifier = Modifier.height(3.dp))
    Text(
      text = label,
      color = TextSecondary,
      fontSize = 10.sp,
      fontWeight = FontWeight.Medium,
      maxLines = 1
    )
    Text(
      text = displayValue,
      color = tintColor,
      fontSize = 9.sp,
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.Bold
    )
  }
}

/**
 * Sidechain Gain Reduction (GR) dynamic meter showing compressor pumping.
 */
@Composable
fun GainReductionMeter(
  gainReductionDb: Float, // Negative value, e.g. -12dB
  modifier: Modifier = Modifier
) {
  val animatedGr by animateFloatAsState(targetValue = gainReductionDb, label = "gr")
  val normalized = (kotlin.math.abs(animatedGr) / 24.0f).coerceIn(0f, 1f)

  Column(modifier = modifier) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text("GR", fontSize = 10.sp, color = NeonAmber, fontWeight = FontWeight.Bold)
      Text(
        text = "${"%.1f".format(animatedGr)} dB",
        fontSize = 10.sp,
        color = NeonAmber,
        fontFamily = FontFamily.Monospace
      )
    }

    Spacer(modifier = Modifier.height(2.dp))

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(8.dp)
        .clip(RoundedCornerShape(4.dp))
        .background(DarkStudioBg)
    ) {
      Box(
        modifier = Modifier
          .fillMaxHeight()
          .fillMaxWidth(fraction = normalized)
          .background(
            Brush.horizontalGradient(
              listOf(NeonAmber, MeterRed)
            )
          )
      )
    }
  }
}

/**
 * Multi-segment VU level peak meter.
 */
@Composable
fun VuPeakMeter(
  peakLevel: Float, // 0.0 to 1.2
  modifier: Modifier = Modifier,
  segments: Int = 12
) {
  val level = peakLevel.coerceIn(0f, 1.2f)

  Row(
    modifier = modifier
      .background(DarkStudioBg, RoundedCornerShape(2.dp))
      .padding(2.dp),
    horizontalArrangement = Arrangement.spacedBy(1.dp)
  ) {
    for (i in 0 until segments) {
      val threshold = i.toFloat() / segments
      val isLit = level >= threshold
      val segColor = when {
        i >= segments - 2 -> MeterRed
        i >= segments - 5 -> MeterYellow
        else -> MeterGreen
      }

      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
          .background(if (isLit) segColor else segColor.copy(alpha = 0.15f))
      )
    }
  }
}

/**
 * Interactive Waveform renderer for audio clips and microphone recordings.
 */
@Composable
fun WaveformCanvas(
  amplitudes: List<Float>,
  tint: Color,
  modifier: Modifier = Modifier
) {
  Canvas(modifier = modifier) {
    if (amplitudes.isEmpty()) return@Canvas

    val width = size.width
    val height = size.height
    val midY = height / 2f
    val step = width / amplitudes.size

    val path = Path()
    path.moveTo(0f, midY)

    for (i in amplitudes.indices) {
      val x = i * step + step / 2f
      val amp = amplitudes[i].coerceIn(0.05f, 1f) * (height / 2f) * 0.9f
      drawLine(
        color = tint,
        start = Offset(x, midY - amp),
        end = Offset(x, midY + amp),
        strokeWidth = (step * 0.7f).coerceAtLeast(1.5f),
        cap = StrokeCap.Round
      )
    }
  }
}
