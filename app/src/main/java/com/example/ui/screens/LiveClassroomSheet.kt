package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FrontHand
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.LiveMicAudioEngine
import com.example.data.model.LiveSessionItem
import com.example.ui.components.PulsingLiveDot
import com.example.ui.components.SheikhAvatarBadge
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.GoldHighlight
import com.example.ui.theme.LiveCrimson
import com.example.ui.theme.LocalAlMaqraColors
import com.example.ui.theme.ParchmentCream
import com.example.util.LocalAlMaqraStrings
import kotlinx.coroutines.delay

@Composable
fun LiveClassroomScreen(
  session: LiveSessionItem,
  elapsedSeconds: Long,
  onCloseWithoutLog: () -> Unit,
  onFinishAndLog: (Int) -> Unit,
  onSaveQuickNote: (String, String, String, String, String) -> Unit,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val strings = LocalAlMaqraStrings.current
  val ext = LocalAlMaqraColors.current

  val audioEngine = remember { LiveMicAudioEngine() }
  val micAmplitude by audioEngine.amplitude.collectAsStateWithLifecycle()

  var hasMicPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED,
    )
  }
  var micActive by remember { mutableStateOf(false) }
  var handRaised by remember { mutableStateOf(false) }
  var sessionSeconds by remember { mutableIntStateOf(0) }
  var showNoteDialog by remember { mutableStateOf(false) }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
  ) { granted ->
    hasMicPermission = granted
    if (granted) {
      micActive = true
      audioEngine.startCapture(scope)
    }
  }

  LaunchedEffect(Unit) {
    while (true) {
      delay(1000L)
      sessionSeconds += 1
    }
  }

  DisposableEffect(Unit) {
    onDispose { audioEngine.stopCapture() }
  }

  val remaining = (session.remainingSeconds - elapsedSeconds).coerceAtLeast(0L)
  val countdownText = String.format("%d:%02d:%02d", remaining / 3600, (remaining % 3600) / 60, remaining % 60)

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .navigationBarsPadding(),
  ) {
    // Solid Brand Header
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(ext.headerBackground),
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .statusBarsPadding()
          .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        IconButton(onClick = onCloseWithoutLog) {
          Icon(Icons.Filled.Close, contentDescription = strings.close, tint = ParchmentCream)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(strings.liveClassroom, style = MaterialTheme.typography.titleMedium, color = ParchmentCream)
          Text("$countdownText remaining", style = MaterialTheme.typography.labelSmall, color = ParchmentCream.copy(alpha = 0.85f))
        }
        PulsingLiveDot(label = strings.ongoingBadge)
      }
    }

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(18.dp),
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      // Teacher & Halaqah Card
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ext.cardBorder),
      ) {
        Column(
          modifier = Modifier.padding(18.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          SheikhAvatarBadge(initial = session.teacherInitial, size = 68.dp)
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = session.arabicTitle,
            fontFamily = AmiriFontFamily,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
          )
          Text(
            text = session.teacherName,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "${session.track} • ${session.level}",
            style = MaterialTheme.typography.bodySmall,
            color = ext.mutedLabel,
          )
        }
      }

      // Live Recitation Microphone & Audio Level Waveform
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ext.cardBorder),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Text(
            text = if (micActive) "Microphone Live — Reciting" else "Microphone Muted",
            style = MaterialTheme.typography.titleSmall,
            color = if (micActive) MaterialTheme.colorScheme.primary else ext.mutedLabel,
          )
          Spacer(modifier = Modifier.height(16.dp))
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .height(54.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            val weights = listOf(0.45f, 0.7f, 0.95f, 0.65f, 1f, 0.8f, 0.9f, 0.6f, 0.75f, 0.5f)
            weights.forEach { w ->
              val activeHeight = if (micActive) (10f + 40f * micAmplitude * w) else 8f
              Box(
                modifier = Modifier
                  .width(8.dp)
                  .height(activeHeight.dp)
                  .clip(CircleShape)
                  .background(if (micActive) ForestGreen else ext.mutedLabel.copy(alpha = 0.3f)),
              )
            }
          }
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = if (handRaised) "Turn Queue Position: #2 (Next after current student)" else "Tap Request Turn to enter the Sheikh's recitation queue",
            style = MaterialTheme.typography.bodySmall,
            color = ext.mutedLabel,
          )
        }
      }

      // Controls Row
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Button(
            onClick = {
              if (!hasMicPermission) {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
              } else if (micActive) {
                micActive = false
                audioEngine.stopCapture()
              } else {
                micActive = true
                audioEngine.startCapture(scope)
              }
            },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = if (micActive) LiveCrimson else ForestGreen,
              contentColor = Color.White,
            ),
            modifier = Modifier
              .weight(1f)
              .height(52.dp),
          ) {
            Icon(
              imageVector = if (micActive) Icons.Filled.MicOff else Icons.Filled.Mic,
              contentDescription = null,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (micActive) strings.muteMic else strings.unmuteMic)
          }

          OutlinedButton(
            onClick = { handRaised = !handRaised },
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, if (handRaised) GoldHighlight else ext.cardBorder),
            modifier = Modifier
              .weight(1f)
              .height(52.dp),
          ) {
            Icon(
              imageVector = Icons.Filled.FrontHand,
              contentDescription = null,
              tint = if (handRaised) GoldHighlight else MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (handRaised) strings.handRaised else strings.raiseHand, maxLines = 1)
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          OutlinedButton(
            onClick = { showNoteDialog = true },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .weight(1f)
              .height(48.dp),
          ) {
            Icon(Icons.Filled.NoteAdd, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text(strings.addNote)
          }

          Button(
            onClick = {
              audioEngine.stopCapture()
              val loggedMinutes = (sessionSeconds / 60).coerceAtLeast(15)
              onFinishAndLog(loggedMinutes)
            },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = ParchmentCream),
            modifier = Modifier
              .weight(1f)
              .height(48.dp),
          ) {
            Icon(Icons.Filled.CallEnd, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text(strings.finishSession, maxLines = 1)
          }
        }
      }
    }
  }

  if (showNoteDialog) {
    var surah by remember { mutableStateOf("Al-Baqarah") }
    var ayah by remember { mutableStateOf("1-10") }
    var body by remember { mutableStateOf("") }

    AlertDialog(
      onDismissRequest = { showNoteDialog = false },
      title = { Text("Tajweed Correction Note") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(value = surah, onValueChange = { surah = it }, label = { Text("Surah") })
          OutlinedTextField(value = ayah, onValueChange = { ayah = it }, label = { Text("Ayah Range") })
          OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text("Teacher Feedback") }, minLines = 2)
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onSaveQuickNote("Session Feedback: ${session.teacherName}", surah, ayah, "feedback", body)
            showNoteDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = ParchmentCream),
        ) { Text(strings.save) }
      },
      dismissButton = {
        TextButton(onClick = { showNoteDialog = false }) { Text(strings.cancel) }
      },
    )
  }
}
