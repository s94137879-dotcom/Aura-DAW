package com.example.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkStudioBg
import com.example.ui.theme.DarkStudioBorder
import com.example.ui.theme.DarkStudioCard
import com.example.ui.theme.DarkStudioHeader
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NewtonePurple
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.DawViewModel

@Composable
fun CloudCollabView(
  viewModel: DawViewModel,
  modifier: Modifier = Modifier
) {
  val projectName by viewModel.projectName.collectAsState()
  val syncStatus by viewModel.cloudSyncStatus.collectAsState()
  val collaborators by viewModel.collaborators.collectAsState()
  val comments by viewModel.projectComments.collectAsState()
  val currentBar by viewModel.audioEngine.currentBar.collectAsState()

  var newCommentText by remember { mutableStateOf("") }
  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkStudioBg)
      .verticalScroll(scrollState)
      .padding(12.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // 1. Cloud Project Banner & Live Sync Status
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .background(
          Brush.horizontalGradient(
            listOf(NeonCyan.copy(alpha = 0.2f), DarkStudioCard)
          )
        )
        .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.CloudDone, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(26.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(projectName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
          Text(syncStatus, color = MatrixGreen, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
      }

      // Actions: Export Stems & Share Invite Link
      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        FilledTonalButton(
          onClick = { viewModel.exportStems() },
          colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = DarkStudioHeader,
            contentColor = NeonAmber
          ),
          modifier = Modifier.height(32.dp).testTag("export_stems_button")
        ) {
          Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Stems ZIP", fontSize = 10.sp)
        }

        FilledTonalButton(
          onClick = {
            viewModel.showToast("Collab Invite link copied: auradaw.live/collab/proj_cyberpunk_84")
          },
          colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = NeonCyan.copy(alpha = 0.25f),
            contentColor = NeonCyan
          ),
          modifier = Modifier.height(32.dp).testTag("share_invite_button")
        ) {
          Icon(Icons.Default.IosShare, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Invite", fontSize = 10.sp)
        }
      }
    }

    // 2. Active Online Collaborators
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .background(DarkStudioCard)
        .border(1.dp, DarkStudioBorder, RoundedCornerShape(8.dp))
        .padding(10.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Group, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("ACTIVE PRODUCERS & COLLABORATORS (${collaborators.size} Online)", color = NeonAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
      }

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        collaborators.forEach { user ->
          Column(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(6.dp))
              .background(DarkStudioHeader)
              .border(0.5.dp, DarkStudioBorder)
              .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(user.avatarColor)),
              contentAlignment = Alignment.Center
            ) {
              Text(user.avatarInitials, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(user.name.split(" ").first(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(user.role, color = TextSecondary, fontSize = 8.sp, maxLines = 1)
          }
        }
      }
    }

    // 3. Timeline Feedback & Chat Notes
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
          Icon(Icons.Default.Comment, contentDescription = null, tint = NewtonePurple, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("TIMELINE FEEDBACK PINS", color = NewtonePurple, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Text("Current: Bar ${"%.1f".format(currentBar)}", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
      }

      Spacer(modifier = Modifier.height(8.dp))

      // List of comments
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        comments.forEach { comment ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(6.dp))
              .background(DarkStudioHeader)
              .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(NeonCyan.copy(alpha = 0.2f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text("Bar ${"%.1f".format(comment.barPosition)}", color = NeonCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "${comment.authorName} • ${comment.timestamp}",
                color = TextSecondary,
                fontSize = 9.sp
              )
              Text(
                text = comment.text,
                color = Color.White,
                fontSize = 11.sp
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Input field to add comment at current bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        OutlinedTextField(
          value = newCommentText,
          onValueChange = { newCommentText = it },
          placeholder = { Text("Leave note pinned at Bar ${"%.1f".format(currentBar)}...", fontSize = 11.sp) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = DarkStudioHeader,
            unfocusedContainerColor = DarkStudioHeader,
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = DarkStudioBorder,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
          ),
          modifier = Modifier.weight(1f).height(46.dp)
        )

        FilledTonalButton(
          onClick = {
            if (newCommentText.isNotBlank()) {
              viewModel.addProjectComment(newCommentText)
              newCommentText = ""
            }
          },
          colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = NeonCyan,
            contentColor = Color.Black
          ),
          modifier = Modifier.height(46.dp)
        ) {
          Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(16.dp))
        }
      }
    }

    // 4. Version History & Snapshot Rollback
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .background(DarkStudioCard)
        .border(1.dp, DarkStudioBorder, RoundedCornerShape(8.dp))
        .padding(10.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.History, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("CLOUD VERSION HISTORY", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
      }

      Spacer(modifier = Modifier.height(8.dp))

      val versions = listOf(
        Triple("v4.2 (Latest)", "Elena Vance tuned 'Stay with me' vocal slices in Newtone", "12m ago"),
        Triple("v4.1", "Alex Miller engaged Sidechain Ducking between Kick and Reese Sub", "35m ago"),
        Triple("v4.0", "Marcus Kane designed Cyberpunk Lead 2077 in Harmor", "1h ago"),
        Triple("v3.9", "Mastering Suite calibrated to -14.0 LUFS EBU R128", "2h ago")
      )

      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        versions.forEach { (ver, note, time) ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(4.dp))
              .background(DarkStudioHeader)
              .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(ver, color = NeonAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
              Text(note, color = TextPrimary, fontSize = 10.sp)
            }
            Text(time, color = TextSecondary, fontSize = 9.sp)
          }
        }
      }
    }
  }
}
