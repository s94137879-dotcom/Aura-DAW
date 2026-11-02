package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.DarkStudioBg
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.views.AutomationView
import com.example.ui.views.CloudCollabView
import com.example.ui.views.HarmorPluginView
import com.example.ui.views.MasteringSuiteView
import com.example.ui.views.MixerRackView
import com.example.ui.views.NewtoneVocalView
import com.example.ui.views.PianoRollView
import com.example.ui.views.PresetManagerDialog
import com.example.ui.views.TimelineView
import com.example.ui.views.TopTransportBar
import com.example.viewmodel.DawTab
import com.example.viewmodel.DawViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme(darkTheme = true) {
        AuraDawApp()
      }
    }
  }
}

@Composable
fun AuraDawApp(viewModel: DawViewModel = viewModel()) {
  val context = LocalContext.current
  val currentTab by viewModel.currentTab.collectAsState()
  val toastMessage by viewModel.toastMessage.collectAsState()

  var showPresetsDialog by remember { mutableStateOf(false) }
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  // Permission Launcher for Microphone Recording
  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    if (isGranted) {
      viewModel.showToast("Microphone permission granted! Ready to record audio takes.")
    } else {
      viewModel.showToast("Microphone permission denied. Live audio recording disabled.")
    }
  }

  LaunchedEffect(Unit) {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
      permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }
  }

  // Toast / Status Message feedback
  LaunchedEffect(toastMessage) {
    toastMessage?.let { msg ->
      scope.launch {
        snackbarHostState.showSnackbar(msg)
      }
      viewModel.clearToast()
    }
  }

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .background(DarkStudioBg)
      .windowInsetsPadding(WindowInsets.safeDrawing),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    containerColor = DarkStudioBg
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      // 1. Top DAW Transport Bar (Playhead, BPM, VU, Loop, Presets, View Switcher)
      TopTransportBar(
        viewModel = viewModel,
        onOpenPresets = { showPresetsDialog = true }
      )

      // 2. Active DAW Workspace Tab
      Box(modifier = Modifier.weight(1f).fillMaxSize()) {
        AnimatedContent(
          targetState = currentTab,
          transitionSpec = { fadeIn() togetherWith fadeOut() },
          label = "daw_workspace"
        ) { targetTab ->
          when (targetTab) {
            DawTab.TIMELINE -> TimelineView(viewModel = viewModel)
            DawTab.PIANO_ROLL -> PianoRollView(viewModel = viewModel)
            DawTab.HARMOR -> HarmorPluginView(viewModel = viewModel)
            DawTab.NEWTONE -> NewtoneVocalView(viewModel = viewModel)
            DawTab.MIXER -> MixerRackView(viewModel = viewModel)
            DawTab.MASTERING -> MasteringSuiteView(viewModel = viewModel)
            DawTab.AUTOMATION -> AutomationView(viewModel = viewModel)
            DawTab.CLOUD_COLLAB -> CloudCollabView(viewModel = viewModel)
          }
        }
      }
    }

    // Preset Manager Dialog
    if (showPresetsDialog) {
      PresetManagerDialog(
        viewModel = viewModel,
        onDismiss = { showPresetsDialog = false }
      )
    }
  }
}
