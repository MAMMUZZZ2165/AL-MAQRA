package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.CatalogData
import com.example.data.model.Certificate
import com.example.data.model.Enrollment
import com.example.data.model.JuzProgress
import com.example.data.model.JuzStatus
import com.example.data.model.OralTest
import com.example.data.model.StudentNote
import com.example.data.model.UserProfile
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.QrCodeGraphic
import com.example.ui.components.SegmentedPillToggle
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.GoldHighlight
import com.example.ui.theme.JuzCompletedDot
import com.example.ui.theme.JuzInProgressDot
import com.example.ui.theme.JuzIncompleteDot
import com.example.ui.theme.LocalAlMaqraColors
import com.example.ui.theme.ParchmentCream
import com.example.util.LocalAlMaqraStrings

@Composable
fun ProfileScreen(
  profile: UserProfile?,
  juzList: List<JuzProgress>,
  enrollments: List<Enrollment>,
  tests: List<OralTest>,
  certificates: List<Certificate>,
  notes: List<StudentNote>,
  onUpdatePersonalInfo: (String, String, String, String, String, String, String) -> Unit,
  onUpdateJuzStatus: (Int, JuzStatus) -> Unit,
  onNavigateToCourses: () -> Unit,
  onNavigateToGroups: () -> Unit,
  onNavigateToCalendar: () -> Unit,
  onScheduleTest: (String, String, String, String) -> Unit,
  onAddNote: (String, String, String, String, String) -> Unit,
  onDeleteNote: (String) -> Unit,
) {
  val strings = LocalAlMaqraStrings.current
  val tabs = listOf(
    strings.tabAbout,
    strings.tabProgress,
    strings.tabSchedule,
    strings.tabTests,
    strings.tabAchievements,
    strings.tabNotes,
  )
  var selectedTab by remember { mutableIntStateOf(0) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
  ) {
    ScrollableTabRow(
      selectedTabIndex = selectedTab,
      containerColor = MaterialTheme.colorScheme.surface,
      contentColor = MaterialTheme.colorScheme.primary,
      edgePadding = 12.dp,
      indicator = { tabPositions ->
        TabRowDefaults.SecondaryIndicator(
          modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
          color = MaterialTheme.colorScheme.primary,
        )
      },
    ) {
      tabs.forEachIndexed { index, title ->
        Tab(
          selected = selectedTab == index,
          onClick = { selectedTab = index },
          text = { Text(title, style = MaterialTheme.typography.titleSmall) },
          modifier = Modifier.testTag("profile_tab_$index"),
        )
      }
    }

    when (selectedTab) {
      0 -> AboutTab(profile = profile, onSaveInfo = onUpdatePersonalInfo)
      1 -> ProgressJuzTab(juzList = juzList, onUpdateJuzStatus = onUpdateJuzStatus)
      2 -> ScheduleTab(
        enrollments = enrollments,
        onMyCourses = onNavigateToCourses,
        onOneToOne = onNavigateToCalendar,
        onMyGroups = onNavigateToGroups,
      )
      3 -> TestsTab(tests = tests, onScheduleTest = onScheduleTest)
      4 -> AchievementsTab(certificates = certificates)
      5 -> NotesTab(notes = notes, onAddNote = onAddNote, onDeleteNote = onDeleteNote)
    }
  }
}

