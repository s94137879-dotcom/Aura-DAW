package com.example.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.audio.DawPreset
import com.example.ui.theme.DarkStudioBg
import com.example.ui.theme.DarkStudioBorder
import com.example.ui.theme.DarkStudioCard
import com.example.ui.theme.DarkStudioHeader
import com.example.ui.theme.HarmorOrange
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.DawViewModel

@Composable
fun PresetManagerDialog(
  viewModel: DawViewModel,
  onDismiss: () -> Unit
) {
  val presets by viewModel.allPresets.collectAsState()
  val filterCategory by viewModel.presetFilterCategory.collectAsState()

  var isCreatingCustom by remember { mutableStateOf(false) }
  var customName by remember { mutableStateOf("") }
  var customCategory by remember { mutableStateOf("Mixing Chain") }
  var customDesc by remember { mutableStateOf("") }

  val categories = listOf("All", "Harmor Synth", "Mixing Chain", "Mastering")
  val filteredPresets = if (filterCategory == "All") {
    presets
  } else {
    presets.filter { it.category == filterCategory }
  }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(DarkStudioHeader)
        .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
      colors = CardDefaults.cardColors(containerColor = DarkStudioHeader)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Tune, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "PRESET MANAGER",
              color = NeonCyan,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold
            )
          }

          IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(18.dp))
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Tabs
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          categories.forEach { cat ->
            val isSel = filterCategory == cat
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(4.dp))
                .background(if (isSel) NeonCyan.copy(alpha = 0.25f) else DarkStudioCard)
                .border(1.dp, if (isSel) NeonCyan else DarkStudioBorder, RoundedCornerShape(4.dp))
                .clickable { viewModel.filterPresetsByCategory(cat) }
                .padding(vertical = 6.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = cat,
                color = if (isSel) NeonCyan else TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Presets List
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(filteredPresets, key = { it.id }) { preset ->
            val catColor = when (preset.category) {
              "Harmor Synth" -> HarmorOrange
              "Mastering" -> MatrixGreen
              else -> NeonAmber
            }

            Column(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(DarkStudioCard)
                .border(0.5.dp, DarkStudioBorder, RoundedCornerShape(6.dp))
                .clickable {
                  viewModel.loadPreset(preset)
                  onDismiss()
                }
                .padding(10.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(preset.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(catColor.copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(preset.category, color = catColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
              }

              Spacer(modifier = Modifier.height(4.dp))
              Text(preset.description, color = TextSecondary, fontSize = 10.sp)
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Save Current Preset Section
        if (isCreatingCustom) {
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(
              value = customName,
              onValueChange = { customName = it },
              placeholder = { Text("Preset Name (e.g. Acid Drop Lead)", fontSize = 11.sp) },
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkStudioCard,
                unfocusedContainerColor = DarkStudioCard,
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = DarkStudioBorder,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
              ),
              modifier = Modifier.fillMaxWidth().height(46.dp)
            )

            OutlinedTextField(
              value = customDesc,
              onValueChange = { customDesc = it },
              placeholder = { Text("Short description...", fontSize = 11.sp) },
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkStudioCard,
                unfocusedContainerColor = DarkStudioCard,
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = DarkStudioBorder,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
              ),
              modifier = Modifier.fillMaxWidth().height(46.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              FilledTonalButton(
                onClick = {
                  if (customName.isNotBlank()) {
                    viewModel.saveCustomPreset(customName, customCategory, customDesc)
                    isCreatingCustom = false
                    customName = ""
                    customDesc = ""
                  }
                },
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = NeonCyan, contentColor = Color.Black),
                modifier = Modifier.weight(1f).height(34.dp)
              ) {
                Text("Confirm Save", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
              FilledTonalButton(
                onClick = { isCreatingCustom = false },
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = DarkStudioCard, contentColor = TextSecondary),
                modifier = Modifier.weight(1f).height(34.dp)
              ) {
                Text("Cancel", fontSize = 11.sp)
              }
            }
          }
        } else {
          FilledTonalButton(
            onClick = { isCreatingCustom = true },
            colors = ButtonDefaults.filledTonalButtonColors(
              containerColor = NeonCyan.copy(alpha = 0.2f),
              contentColor = NeonCyan
            ),
            modifier = Modifier.fillMaxWidth().height(36.dp)
          ) {
            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Save Current DAW Setup as New Preset", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
