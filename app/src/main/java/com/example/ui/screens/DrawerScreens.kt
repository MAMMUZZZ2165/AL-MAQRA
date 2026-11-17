package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.CatalogData
import com.example.data.model.AppLanguage
import com.example.data.model.DirectMessage
import com.example.data.model.Enrollment
import com.example.data.model.ReciterProfile
import com.example.data.model.SessionLog
import com.example.data.model.StudyPlan
import com.example.data.model.ThemeMode
import com.example.data.model.UserProfile
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.SegmentedPillToggle
import com.example.ui.components.SheikhAvatarBadge
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.GoldHighlight
import com.example.ui.theme.LocalAlMaqraColors
import com.example.ui.theme.ParchmentCream
import com.example.util.LocalAlMaqraStrings

@Composable
fun ProgressAnalyticsScreen(
  profile: UserProfile?,
  sessionLogs: List<SessionLog>,
  filterRange: String,
  onSelectFilterRange: (String) -> Unit,
) {
  val strings = LocalAlMaqraStrings.current
  val ext = LocalAlMaqraColors.current
  var historyTab by remember { mutableIntStateOf(1) } // Registered courses default per screenshot
  var selectedLogDetail by remember { mutableStateOf<SessionLog?>(null) }

  val categoryKey = when (historyTab) {
    0 -> "guest"
    1 -> "registered"
    else -> "one_to_one"
  }
  val filteredLogs = remember(sessionLogs, categoryKey) {
    sessionLogs.filter { it.sessionCategory == categoryKey }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    // Filter Chips Row
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        listOf("All Time", "30 Days", "7 Days").forEach { range ->
          FilterChip(
            selected = filterRange == range,
            onClick = { onSelectFilterRange(range) },
            label = { Text(range) },
          )
        }
      }
    }

    // General Metrics Grid (2-column + 3-column with lattice watermarks)
    item {
      Text(
        text = strings.generalMetrics,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
      )
      Spacer(modifier = Modifier.height(10.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        LatticeMetricCard(
          label = strings.avgReadingTime,
          value = String.format("%.1f %s", profile?.readingHours ?: 144.0, strings.hoursUnit),
          modifier = Modifier.weight(1f),
        )
        LatticeMetricCard(
          label = strings.avgRating,
          value = String.format("%.1f / 5.0", profile?.avgRating ?: 0.0),
          modifier = Modifier.weight(1f),
        )
      }
      Spacer(modifier = Modifier.height(10.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        LatticeMetricCard(
          label = strings.oneToOneSessions,
          value = "${profile?.oneToOneCount ?: 6}",
          modifier = Modifier.weight(1f),
        )
        LatticeMetricCard(
          label = strings.attendedSessions,
          value = "${profile?.attendedSessionsCount ?: 36}",
          modifier = Modifier.weight(1f),
        )
        LatticeMetricCard(
          label = strings.testsCompleted,
          value = "${profile?.testsCompletedCount ?: 0}",
          modifier = Modifier.weight(1f),
        )
      }
    }

    // Plans Metrics 3x2 Grid
    item {
      Text(
        text = strings.plansMetrics,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
      )
      Spacer(modifier = Modifier.height(10.dp))
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
          LatticeMetricCard(strings.memorizedPages, "${profile?.memorizedPages ?: 64}", Modifier.weight(1f))
          LatticeMetricCard(strings.memorizedSurah, "${profile?.memorizedSurahs ?: 38}", Modifier.weight(1f))
          LatticeMetricCard(strings.memorizedJuz, "${profile?.memorizedJuz ?: 3}", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
          LatticeMetricCard(strings.memorizedAyah, "${profile?.memorizedAyahs ?: 820}", Modifier.weight(1f))
          LatticeMetricCard(strings.memorizedLetters, "${profile?.memorizedLetters ?: 34200}", Modifier.weight(1f))
          LatticeMetricCard(strings.gainedDeeds, "${profile?.gainedDeeds ?: 342000}", Modifier.weight(1f))
        }
      }
    }

    // Session History Breakdown
    item {
      Text(
        text = strings.titleProgress,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
      )
      Spacer(modifier = Modifier.height(10.dp))
      SegmentedPillToggle(
        options = listOf(strings.guestSessions, strings.registeredCourses, strings.oneToOneTab),
        selectedIndex = historyTab,
        onSelect = { historyTab = it },
      )
    }

    if (filteredLogs.isEmpty()) {
      item {
        EmptyStateCard(message = strings.noSessionsFound)
      }
    } else {
      items(filteredLogs, key = { it.id }) { log ->
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(18.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, ext.cardBorder),
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Surface(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              color = ext.cardBannerBg,
            ) {
              Text(
                text = log.courseTitle,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = ext.cardBannerText,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
              )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(
                text = log.arabicTitle,
                fontFamily = AmiriFontFamily,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
              )
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Filled.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(strings.subjectQuran, style = MaterialTheme.typography.labelLarge)
              }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              SheikhAvatarBadge(initial = log.teacherName.firstOrNull()?.uppercase() ?: "ع", size = 36.dp)
              Spacer(modifier = Modifier.width(10.dp))
              Text(log.teacherName, style = MaterialTheme.typography.bodyMedium)
              Text("  •  ", color = ext.goldAccent)
              Text(log.teacherCountry, style = MaterialTheme.typography.bodySmall, color = ext.mutedLabel)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.SignalCellularAlt, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(4.dp))
                Text(log.level, style = MaterialTheme.typography.bodySmall)
              }
              Text(log.courseTitle, style = MaterialTheme.typography.bodySmall, color = ext.mutedLabel)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Filled.VideoLibrary, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
              Spacer(Modifier.width(6.dp))
              Text("${log.completedSessions} ${strings.sessionsUnit}", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = ext.cardBorder)
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
              onClick = { selectedLogDetail = log },
              shape = RoundedCornerShape(12.dp),
              border = BorderStroke(1.dp, ext.cardBorder),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Text(strings.viewSessions, color = MaterialTheme.colorScheme.primary)
            }
          }
        }
      }
    }
  }

  selectedLogDetail?.let { log ->
    AlertDialog(
      onDismissRequest = { selectedLogDetail = null },
      title = { Text(log.courseTitle) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(log.arabicTitle, fontFamily = AmiriFontFamily, style = MaterialTheme.typography.titleMedium)
          Text("Teacher: ${log.teacherName} (${log.teacherCountry})")
          Text("Level: ${log.level}")
          Text("Completed Sessions: ${log.completedSessions}")
          Text("Total Study Time: ${log.durationMinutes} minutes")
        }
      },
      confirmButton = {
        TextButton(onClick = { selectedLogDetail = null }) { Text(strings.close) }
      },
    )
  }
}