@Composable
private fun AboutTab(
  profile: UserProfile?,
  onSaveInfo: (String, String, String, String, String, String, String) -> Unit,
) {
  val strings = LocalAlMaqraStrings.current
  val ext = LocalAlMaqraColors.current
  var showEditDialog by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ext.cardBorder),
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = strings.personalInfo,
              style = MaterialTheme.typography.titleLarge,
              color = MaterialTheme.colorScheme.onSurface,
            )
            IconButton(
              onClick = { showEditDialog = true },
              modifier = Modifier.testTag("edit_profile_button"),
            ) {
              Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = strings.editProfile,
                tint = MaterialTheme.colorScheme.primary,
              )
            }
          }
          HorizontalDivider(color = ext.cardBorder, modifier = Modifier.padding(vertical = 8.dp))

          InfoRow(Icons.Filled.Person, strings.nameLabel, profile?.arabicName ?: "محمد - صالح", useArabicFont = true)
          InfoRow(Icons.Filled.LocationOn, strings.countryLabel, profile?.country ?: "India")
          InfoRow(Icons.Filled.Phone, strings.phoneLabel, profile?.phone ?: "+918547182165")
          InfoRow(Icons.Filled.Email, strings.emailLabel, profile?.email ?: "—")
          InfoRow(Icons.Filled.Work, strings.jobTitleLabel, profile?.jobTitle ?: "student")
          InfoRow(Icons.Filled.CalendarMonth, strings.dobLabel, profile?.dateOfBirth ?: "5 April 2004")
          InfoRow(Icons.Filled.Male, strings.genderLabel, profile?.gender ?: "Male")
        }
      }
    }
  }

  if (showEditDialog) {
    var displayName by remember { mutableStateOf(profile?.displayName ?: "") }
    var arabicName by remember { mutableStateOf(profile?.arabicName ?: "محمد - صالح") }
    var country by remember { mutableStateOf(profile?.country ?: "India") }
    var phone by remember { mutableStateOf(profile?.phone ?: "+918547182165") }
    var jobTitle by remember { mutableStateOf(profile?.jobTitle ?: "student") }
    var dob by remember { mutableStateOf(profile?.dateOfBirth ?: "5 April 2004") }
    var gender by remember { mutableStateOf(profile?.gender ?: "Male") }

    AlertDialog(
      onDismissRequest = { showEditDialog = false },
      title = { Text(strings.editProfile) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(value = displayName, onValueChange = { displayName = it }, label = { Text("English Name") })
          OutlinedTextField(value = arabicName, onValueChange = { arabicName = it }, label = { Text("Arabic Name") })
          OutlinedTextField(value = country, onValueChange = { country = it }, label = { Text(strings.countryLabel) })
          OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text(strings.phoneLabel) })
          OutlinedTextField(value = jobTitle, onValueChange = { jobTitle = it }, label = { Text(strings.jobTitleLabel) })
          OutlinedTextField(value = dob, onValueChange = { dob = it }, label = { Text(strings.dobLabel) })
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Male", "Female").forEach { option ->
              OutlinedButton(
                onClick = { gender = option },
                border = BorderStroke(1.dp, if (gender == option) ForestGreen else ext.cardBorder),
              ) {
                Text(option)
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onSaveInfo(displayName, arabicName, country, phone, jobTitle, dob, gender)
            showEditDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = ParchmentCream),
        ) { Text(strings.save) }
      },
      dismissButton = {
        TextButton(onClick = { showEditDialog = false }) { Text(strings.cancel) }
      },
    )
  }
}

