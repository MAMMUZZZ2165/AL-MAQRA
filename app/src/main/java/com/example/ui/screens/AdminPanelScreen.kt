package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.BOOTSTRAPPED_ADMIN_EMAIL
import com.example.data.model.AdminCourse
import com.example.data.model.AdminManager
import com.example.data.model.AdminTeacher
import com.example.data.model.CourseCatalogItem
import com.example.data.model.DailyQuote
import com.example.data.model.LoginEvent
import com.example.data.model.ReciterProfile
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.SheikhAvatarBadge
import com.example.ui.components.emailAdminLoginDigest
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.LiveCrimson
import com.example.ui.theme.LocalAlMaqraColors
import com.example.ui.theme.ParchmentCream
import com.example.util.LocalAlMaqraStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminPanelScreen(
  currentUserEmail: String?,
  isPrimaryAdmin: Boolean,
  allCourses: List<CourseCatalogItem>,
  allReciters: List<ReciterProfile>,
  adminCourses: List<AdminCourse>,
  adminTeachers: List<AdminTeacher>,
  adminManagers: List<AdminManager>,
  dailyQuotes: List<DailyQuote>,
  loginEvents: List<LoginEvent>,
  onSaveCourse: (String?, String, String, String, String, String, String, String, String, String, Int, String) -> Unit,
  onDeleteCourse: (String) -> Unit,
  onSaveTeacher: (String?, String, String, String, String, String, Double, Boolean) -> Unit,
  onToggleTeacherAvailability: (AdminTeacher) -> Unit,
  onDeleteTeacher: (String) -> Unit,
  onAddManager: (String, String, String, String) -> Unit,
  onToggleManagerStatus: (AdminManager) -> Unit,
  onDeleteManager: (String) -> Unit,
  onSaveQuote: (String?, String, String, String, String, String) -> Unit,
  onDeleteQuote: (String) -> Unit,
  onMarkLoginRead: (String) -> Unit,
  onMarkAllLoginsRead: (List<LoginEvent>) -> Unit,
) {
  val context = LocalContext.current
  val strings = LocalAlMaqraStrings.current
  val ext = LocalAlMaqraColors.current
  var selectedTab by remember { mutableIntStateOf(0) }

  var showCourseDialog by remember { mutableStateOf(false) }
  var editingCourse by remember { mutableStateOf<CourseCatalogItem?>(null) }

  var showTeacherDialog by remember { mutableStateOf(false) }
  var editingTeacher by remember { mutableStateOf<ReciterProfile?>(null) }

  var showQuoteDialog by remember { mutableStateOf(false) }
  var editingQuote by remember { mutableStateOf<DailyQuote?>(null) }

  var showAddManagerDialog by remember { mutableStateOf(false) }

  val cleanEmail = currentUserEmail?.trim()?.lowercase().orEmpty()
  val isActiveManager = remember(adminManagers, cleanEmail) {
    adminManagers.any { it.email.lowercase() == cleanEmail && it.status == "active" }
  }
  val hasManagementAccess = isPrimaryAdmin || isActiveManager
  val firestoreCourseIds = remember(adminCourses) { adminCourses.map { it.id }.toSet() }
  val firestoreTeacherMap = remember(adminTeachers) { adminTeachers.associateBy { it.id } }

  if (!isPrimaryAdmin && selectedTab >= 4) {
    selectedTab = 0
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Role Summary Banner
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(14.dp),
        color = ext.cardBannerBg,
        border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.16f)),
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(
            imageVector = if (hasManagementAccess) Icons.Filled.AdminPanelSettings else Icons.Filled.Shield,
            contentDescription = null,
            tint = ForestGreen,
          )
          Spacer(Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = when {
                isPrimaryAdmin -> "Primary Administrator Access"
                isActiveManager -> "Appointed Manager Access"
                else -> "Read-Only Preview ($cleanEmail)"
              },
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = ForestGreen,
            )
            Text(
              text = when {
                isPrimaryAdmin -> "Full control to edit all courses & teachers, manage daily quotes, appoint managers, and audit sign-in emails."
                isActiveManager -> "You can publish new courses & teachers. Editing existing catalog items & quotes is reserved for the Primary Admin."
                else -> "Only $BOOTSTRAPPED_ADMIN_EMAIL can edit all courses, teachers, daily quotes, and login alerts."
              },
              style = MaterialTheme.typography.bodySmall,
              color = ForestGreen.copy(alpha = 0.78f),
            )
          }
        }
      }

      ScrollableTabRow(
        selectedTabIndex = selectedTab,
        edgePadding = 12.dp,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        indicator = { tabPositions ->
          if (selectedTab in tabPositions.indices) {
            TabRowDefaults.SecondaryIndicator(
              modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
              color = MaterialTheme.colorScheme.primary,
            )
          }
        },
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = { Text("${strings.coursesTab} (${allCourses.size})") },
          modifier = Modifier.testTag("admin_tab_courses"),
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = { Text("${strings.teachersTab} (${allReciters.size})") },
          modifier = Modifier.testTag("admin_tab_teachers"),
        )
        Tab(
          selected = selectedTab == 2,
          onClick = { selectedTab = 2 },
          text = { Text("${strings.quotesTab} (${dailyQuotes.size})") },
          modifier = Modifier.testTag("admin_tab_quotes"),
        )
        Tab(
          selected = selectedTab == 3,
          onClick = { selectedTab = 3 },
          text = { Text("${strings.managersTab} (${adminManagers.size})") },
          modifier = Modifier.testTag("admin_tab_managers"),
        )
        if (isPrimaryAdmin) {
          val unreadLogins = loginEvents.count { it.status == "unread" }
          val badgeLabel = if (unreadLogins > 0) "${strings.loginsTab} ($unreadLogins new)" else "${strings.loginsTab} (${loginEvents.size})"
          Tab(
            selected = selectedTab == 4,
            onClick = { selectedTab = 4 },
            text = { Text(badgeLabel) },
            modifier = Modifier.testTag("admin_tab_logins"),
          )
        }
      }

      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 92.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        when (selectedTab) {
          0 -> {
            items(allCourses, key = { it.id }) { course ->
              val isCustomDoc = course.id in firestoreCourseIds
              Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, ext.cardBorder),
              ) {
                Column(modifier = Modifier.padding(14.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                      Icon(Icons.Filled.MenuBook, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                      Spacer(Modifier.width(8.dp))
                      Column {
                        Text(course.arabicTitle, fontFamily = AmiriFontFamily, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        Text(course.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                      }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      if (isPrimaryAdmin) {
                        IconButton(
                          onClick = {
                            editingCourse = course
                            showCourseDialog = true
                          },
                          modifier = Modifier.testTag("edit_course_${course.id}"),
                        ) {
                          Icon(Icons.Filled.Edit, contentDescription = "Edit course", tint = ForestGreen)
                        }
                      }
                      if (hasManagementAccess && isCustomDoc) {
                        IconButton(onClick = { onDeleteCourse(course.id) }) {
                          Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete course", tint = MaterialTheme.colorScheme.error)
                        }
                      }
                    }
                  }
                  if (course.description.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(course.description, style = MaterialTheme.typography.bodySmall, color = ext.mutedLabel)
                  }
                  Spacer(Modifier.height(6.dp))
                  Text(
                    "${course.track} • ${course.mode} • ${course.level} • ${course.language}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ext.mutedLabel,
                  )
                  Text(
                    "${course.teacherName} (${course.country}) • ${course.scheduleSummary} • ${course.seatsLeft} seats",
                    style = MaterialTheme.typography.bodySmall,
                    color = ext.mutedLabel,
                  )
                }
              }
            }
          }

          1 -> {
            items(allReciters, key = { it.id }) { reciter ->
              val firestoreDoc = firestoreTeacherMap[reciter.id]
              Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, ext.cardBorder),
              ) {
                Column(modifier = Modifier.padding(14.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    SheikhAvatarBadge(initial = reciter.name.firstOrNull()?.uppercase() ?: "S", size = 42.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                      Text(reciter.arabicName, fontFamily = AmiriFontFamily, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                      Text(reciter.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                      Text("${reciter.country} • ${reciter.languages.joinToString(", ")}", style = MaterialTheme.typography.bodySmall, color = ext.mutedLabel)
                    }
                    if (isPrimaryAdmin) {
                      IconButton(
                        onClick = {
                          editingTeacher = reciter
                          showTeacherDialog = true
                        },
                        modifier = Modifier.testTag("edit_teacher_${reciter.id}"),
                      ) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit teacher", tint = ForestGreen)
                      }
                    }
                    if (hasManagementAccess && firestoreDoc != null) {
                      IconButton(onClick = { onDeleteTeacher(firestoreDoc.id) }) {
                        Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete teacher", tint = MaterialTheme.colorScheme.error)
                      }
                    }
                  }
                  Spacer(Modifier.height(8.dp))
                  Text(reciter.specialty, style = MaterialTheme.typography.bodySmall, color = ext.mutedLabel)
                  if (hasManagementAccess && firestoreDoc != null) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                      Text(
                        text = if (firestoreDoc.availableStatus == "available") "Available for Quick Call" else "Busy / Offline",
                        style = MaterialTheme.typography.labelMedium,
                      )
                      Switch(
                        checked = firestoreDoc.availableStatus == "available",
                        onCheckedChange = { onToggleTeacherAvailability(firestoreDoc) },
                      )
                    }
                  }
                }
              }
            }
          }

          2 -> {
            item {
              Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, ext.cardBorder),
              ) {
                Row(
                  modifier = Modifier.padding(14.dp),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Icon(Icons.Filled.FormatQuote, null, tint = ForestGreen)
                  Spacer(Modifier.width(10.dp))
                  Text(
                    text = if (isPrimaryAdmin) {
                      "Add or edit Daily Qur'an & Tajweed Quotes. Published quotes appear immediately on every student's Home screen alongside curated academy quotes."
                    } else {
                      "Only the Primary Administrator ($BOOTSTRAPPED_ADMIN_EMAIL) can publish or edit Daily Tajweed Quotes."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = ext.mutedLabel,
                  )
                }
              }
            }

            if (dailyQuotes.isEmpty()) {
              item {
                EmptyStateCard(
                  message = "No custom daily quotes published yet. Built-in Qur'an & Tajweed quotes are currently showing on Home.",
                  ctaLabel = if (isPrimaryAdmin) strings.addQuote else null,
                  onCtaClick = if (isPrimaryAdmin) ({
                    editingQuote = null
                    showQuoteDialog = true
                  }) else null,
                )
              }
            } else {
              items(dailyQuotes, key = { it.id }) { quote ->
                Surface(
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(16.dp),
                  color = ext.cardBannerBg,
                  border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.16f)),
                ) {
                  Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically,
                    ) {
                      Surface(shape = RoundedCornerShape(8.dp), color = ForestGreen) {
                        Text(
                          text = "${quote.category.ifBlank { "Tajweed" }} • ${quote.dateLabel.ifBlank { "Daily" }}",
                          style = MaterialTheme.typography.labelSmall,
                          color = ParchmentCream,
                          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                      }
                      if (isPrimaryAdmin) {
                        Row {
                          IconButton(
                            onClick = {
                              editingQuote = quote
                              showQuoteDialog = true
                            },
                            modifier = Modifier.testTag("edit_quote_${quote.id}"),
                          ) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit quote", tint = ForestGreen)
                          }
                          IconButton(onClick = { onDeleteQuote(quote.id) }) {
                            Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete quote", tint = MaterialTheme.colorScheme.error)
                          }
                        }
                      }
                    }
                    if (quote.arabicText.isNotBlank()) {
                      Spacer(Modifier.height(6.dp))
                      Text(
                        text = quote.arabicText,
                        fontFamily = AmiriFontFamily,
                        style = MaterialTheme.typography.titleMedium,
                        color = ForestGreen,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth(),
                      )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                      text = quote.quoteText,
                      style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                      color = ForestGreen,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                      text = "— ${quote.source}",
                      style = MaterialTheme.typography.labelMedium,
                      color = ForestGreen.copy(alpha = 0.8f),
                    )
                  }
                }
              }
            }
          }

          3 -> {
            if (adminManagers.isEmpty()) {
              item {
                EmptyStateCard(
                  message = if (isPrimaryAdmin) {
                    "No managers appointed yet. Add a manager's Google email to grant Course & Teacher publishing permissions."
                  } else {
                    "Manager directory is managed by the primary administrator."
                  },
                  ctaLabel = if (isPrimaryAdmin) strings.addManager else null,
                  onCtaClick = if (isPrimaryAdmin) ({ showAddManagerDialog = true }) else null,
                )
              }
            } else {
              items(adminManagers, key = { it.id }) { mgr ->
                Surface(
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(16.dp),
                  color = MaterialTheme.colorScheme.surface,
                  border = BorderStroke(1.dp, ext.cardBorder),
                ) {
                  Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Icon(Icons.Filled.School, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                      Text(mgr.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                      Text(mgr.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                      Text("${mgr.role}${if (mgr.phone.isNotBlank()) " • ${mgr.phone}" else ""}", style = MaterialTheme.typography.bodySmall, color = ext.mutedLabel)
                    }
                    if (isPrimaryAdmin) {
                      Switch(
                        checked = mgr.status == "active",
                        onCheckedChange = { onToggleManagerStatus(mgr) },
                      )
                      IconButton(onClick = { onDeleteManager(mgr.id) }) {
                        Icon(Icons.Filled.DeleteOutline, contentDescription = "Remove manager", tint = MaterialTheme.colorScheme.error)
                      }
                    }
                  }
                }
              }
            }
          }

          else -> {
            // Admin Login Activity + Email Digest Tab
            item {
              val unreadCount = loginEvents.count { it.status == "unread" }
              Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, ext.cardBorder),
              ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                  Text(
                    text = "Student Sign-In Audit (${loginEvents.size} total • $unreadCount unread)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                  )
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                  ) {
                    OutlinedButton(
                      onClick = { onMarkAllLoginsRead(loginEvents) },
                      enabled = unreadCount > 0,
                      shape = RoundedCornerShape(10.dp),
                      modifier = Modifier.weight(1f),
                    ) {
                      Icon(Icons.Filled.DoneAll, null, modifier = Modifier.size(16.dp))
                      Spacer(Modifier.width(6.dp))
                      Text("Mark All Read", maxLines = 1)
                    }
                    Button(
                      onClick = { emailAdminLoginDigest(context, loginEvents) },
                      shape = RoundedCornerShape(10.dp),
                      colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = ParchmentCream),
                      modifier = Modifier.weight(1f).testTag("admin_email_login_digest"),
                    ) {
                      Icon(Icons.Filled.Email, null, modifier = Modifier.size(16.dp))
                      Spacer(Modifier.width(6.dp))
                      Text("Email Digest", maxLines = 1)
                    }
                  }
                }
              }
            }

            if (loginEvents.isEmpty()) {
              item {
                EmptyStateCard(message = "No student sign-in events recorded yet.")
              }
            } else {
              items(loginEvents, key = { it.id }) { ev ->
                val isUnread = ev.status == "unread"
                val timeStr = remember(ev.loginAt) {
                  ev.loginAt?.toDate()?.let {
                    SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(it)
                  } ?: "Just now"
                }
                Surface(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                      if (isUnread) onMarkLoginRead(ev.id)
                    },
                  shape = RoundedCornerShape(16.dp),
                  color = if (isUnread) ext.cardBannerBg else MaterialTheme.colorScheme.surface,
                  border = BorderStroke(1.dp, if (isUnread) ForestGreen else ext.cardBorder),
                ) {
                  Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Box(
                      modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isUnread) LiveCrimson else ext.mutedLabel.copy(alpha = 0.35f)),
                    )
                    Spacer(Modifier.width(12.dp))
                    Icon(Icons.Filled.Login, null, tint = ForestGreen, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                      ) {
                        Text(
                          text = ev.userName.ifBlank { "Student" },
                          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                          color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                          text = if (isUnread) "NEW" else "Read",
                          style = MaterialTheme.typography.labelSmall,
                          color = if (isUnread) LiveCrimson else ext.mutedLabel,
                        )
                      }
                      Text(
                        text = ev.userEmail.ifBlank { "No email attached" },
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = ForestGreen,
                      )
                      Text(
                        text = "${ev.deviceInfo} • $timeStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = ext.mutedLabel,
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    val canAddInTab = when (selectedTab) {
      0, 1 -> hasManagementAccess
      2, 3 -> isPrimaryAdmin
      else -> false
    }
    if (canAddInTab) {
      FloatingActionButton(
        onClick = {
          when (selectedTab) {
            0 -> {
              editingCourse = null
              showCourseDialog = true
            }
            1 -> {
              editingTeacher = null
              showTeacherDialog = true
            }
            2 -> {
              editingQuote = null
              showQuoteDialog = true
            }
            else -> showAddManagerDialog = true
          }
        },
        containerColor = ForestGreen,
        contentColor = ParchmentCream,
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(20.dp)
          .testTag("admin_fab_add"),
      ) {
        Icon(
          imageVector = if (selectedTab == 3) Icons.Filled.PersonAdd else Icons.Filled.Add,
          contentDescription = "Add item",
        )
      }
    }
  }

  if (showCourseDialog) {
    val existing = editingCourse
    var title by remember(existing) { mutableStateOf(existing?.title ?: "") }
    var arabicTitle by remember(existing) { mutableStateOf(existing?.arabicTitle ?: "") }
    var track by remember(existing) { mutableStateOf(existing?.track ?: "Recitation & Tajweed") }
    var mode by remember(existing) { mutableStateOf(existing?.mode ?: "Online") }
    var level by remember(existing) { mutableStateOf(existing?.level ?: "Beginner") }
    var language by remember(existing) { mutableStateOf(existing?.language ?: "English") }
    var teacherName by remember(existing) { mutableStateOf(existing?.teacherName ?: allReciters.firstOrNull()?.name ?: "Sheikh Ali Habibullah") }
    var country by remember(existing) { mutableStateOf(existing?.country ?: "India") }
    var schedule by remember(existing) { mutableStateOf(existing?.scheduleSummary ?: "Mon, Wed, Fri • 7:30 PM") }
    var seats by remember(existing) { mutableStateOf((existing?.seatsLeft ?: 20).toString()) }
    var description by remember(existing) { mutableStateOf(existing?.description ?: "") }

    AlertDialog(
      onDismissRequest = {
        showCourseDialog = false
        editingCourse = null
      },
      title = { Text(if (existing == null) strings.addCourse else "Edit Course") },
      text = {
        Column(
          modifier = Modifier.verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Course Title") }, singleLine = true)
          OutlinedTextField(value = arabicTitle, onValueChange = { arabicTitle = it }, label = { Text("Arabic Title") }, singleLine = true)
          OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Course Description") }, maxLines = 3)
          OutlinedTextField(value = track, onValueChange = { track = it }, label = { Text("Track") }, singleLine = true)
          Text("Mode", style = MaterialTheme.typography.labelMedium)
          FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Online", "Offline").forEach { m ->
              FilterChip(selected = mode == m, onClick = { mode = m }, label = { Text(m) })
            }
          }
          Text("Level", style = MaterialTheme.typography.labelMedium)
          FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Beginner", "Intermediate", "Expert").forEach { l ->
              FilterChip(selected = level == l, onClick = { level = l }, label = { Text(l) })
            }
          }
          OutlinedTextField(value = language, onValueChange = { language = it }, label = { Text("Language") }, singleLine = true)
          OutlinedTextField(value = teacherName, onValueChange = { teacherName = it }, label = { Text("Teacher Name") }, singleLine = true)
          OutlinedTextField(value = country, onValueChange = { country = it }, label = { Text("Country") }, singleLine = true)
          OutlinedTextField(value = schedule, onValueChange = { schedule = it }, label = { Text("Schedule") }, singleLine = true)
          OutlinedTextField(value = seats, onValueChange = { seats = it.filter { ch -> ch.isDigit() } }, label = { Text("Available Seats") }, singleLine = true)
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onSaveCourse(
              existing?.id,
              title,
              arabicTitle,
              track,
              mode,
              level,
              language,
              teacherName,
              country,
              schedule,
              seats.toIntOrNull() ?: 20,
              description,
            )
            showCourseDialog = false
            editingCourse = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = ParchmentCream),
        ) { Text(strings.save) }
      },
      dismissButton = {
        TextButton(onClick = {
          showCourseDialog = false
          editingCourse = null
        }) { Text(strings.cancel) }
      },
    )
  }

  if (showTeacherDialog) {
    val existing = editingTeacher
    var name by remember(existing) { mutableStateOf(existing?.name ?: "") }
    var arabicName by remember(existing) { mutableStateOf(existing?.arabicName ?: "") }
    var country by remember(existing) { mutableStateOf(existing?.country ?: "India") }
    var specialty by remember(existing) { mutableStateOf(existing?.specialty ?: "Hafs 'an Asim • Tajweed & Hifz") }
    var languagesText by remember(existing) { mutableStateOf(existing?.languages?.joinToString(", ") ?: "Arabic, English") }
    var rating by remember(existing) { mutableStateOf((existing?.rating ?: 4.9).toString()) }
    var availableNow by remember(existing) { mutableStateOf(existing?.availableNow ?: true) }

    AlertDialog(
      onDismissRequest = {
        showTeacherDialog = false
        editingTeacher = null
      },
      title = { Text(if (existing == null) strings.addTeacher else "Edit Teacher") },
      text = {
        Column(
          modifier = Modifier.verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Teacher Full Name") }, singleLine = true)
          OutlinedTextField(value = arabicName, onValueChange = { arabicName = it }, label = { Text("Arabic Name") }, singleLine = true)
          OutlinedTextField(value = country, onValueChange = { country = it }, label = { Text("Country") }, singleLine = true)
          OutlinedTextField(value = specialty, onValueChange = { specialty = it }, label = { Text("Specialty / Ijazah") }, maxLines = 2)
          OutlinedTextField(value = languagesText, onValueChange = { languagesText = it }, label = { Text("Languages (comma separated)") }, singleLine = true)
          OutlinedTextField(value = rating, onValueChange = { rating = it }, label = { Text("Rating (0.0 - 5.0)") }, singleLine = true)
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Available for Quick Call", style = MaterialTheme.typography.bodyMedium)
            Switch(checked = availableNow, onCheckedChange = { availableNow = it })
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onSaveTeacher(
              existing?.id,
              name,
              arabicName,
              country,
              specialty,
              languagesText,
              rating.toDoubleOrNull() ?: 4.9,
              availableNow,
            )
            showTeacherDialog = false
            editingTeacher = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = ParchmentCream),
        ) { Text(strings.save) }
      },
      dismissButton = {
        TextButton(onClick = {
          showTeacherDialog = false
          editingTeacher = null
        }) { Text(strings.cancel) }
      },
    )
  }

  if (showQuoteDialog) {
    val existing = editingQuote
    val todayLabel = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date()) }
    var quoteText by remember(existing) { mutableStateOf(existing?.quoteText ?: "") }
    var arabicText by remember(existing) { mutableStateOf(existing?.arabicText ?: "") }
    var source by remember(existing) { mutableStateOf(existing?.source ?: "AL MAQRA Daily Wisdom") }
    var category by remember(existing) { mutableStateOf(existing?.category?.ifBlank { "Tajweed" } ?: "Tajweed") }
    var dateLabel by remember(existing) { mutableStateOf(existing?.dateLabel?.ifBlank { todayLabel } ?: todayLabel) }

    AlertDialog(
      onDismissRequest = {
        showQuoteDialog = false
        editingQuote = null
      },
      title = { Text(if (existing == null) strings.addQuote else "Edit Daily Quote") },
      text = {
        Column(
          modifier = Modifier.verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          OutlinedTextField(
            value = quoteText,
            onValueChange = { quoteText = it },
            label = { Text("Quote / Reflection Text") },
            maxLines = 4,
          )
          OutlinedTextField(
            value = arabicText,
            onValueChange = { arabicText = it },
            label = { Text("Arabic Verse / Quote (optional)") },
            maxLines = 3,
          )
          OutlinedTextField(
            value = source,
            onValueChange = { source = it },
            label = { Text("Source / Reference") },
            singleLine = true,
          )
          Text("Category", style = MaterialTheme.typography.labelMedium)
          FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Tajweed", "Hifz", "Qur'an Study", "Recitation").forEach { cat ->
              FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text(cat) })
            }
          }
          OutlinedTextField(
            value = dateLabel,
            onValueChange = { dateLabel = it },
            label = { Text("Date Label") },
            singleLine = true,
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (quoteText.isNotBlank()) {
              onSaveQuote(existing?.id, quoteText, arabicText, source, category, dateLabel)
              showQuoteDialog = false
              editingQuote = null
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = ParchmentCream),
        ) { Text(strings.save) }
      },
      dismissButton = {
        TextButton(onClick = {
          showQuoteDialog = false
          editingQuote = null
        }) { Text(strings.cancel) }
      },
    )
  }

  if (showAddManagerDialog) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Academic Manager") }

    AlertDialog(
      onDismissRequest = { showAddManagerDialog = false },
      title = { Text(strings.addManager) },
      text = {
        Column(
          modifier = Modifier.verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Manager Full Name") }, singleLine = true)
          OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Google Account Email") }, singleLine = true)
          OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, singleLine = true)
          Text("Manager Role", style = MaterialTheme.typography.labelMedium)
          FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Academic Manager", "Operations Manager", "Halaqah Supervisor", "Branch Lead").forEach { r ->
              FilterChip(selected = role == r, onClick = { role = r }, label = { Text(r) })
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onAddManager(name, email, phone, role)
            showAddManagerDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = ParchmentCream),
        ) { Text(strings.save) }
      },
      dismissButton = {
        TextButton(onClick = { showAddManagerDialog = false }) { Text(strings.cancel) }
      },
    )
  }
}