@Composable
private fun LatticeMetricCard(
  label: String,
  value: String,
  modifier: Modifier = Modifier,
) {
  val ext = LocalAlMaqraColors.current
  Surface(
    modifier = modifier.height(92.dp),
    shape = RoundedCornerShape(14.dp),
    color = ext.metricCardBg,
    border = BorderStroke(1.dp, ext.cardBorder),
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(10.dp),
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = ext.mutedLabel,
        maxLines = 2,
      )
      Text(
        text = value,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface,
      )
    }
  }
}

@Composable
fun RecitersScreen(
  reciters: List<ReciterProfile> = CatalogData.reciters,
  onCallReciter: (ReciterProfile) -> Unit,
  onMessageReciter: (ReciterProfile) -> Unit,
) {
  val ext = LocalAlMaqraColors.current
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    items(reciters, key = { it.id }) { reciter ->
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ext.cardBorder),
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            SheikhAvatarBadge(initial = reciter.name.split(" ").getOrNull(1)?.firstOrNull()?.toString() ?: "S", size = 48.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(reciter.arabicName, fontFamily = AmiriFontFamily, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
              Text(reciter.name, style = MaterialTheme.typography.titleSmall)
              Text("${reciter.country} • ${reciter.languages.joinToString(", ")}", style = MaterialTheme.typography.bodySmall, color = ext.mutedLabel)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Filled.Star, null, tint = GoldHighlight, modifier = Modifier.size(16.dp))
              Spacer(Modifier.width(3.dp))
              Text("${reciter.rating}", style = MaterialTheme.typography.labelLarge)
            }
          }
          Spacer(modifier = Modifier.height(8.dp))
          Text(reciter.specialty, style = MaterialTheme.typography.bodySmall, color = ext.mutedLabel)
          Spacer(modifier = Modifier.height(12.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
              onClick = { onCallReciter(reciter) },
              colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = ParchmentCream),
              modifier = Modifier.weight(1f),
            ) {
              Icon(Icons.Filled.Call, null, modifier = Modifier.size(16.dp))
              Spacer(Modifier.width(6.dp))
              Text(if (reciter.availableNow) "Call Now" else "Quick Call")
            }
            OutlinedButton(
              onClick = { onMessageReciter(reciter) },
              modifier = Modifier.weight(1f),
            ) {
              Text("Message")
            }
          }
        }
      }
    }
  }
}