@Composable
private fun InfoRow(
  icon: ImageVector,
  label: String,
  value: String,
  useArabicFont: Boolean = false,
) {
  val ext = LocalAlMaqraColors.current
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = label,
      tint = MaterialTheme.colorScheme.primary,
      modifier = Modifier.size(20.dp),
    )
    Spacer(modifier = Modifier.width(12.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.weight(0.45f),
    )
    Text(
      text = value,
      fontFamily = if (useArabicFont) AmiriFontFamily else MaterialTheme.typography.bodyMedium.fontFamily,
      style = MaterialTheme.typography.bodyMedium,
      color = ext.mutedLabel,
      modifier = Modifier.weight(0.55f),
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProgressJuzTab(
  juzList: List<JuzProgress>,
  onUpdateJuzStatus: (Int, JuzStatus) -> Unit,
) {
  val strings = LocalAlMaqraStrings.current
  val ext = LocalAlMaqraColors.current
  var selectedJuzNumber by remember { mutableStateOf<Int?>(null) }

  val juzByNumber = remember(juzList) { juzList.associateBy { it.juzNumber } }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    // Subject selector box
    item {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ext.cardBorder),
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = strings.subjectQuran,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
          )
        }
      }
    }

    // Legend Pills
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        LegendPill(
          label = strings.completedLabel,
          dotColor = JuzCompletedDot,
          bgColor = ext.juzCompletedBg,
          textColor = ext.juzCompletedText,
          modifier = Modifier.weight(1f),
        )
        LegendPill(
          label = strings.incompleteLabel,
          dotColor = JuzIncompleteDot,
          bgColor = ext.juzIncompleteBg,
          textColor = ext.juzIncompleteText,
          modifier = Modifier.weight(1f),
        )
        LegendPill(
          label = strings.inProgressLabel,
          dotColor = JuzInProgressDot,
          bgColor = ext.juzInProgressBg,
          textColor = ext.juzInProgressText,
          modifier = Modifier.weight(1f),
        )
      }
    }

    // 5-column 30-Juz Grid (6 rows x 5 columns)
    val rows = (1..30).chunked(5)
    items(rows) { rowNumbers ->
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        rowNumbers.forEach { num ->
          val progress = juzByNumber[num]
          val status = JuzStatus.fromRaw(progress?.status)
          val (bgColor, txtColor) = when (status) {
            JuzStatus.COMPLETED -> ext.juzCompletedBg to ext.juzCompletedText
            JuzStatus.IN_PROGRESS -> ext.juzInProgressBg to ext.juzInProgressText
            JuzStatus.INCOMPLETE -> ext.juzIncompleteBg to ext.juzIncompleteText
          }
          Surface(
            modifier = Modifier
              .weight(1f)
              .aspectRatio(0.95f)
              .clip(RoundedCornerShape(12.dp))
              .clickable { selectedJuzNumber = num }
              .testTag("juz_tile_$num"),
            shape = RoundedCornerShape(12.dp),
            color = bgColor,
          ) {
            Column(
              modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center,
            ) {
              Text(
                text = strings.juzLabel,
                style = MaterialTheme.typography.labelSmall,
                color = txtColor,
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "$num",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = txtColor,
              )
            }
          }
        }
      }
    }
  }

  selectedJuzNumber?.let { num ->
    val meta = CatalogData.juzMetadata.firstOrNull { it.number == num }
    val prog = juzByNumber[num]
    val status = JuzStatus.fromRaw(prog?.status)
    val completedAyahs = prog?.completedAyahs ?: 0
    val totalAyahs = meta?.totalAyahs ?: 148
    val ratio = (completedAyahs.toFloat() / totalAyahs.coerceAtLeast(1)).coerceIn(0f, 1f)

    ModalBottomSheet(onDismissRequest = { selectedJuzNumber = null }) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "${strings.juzLabel} $num — ${meta?.arabicName ?: ""}",
            fontFamily = AmiriFontFamily,
            style = MaterialTheme.typography.headlineSmall,
          )
          Text(
            text = "$completedAyahs / $totalAyahs Ayahs",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
          )
        }
        Text(
          text = meta?.surahSpan ?: "",
          style = MaterialTheme.typography.bodyMedium,
          color = ext.mutedLabel,
        )
        LinearProgressIndicator(
          progress = { ratio },
          modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(CircleShape),
          color = ForestGreen,
        )
        Text("Update Status", style = MaterialTheme.typography.labelLarge)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          listOf(
            JuzStatus.COMPLETED to strings.completedLabel,
            JuzStatus.IN_PROGRESS to strings.inProgressLabel,
            JuzStatus.INCOMPLETE to strings.incompleteLabel,
          ).forEach { (targetStatus, label) ->
            val selected = status == targetStatus
            OutlinedButton(
              onClick = {
                onUpdateJuzStatus(num, targetStatus)
                selectedJuzNumber = null
              },
              modifier = Modifier.weight(1f),
              colors = if (selected) {
                ButtonDefaults.outlinedButtonColors(containerColor = ForestGreen, contentColor = ParchmentCream)
              } else {
                ButtonDefaults.outlinedButtonColors()
              },
            ) {
              Text(label, maxLines = 1, style = MaterialTheme.typography.labelSmall)
            }
          }
        }
        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }
}

@Composable
private fun LegendPill(
  label: String,
  dotColor: Color,
  bgColor: Color,
  textColor: Color,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(10.dp),
    color = bgColor,
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center,
    ) {
      Box(
        modifier = Modifier
          .size(8.dp)
          .clip(CircleShape)
          .background(dotColor),
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
        color = textColor,
        maxLines = 1,
      )
    }
  }
}

