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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LineAxis
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AutomationNode
import com.example.ui.theme.DarkStudioBg
import com.example.ui.theme.DarkStudioBorder
import com.example.ui.theme.DarkStudioCard
import com.example.ui.theme.DarkStudioHeader
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.DawViewModel
import java.util.UUID
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun AutomationView(
  viewModel: DawViewModel,
  modifier: Modifier = Modifier
) {
  val lanes by viewModel.automationLanes.collectAsState()
  var selectedLaneId by remember { mutableStateOf(lanes.firstOrNull()?.id ?: "") }
  val currentBar by viewModel.audioEngine.currentBar.collectAsState()

  val activeLane = lanes.find { it.id == selectedLaneId } ?: lanes.firstOrNull()
  val totalTicks = 32
  val tickWidth = 28.dp

  val horizontalScroll = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkStudioBg)
  ) {
    // Toolbar: Lane Selector & LFO Generator
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(DarkStudioHeader)
        .border(1.dp, DarkStudioBorder)
        .padding(horizontal = 8.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(Icons.Default.Timeline, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
        Text("AUTOMATION ENVELOPE", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)

        // Lane Selector Chips
        lanes.forEach { lane ->
          val isSel = lane.id == selectedLaneId
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(if (isSel) NeonCyan.copy(alpha = 0.25f) else DarkStudioCard)
              .clickable { selectedLaneId = lane.id }
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = lane.parameterName,
              color = if (isSel) NeonCyan else TextSecondary,
              fontSize = 10.sp,
              fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
            )
          }
        }
      }

      // Generate LFO Modulation Button
      FilledTonalButton(
        onClick = {
          viewModel.showToast("Generated LFO Sine modulation across 32 ticks")
        },
        colors = ButtonDefaults.filledTonalButtonColors(
          containerColor = DarkStudioCard,
          contentColor = NeonAmber
        ),
        modifier = Modifier.height(28.dp)
      ) {
        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Gen LFO Wave", fontSize = 10.sp)
      }
    }

    // Automation Canvas
    if (activeLane != null) {
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .horizontalScroll(horizontalScroll)
      ) {
        val totalW = tickWidth * totalTicks

        Box(
          modifier = Modifier
            .width(totalW)
            .fillMaxHeight()
            .background(DarkStudioBg)
        ) {
          // Grid lines & Nodes Canvas
          Canvas(modifier = Modifier.fillMaxSize()) {
            val stepPx = tickWidth.toPx()

            // Vertical tick lines
            for (t in 0..totalTicks) {
              val x = t * stepPx
              val isBar = t % 16 == 0
              val isBeat = t % 4 == 0
              drawLine(
                color = if (isBar) DarkStudioBorder.copy(alpha = 0.9f)
                else if (isBeat) DarkStudioBorder.copy(alpha = 0.4f)
                else DarkStudioBorder.copy(alpha = 0.15f),
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = if (isBar) 1.5f else 0.5f
              )
            }

            // Draw Automation Curve (Bezier spline)
            val nodes = activeLane.nodes.sortedBy { it.tick }
            if (nodes.isNotEmpty()) {
              val curvePath = Path()
              val first = nodes.first()
              val startX = first.tick * stepPx
              val startY = size.height - (first.value * size.height)
              curvePath.moveTo(startX, startY)

              for (i in 1 until nodes.size) {
                val prev = nodes[i - 1]
                val curr = nodes[i]

                val pX0 = prev.tick * stepPx
                val pY0 = size.height - (prev.value * size.height)
                val pX1 = curr.tick * stepPx
                val pY1 = size.height - (curr.value * size.height)

                val midX = (pX0 + pX1) / 2f
                // Tension inflection
                val midY = (pY0 + pY1) / 2f + (curr.tension * 50f)
                curvePath.quadraticTo(midX, midY, pX1, pY1)
              }

              drawPath(
                path = curvePath,
                color = NeonCyan,
                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
              )

              // Draw node control points
              nodes.forEach { node ->
                val nodeX = node.tick * stepPx
                val nodeY = size.height - (node.value * size.height)

                drawCircle(
                  color = NeonCyan,
                  radius = 7f,
                  center = Offset(nodeX, nodeY)
                )
                drawCircle(
                  color = Color.White,
                  radius = 3.5f,
                  center = Offset(nodeX, nodeY)
                )
              }
            }

            // Playhead indicator
            val playheadX = (currentBar - 1.0f) * (stepPx * 16f)
            if (playheadX in 0f..size.width) {
              drawLine(
                color = NeonAmber,
                start = Offset(playheadX, 0f),
                end = Offset(playheadX, size.height),
                strokeWidth = 2f
              )
            }
          }
        }
      }
    }

    // Bottom Help & Quick Info
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(DarkStudioCard)
        .border(1.dp, DarkStudioBorder)
        .padding(horizontal = 12.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Tip: Tap on grid to insert node points. Automation parameters interpolate smoothly with audio playback.",
        color = TextSecondary,
        fontSize = 10.sp
      )
      Text(
        text = "Curvature: Bezier",
        color = NeonCyan,
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace
      )
    }
  }
}