@Composable
fun StudyPlansScreen(
  plans: List<StudyPlan>,
  onAddPlan: (String, Int, String, String) -> Unit,
  onTogglePlan: (StudyPlan) -> Unit,
) {
  val strings = LocalAlMaqraStrings.current
  val ext = LocalAlMaqraColors.current
  var showCreateDialog by remember { mutableStateOf(false) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      if (plans.isEmpty()) {
        item {
          EmptyStateCard(
            message = "Build a daily Hifz or Tilawah routine tailored to your schedule.",
            ctaLabel = "Create Study Plan",
            onCtaClick = { showCreateDialog = true },
          )
        }
      } else {
        items(plans, key = { it.id }) { plan ->
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, ext.cardBorder),
          ) {
            Row(
              modifier = Modifier.padding(16.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(plan.title, style = MaterialTheme.typography.titleMedium)
                Text("${plan.dailyTargetPages} pages/day • ${plan.scheduleDays}", style = MaterialTheme.typography.bodySmall, color = ext.mutedLabel)
                Text("Preferred: ${plan.preferredTime}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
              }
              Switch(
                checked = plan.status == "active",
                onCheckedChange = { onTogglePlan(plan) },
              )
            }
          }
        }
        item {
          Button(
            onClick = { showCreateDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = ParchmentCream),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Icon(Icons.Filled.Add, null)
            Spacer(Modifier.width(6.dp))
            Text("Add Study Plan")
          }
        }
      }
    }
  }

  if (showCreateDialog) {
    var title by remember { mutableStateOf("Fajr Hifz Memorization") }
    var pages by remember { mutableStateOf("2") }
    var days by remember { mutableStateOf("Daily") }
    var time by remember { mutableStateOf("5:45 AM") }

    AlertDialog(
      onDismissRequest = { showCreateDialog = false },
      title = { Text("Create Study Plan") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Plan Title") })
          OutlinedTextField(value = pages, onValueChange = { pages = it }, label = { Text("Daily Target Pages") })
          OutlinedTextField(value = days, onValueChange = { days = it }, label = { Text("Days") })
          OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Time Slot") })
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onAddPlan(title, pages.toIntOrNull() ?: 2, days, time)
            showCreateDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = ParchmentCream),
        ) { Text(strings.save) }
      },
      dismissButton = {
        TextButton(onClick = { showCreateDialog = false }) { Text(strings.cancel) }
      },
    )
  }
}

@Composable
fun MessagesScreen(
  messages: List<DirectMessage>,
  onSendMessage: (String, String, String) -> Unit,
) {
  val ext = LocalAlMaqraColors.current
  var selectedReciter by remember { mutableStateOf(CatalogData.reciters.first()) }
  var input by remember { mutableStateOf("") }
  val threadMessages = remember(messages, selectedReciter) {
    messages.filter { it.reciterId == selectedReciter.id }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
  ) {
    // Reciter Selector Row
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      CatalogData.reciters.take(3).forEach { reciter ->
        FilterChip(
          selected = selectedReciter.id == reciter.id,
          onClick = { selectedReciter = reciter },
          label = { Text(reciter.name.split(" ").take(2).joinToString(" "), maxLines = 1) },
        )
      }
    }

    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      if (threadMessages.isEmpty()) {
        item {
          EmptyStateCard(
            message = "Start a conversation with ${selectedReciter.name} regarding Tajweed, Hifz, or scheduling.",
          )
        }
      } else {
        items(threadMessages, key = { it.id }) { msg ->
          val isMe = msg.senderRole == "student"
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
          ) {
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = if (isMe) ForestGreen else MaterialTheme.colorScheme.surface,
              border = if (isMe) null else BorderStroke(1.dp, ext.cardBorder),
            ) {
              Text(
                text = msg.body,
                color = if (isMe) ParchmentCream else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
              )
            }
          }
        }
      }
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.surface)
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      OutlinedTextField(
        value = input,
        onValueChange = { input = it },
        placeholder = { Text("Write a message...") },
        modifier = Modifier.weight(1f),
        singleLine = true,
      )
      Spacer(Modifier.width(8.dp))
      IconButton(
        onClick = {
          if (input.isNotBlank()) {
            onSendMessage(selectedReciter.id, selectedReciter.name, input)
            input = ""
          }
        },
        modifier = Modifier
          .size(48.dp)
          .clip(CircleShape)
          .background(ForestGreen),
      ) {
        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = ParchmentCream)
      }
    }
  }
}