@Composable
private fun ScheduleTab(
  enrollments: List<Enrollment>,
  onMyCourses: () -> Unit,
  onOneToOne: () -> Unit,
  onMyGroups: () -> Unit,
) {
  val strings = LocalAlMaqraStrings.current
  val courseCount = enrollments.count { it.targetType == "course" }
  val groupCount = enrollments.count { it.targetType == "group" }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        ScheduleShortcutCard(
          icon = Icons.Filled.MenuBook,
          title = strings.myCourses,
          subtitle = "$courseCount active",
          onClick = onMyCourses,
          modifier = Modifier.weight(1f),
        )
        ScheduleShortcutCard(
          icon = Icons.Filled.EventAvailable,
          title = strings.upcomingOneToOne,
          subtitle = "View calendar",
          onClick = onOneToOne,
          modifier = Modifier.weight(1f),
        )
      }
    }
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        ScheduleShortcutCard(
          icon = Icons.Filled.Groups,
          title = strings.myGroups,
          subtitle = "$groupCount joined",
          onClick = onMyGroups,
          modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.weight(1f))
      }
    }
  }
}

@Composable
private fun ScheduleShortcutCard(
  icon: ImageVector,
  title: String,
  subtitle: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val ext = LocalAlMaqraColors.current
  Surface(
    modifier = modifier
      .aspectRatio(1.1f)
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, ext.cardBorder),
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Icon(
        imageVector = icon,
        contentDescription = title,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(36.dp),
      )
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = ext.mutedLabel,
      )
    }
  }
}

@Composable
private fun TestsTab(
  tests: List<OralTest>,
  onScheduleTest: (String, String, String, String) -> Unit,
) {
  val strings = LocalAlMaqraStrings.current
  val ext = LocalAlMaqraColors.current
  var showDialog by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    if (tests.isEmpty()) {
      item {
        EmptyStateCard(
          message = strings.noTestsYet,
          ctaLabel = strings.scheduleOralTest,
          onCtaClick = { showDialog = true },
        )
      }
    } else {
      items(tests, key = { it.id }) { test ->
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, ext.cardBorder),
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(test.title, style = MaterialTheme.typography.titleMedium)
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = ForestGreen.copy(alpha = 0.14f),
              ) {
                Text(
                  text = test.status,
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
              }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text("${test.juzRange} • ${test.sheikhName}", style = MaterialTheme.typography.bodyMedium)
            Text(test.scheduledDate, style = MaterialTheme.typography.bodySmall, color = ext.mutedLabel)
          }
        }
      }
      item {
        Button(
          onClick = { showDialog = true },
          colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = ParchmentCream),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(strings.scheduleOralTest)
        }
      }
    }
  }

  if (showDialog) {
    var title by remember { mutableStateOf("Juz 30 Oral Evaluation") }
    var juzRange by remember { mutableStateOf("Juz 30") }
    var sheikh by remember { mutableStateOf("Sheikh Ali Habibullah") }
    var date by remember { mutableStateOf("Friday • 7:00 PM") }

    AlertDialog(
      onDismissRequest = { showDialog = false },
      title = { Text(strings.scheduleOralTest) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Exam Title") })
          OutlinedTextField(value = juzRange, onValueChange = { juzRange = it }, label = { Text("Juz Range") })
          OutlinedTextField(value = sheikh, onValueChange = { sheikh = it }, label = { Text("Sheikh") })
          OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Preferred Date & Time") })
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onScheduleTest(title, juzRange, sheikh, date)
            showDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = ParchmentCream),
        ) { Text(strings.save) }
      },
      dismissButton = {
        TextButton(onClick = { showDialog = false }) { Text(strings.cancel) }
      },
    )
  }
}

@Composable
private fun AchievementsTab(certificates: List<Certificate>) {
  val strings = LocalAlMaqraStrings.current
  val ext = LocalAlMaqraColors.current
  var selectedSubTab by remember { mutableIntStateOf(0) }
  var searchQuery by remember { mutableStateOf("") }

  val filteredCerts = remember(certificates, searchQuery) {
    if (searchQuery.isBlank()) certificates
    else certificates.filter {
      it.title.contains(searchQuery, ignoreCase = true) ||
        it.track.contains(searchQuery, ignoreCase = true)
    }
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item {
      SegmentedPillToggle(
        options = listOf(strings.certificates, strings.myBadges),
        selectedIndex = selectedSubTab,
        onSelect = { selectedSubTab = it },
      )
    }

    if (selectedSubTab == 0) {
      item {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
          placeholder = { Text(strings.searchCertificates) },
          shape = RoundedCornerShape(14.dp),
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
      }
      if (filteredCerts.isEmpty()) {
        item {
          EmptyStateCard(message = strings.noCertificatesYet)
        }
      } else {
        items(filteredCerts, key = { it.id }) { cert ->
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
                Text(cert.title, style = MaterialTheme.typography.titleMedium)
                Text(cert.track, style = MaterialTheme.typography.bodySmall, color = ext.mutedLabel)
                Text("Code: ${cert.verificationCode}", style = MaterialTheme.typography.labelSmall)
              }
              QrCodeGraphic(modifier = Modifier.size(64.dp))
            }
          }
        }
      }
    } else {
      items(CatalogData.badges, key = { it.id }) { badge ->
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
            Surface(
              shape = CircleShape,
              color = if (badge.unlocked) GoldHighlight.copy(alpha = 0.22f) else ext.cardBorder,
              modifier = Modifier.size(44.dp),
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Filled.Badge,
                  contentDescription = null,
                  tint = if (badge.unlocked) GoldHighlight else ext.mutedLabel,
                )
              }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(badge.title, style = MaterialTheme.typography.titleMedium)
              Text(badge.subtitle, style = MaterialTheme.typography.bodySmall, color = ext.mutedLabel)
            }
            if (badge.unlocked) {
              Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Unlocked",
                tint = JuzCompletedDot,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun NotesTab(
  notes: List<StudentNote>,
  onAddNote: (String, String, String, String, String) -> Unit,
  onDeleteNote: (String) -> Unit,
) {
  val strings = LocalAlMaqraStrings.current
  val ext = LocalAlMaqraColors.current
  var showDialog by remember { mutableStateOf(false) }

  Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      if (notes.isEmpty()) {
        item {
          EmptyStateCard(
            message = strings.noNotesYet,
            ctaLabel = strings.addNote,
            onCtaClick = { showDialog = true },
          )
        }
      } else {
        items(notes, key = { it.id }) { note ->
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, ext.cardBorder),
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(note.title, style = MaterialTheme.typography.titleMedium)
                  Text(
                    "${note.surahRef} • Ayah ${note.ayahRange} • ${note.category}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                  )
                }
                IconButton(onClick = { onDeleteNote(note.id) }) {
                  Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete note")
                }
              }
              Spacer(modifier = Modifier.height(6.dp))
              Text(note.content, style = MaterialTheme.typography.bodyMedium)
            }
          }
        }
      }
    }

    if (notes.isNotEmpty()) {
      FloatingActionButton(
        onClick = { showDialog = true },
        containerColor = ForestGreen,
        contentColor = ParchmentCream,
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(16.dp),
      ) {
        Icon(Icons.Filled.Add, contentDescription = strings.addNote)
      }
    }
  }

  if (showDialog) {
    var title by remember { mutableStateOf("") }
    var surah by remember { mutableStateOf("Al-Baqarah") }
    var ayah by remember { mutableStateOf("255") }
    var content by remember { mutableStateOf("") }

    AlertDialog(
      onDismissRequest = { showDialog = false },
      title = { Text(strings.addNote) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Note Title") })
          OutlinedTextField(value = surah, onValueChange = { surah = it }, label = { Text("Surah") })
          OutlinedTextField(value = ayah, onValueChange = { ayah = it }, label = { Text("Ayah Range") })
          OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Tajweed / Hifz Feedback") }, minLines = 3)
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onAddNote(title, surah, ayah, "tajweed", content)
            showDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = ParchmentCream),
        ) { Text(strings.save) }
      },
      dismissButton = {
        TextButton(onClick = { showDialog = false }) { Text(strings.cancel) }
      },
    )
  }
}