@Composable
fun CalendarScreen(enrollments: List<Enrollment>) {
  val ext = LocalAlMaqraColors.current
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Text("Upcoming Schedule", style = MaterialTheme.typography.titleLarge)
    }
    items(CatalogData.liveSessions, key = { it.id }) { s ->
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ext.cardBorder),
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(Icons.Filled.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary)
          Spacer(Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(s.track, style = MaterialTheme.typography.titleSmall)
            Text(s.arabicTitle, fontFamily = AmiriFontFamily, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            Text("${s.timeSlot} • ${s.teacherName}", style = MaterialTheme.typography.bodySmall, color = ext.mutedLabel)
          }
        }
      }
    }
    if (enrollments.isNotEmpty()) {
      item {
        Text("Enrolled Circles (${enrollments.size})", style = MaterialTheme.typography.titleMedium)
      }
      items(enrollments, key = { it.id }) { enr ->
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
          Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Column {
              Text(enr.arabicTitle, fontFamily = AmiriFontFamily, style = MaterialTheme.typography.titleSmall)
              Text("${enr.track} • ${enr.teacherName}", style = MaterialTheme.typography.bodySmall, color = ext.mutedLabel)
            }
          }
        }
      }
    }
  }
}

@Composable
fun SettingsScreen(
  currentTheme: ThemeMode,
  currentLanguage: AppLanguage,
  onSelectTheme: (ThemeMode) -> Unit,
  onSelectLanguage: (AppLanguage) -> Unit,
) {
  val strings = LocalAlMaqraStrings.current
  val ext = LocalAlMaqraColors.current

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ext.cardBorder),
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(strings.themeLabel, style = MaterialTheme.typography.titleMedium)
          Spacer(Modifier.height(8.dp))
          listOf(
            ThemeMode.SYSTEM to strings.themeSystem,
            ThemeMode.LIGHT to strings.themeLight,
            ThemeMode.DARK to strings.themeDark,
          ).forEach { (mode, label) ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectTheme(mode) }
                .padding(vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              RadioButton(selected = currentTheme == mode, onClick = { onSelectTheme(mode) })
              Spacer(Modifier.width(8.dp))
              Text(label, style = MaterialTheme.typography.bodyLarge)
            }
          }
        }
      }
    }

    item {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ext.cardBorder),
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(strings.languageLabel, style = MaterialTheme.typography.titleMedium)
          Spacer(Modifier.height(8.dp))
          AppLanguage.entries.forEach { lang ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectLanguage(lang) }
                .padding(vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              RadioButton(selected = currentLanguage == lang, onClick = { onSelectLanguage(lang) })
              Spacer(Modifier.width(8.dp))
              Text(lang.label, style = MaterialTheme.typography.bodyLarge)
            }
          }
        }
      }
    }
  }
}

@Composable
fun InvitationScreen(userName: String) {
  val context = LocalContext.current
  val ext = LocalAlMaqraColors.current
  val inviteCode = remember(userName) {
    "MAQRA-${userName.filter { it.isLetterOrDigit() }.uppercase().take(4).ifBlank { "HIFZ" }}-2026"
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(20.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Surface(
      shape = RoundedCornerShape(18.dp),
      color = MaterialTheme.colorScheme.surface,
      border = BorderStroke(1.dp, ext.cardBorder),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Text("Invite Friends to AL MAQRA", style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
          "Share your referral code and earn deeds whenever fellow students join live Quran circles.",
          style = MaterialTheme.typography.bodyMedium,
          color = ext.mutedLabel,
          textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(18.dp))
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = ext.cardBannerBg,
        ) {
          Text(
            text = inviteCode,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = ForestGreen,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
          )
        }
        Spacer(Modifier.height(18.dp))
        Button(
          onClick = {
            val sendIntent = Intent().apply {
              action = Intent.ACTION_SEND
              putExtra(
                Intent.EXTRA_TEXT,
                "Join me on AL MAQRA for live Quran recitation circles and 30-Juz Hifz tracking! Use code: $inviteCode",
              )
              type = "text/plain"
            }
            context.startActivity(Intent.createChooser(sendIntent, "Share AL MAQRA"))
          },
          colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = ParchmentCream),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Icon(Icons.Filled.Share, null)
          Spacer(Modifier.width(8.dp))
          Text("Share Invitation")
        }
      }
    }
  }
}

@Composable
fun AboutAlMaqraScreen() {
  val ext = LocalAlMaqraColors.current
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(20.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Surface(
      shape = RoundedCornerShape(18.dp),
      color = MaterialTheme.colorScheme.surface,
      border = BorderStroke(1.dp, ext.cardBorder),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Text("المقرأة • AL MAQRA", fontFamily = AmiriFontFamily, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        Text(
          "AL MAQRA connects students worldwide with certified Sheikhs for structured Quran memorization (Hifz), live group circles (Halaqat), one-to-one Tajweed correction, oral evaluations, and comprehensive Juz 1–30 progress tracking.",
          style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(14.dp))
        Text("Version 1.0 • Cloud-Synced Learning Platform", style = MaterialTheme.typography.labelMedium, color = ext.mutedLabel)
      }
    }
  }
}
