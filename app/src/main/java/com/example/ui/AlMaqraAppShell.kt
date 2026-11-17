package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.auth.signOutUser
import com.example.data.CatalogData
import com.example.data.WHATSAPP_SUPPORT_NUMBER
import com.example.data.model.AdminCourse
import com.example.data.model.AdminTeacher
import com.example.data.model.AppLanguage
import com.example.data.model.CourseCatalogItem
import com.example.data.model.ReciterProfile
import com.example.data.model.ThemeMode
import com.example.ui.components.AlMaqraTopBar
import com.example.ui.components.emailAdminLoginDigest
import com.example.ui.components.openWhatsAppSupport
import com.example.ui.screens.AboutAlMaqraScreen
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.CoursesScreen
import com.example.ui.screens.GroupsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InvitationScreen
import com.example.ui.screens.LiveClassroomScreen
import com.example.ui.screens.MessagesScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ProgressAnalyticsScreen
import com.example.ui.screens.QuranScreen
import com.example.ui.screens.RecitersScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StudyPlansScreen
import com.example.ui.theme.AlMaqraTheme
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.LiveCrimson
import com.example.ui.theme.LocalAlMaqraColors
import com.example.ui.theme.ParchmentCream
import com.example.util.LocalAlMaqraStrings
import com.example.util.layoutDirectionFor
import com.example.util.stringsFor
import kotlinx.coroutines.launch

object Routes {
  const val HOME = "home"
  const val GROUPS = "groups"
  const val COURSES = "courses"
  const val PROFILE = "profile"
  const val PROGRESS = "progress"
  const val QURAN = "quran"
  const val RECITERS = "reciters"
  const val STUDY_PLANS = "study_plans"
  const val MESSAGES = "messages"
  const val CALENDAR = "calendar"
  const val AI_ASSISTANT = "ai_assistant"
  const val ADMIN_PANEL = "admin_panel"
  const val SETTINGS = "settings"
  const val INVITATION = "invitation"
  const val ABOUT = "about"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlMaqraAppShell(
  viewModel: AlMaqraViewModel,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember { CredentialManager.create(context) }

  val profileState by viewModel.userProfileState.collectAsStateWithLifecycle()
  val juzState by viewModel.juzProgressState.collectAsStateWithLifecycle()
  val enrollmentsState by viewModel.enrollmentsState.collectAsStateWithLifecycle()
  val notesState by viewModel.notesState.collectAsStateWithLifecycle()
  val plansState by viewModel.studyPlansState.collectAsStateWithLifecycle()
  val testsState by viewModel.testsState.collectAsStateWithLifecycle()
  val certsState by viewModel.certificatesState.collectAsStateWithLifecycle()
  val messagesState by viewModel.messagesState.collectAsStateWithLifecycle()
  val logsState by viewModel.sessionLogsState.collectAsStateWithLifecycle()
  val adminCoursesState by viewModel.adminCoursesState.collectAsStateWithLifecycle()
  val adminTeachersState by viewModel.adminTeachersState.collectAsStateWithLifecycle()
  val adminManagersState by viewModel.adminManagersState.collectAsStateWithLifecycle()
  val dailyQuotesState by viewModel.dailyQuotesState.collectAsStateWithLifecycle()
  val loginEventsState by viewModel.loginEventsState.collectAsStateWithLifecycle()
  val aiMessages by viewModel.aiMessages.collectAsStateWithLifecycle()
  val aiUiState by viewModel.aiUiState.collectAsStateWithLifecycle()
  val elapsedSeconds by viewModel.elapsedSeconds.collectAsStateWithLifecycle()
  val courseFilter by viewModel.courseFilter.collectAsStateWithLifecycle()
  val activeClassroom by viewModel.activeClassroom.collectAsStateWithLifecycle()

  val profile = (profileState as? UiState.Success)?.data
  val juzList = (juzState as? UiState.Success)?.data.orEmpty()
  val enrollments = (enrollmentsState as? UiState.Success)?.data.orEmpty()
  val notes = (notesState as? UiState.Success)?.data.orEmpty()
  val studyPlans = (plansState as? UiState.Success)?.data.orEmpty()
  val tests = (testsState as? UiState.Success)?.data.orEmpty()
  val certificates = (certsState as? UiState.Success)?.data.orEmpty()
  val messages = (messagesState as? UiState.Success)?.data.orEmpty()
  val sessionLogs = (logsState as? UiState.Success)?.data.orEmpty()
  val adminCourses = (adminCoursesState as? UiState.Success)?.data.orEmpty()
  val adminTeachers = (adminTeachersState as? UiState.Success)?.data.orEmpty()
  val adminManagers = (adminManagersState as? UiState.Success)?.data.orEmpty()
  val dailyQuotes = (dailyQuotesState as? UiState.Success)?.data.orEmpty()
  val loginEvents = (loginEventsState as? UiState.Success)?.data.orEmpty()

  val mergedCourses = remember(adminCourses) {
    val overrideMap = adminCourses.associateBy { it.id }
    val baseStaticIds = CatalogData.courses.map { it.id }.toSet()
    val resolvedBase = CatalogData.courses.map { base ->
      overrideMap[base.id]?.toCatalogItem() ?: base
    }
    val customAdded = adminCourses.filter { it.id !in baseStaticIds }.map { it.toCatalogItem() }
    customAdded + resolvedBase
  }

  val mergedReciters = remember(adminTeachers) {
    val overrideMap = adminTeachers.associateBy { it.id }
    val baseStaticIds = CatalogData.reciters.map { it.id }.toSet()
    val resolvedBase = CatalogData.reciters.map { base ->
      overrideMap[base.id]?.toReciterProfile() ?: base
    }
    val customAdded = adminTeachers.filter { it.id !in baseStaticIds }.map { it.toReciterProfile() }
    customAdded + resolvedBase
  }

  val themeMode = ThemeMode.fromRaw(profile?.themeMode)
  val language = AppLanguage.fromRaw(profile?.preferredLanguage)
  val strings = remember(language) { stringsFor(language) }
  val layoutDir = remember(language) { layoutDirectionFor(language) }

  val snackbarHostState = remember { SnackbarHostState() }
  LaunchedEffect(Unit) {
    viewModel.toastMessages.collect { msg ->
      snackbarHostState.showSnackbar(msg)
    }
  }

  CompositionLocalProvider(
    LocalAlMaqraStrings provides strings,
    LocalLayoutDirection provides layoutDir,
  ) {
    AlMaqraTheme(themeMode = themeMode) {
      val navController = rememberNavController()
      val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
      val backStackEntry by navController.currentBackStackEntryAsState()
      val currentRoute = backStackEntry?.destination?.route ?: Routes.HOME

      var showNotificationsSheet by remember { mutableStateOf(false) }
      var showCallReciterSheet by remember { mutableStateOf(false) }
      var analyticsFilterRange by remember { mutableStateOf("All Time") }
      var showAnalyticsFilterSheet by remember { mutableStateOf(false) }

      val topLevelRoutes = setOf(Routes.HOME, Routes.GROUPS, Routes.COURSES, Routes.PROFILE)
      val isTopLevel = currentRoute in topLevelRoutes

      fun navigateTopLevel(route: String) {
        navController.navigate(route) {
          popUpTo(navController.graph.findStartDestination().id) { saveState = true }
          launchSingleTop = true
          restoreState = true
        }
      }

      fun openDrawerScreen(route: String) {
        scope.launch { drawerState.close() }
        navController.navigate(route) { launchSingleTop = true }
      }

      if (activeClassroom != null) {
        BackHandler { viewModel.closeLiveClassroom() }
        LiveClassroomScreen(
          session = activeClassroom!!,
          elapsedSeconds = elapsedSeconds,
          onCloseWithoutLog = { viewModel.closeLiveClassroom() },
          onFinishAndLog = { mins -> viewModel.completeLiveClassroom(activeClassroom!!, mins) },
          onSaveQuickNote = { t, s, a, c, body -> viewModel.addNote(t, s, a, c, body) },
        )
        return@AlMaqraTheme
      }

      ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
          DrawerContent(
            arabicName = profile?.arabicName ?: "محمد - صالح",
            englishName = profile?.displayName ?: "Student",
            onViewProfile = {
              scope.launch { drawerState.close() }
              navigateTopLevel(Routes.PROFILE)
            },
            onCallReciter = {
              scope.launch { drawerState.close() }
              showCallReciterSheet = true
            },
            onOpenWhatsApp = {
              scope.launch { drawerState.close() }
              openWhatsAppSupport(context)
            },
            onOpenQuran = {
              openDrawerScreen(Routes.QURAN)
            },
            onNavigate = ::openDrawerScreen,
            onLogout = {
              scope.launch {
                drawerState.close()
                signOutUser(credentialManager, scope)
              }
            },
          )
        },
      ) {
        val screenTitle = when (currentRoute) {
          Routes.HOME -> strings.titleHome
          Routes.GROUPS -> strings.titleGroups
          Routes.COURSES -> strings.titleSessions
          Routes.PROFILE -> strings.titleProfile
          Routes.PROGRESS -> strings.titleProgress
          Routes.QURAN -> strings.quranReader
          Routes.RECITERS -> strings.reciters
          Routes.STUDY_PLANS -> strings.studyPlans
          Routes.MESSAGES -> strings.messages
          Routes.CALENDAR -> strings.calendar
          Routes.AI_ASSISTANT -> strings.aiAssistant
          Routes.ADMIN_PANEL -> strings.adminPanel
          Routes.SETTINGS -> strings.settings
          Routes.INVITATION -> strings.invitation
          Routes.ABOUT -> strings.aboutApp
          else -> strings.appName
        }

        Scaffold(
          snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
          topBar = {
            AlMaqraTopBar(
              title = screenTitle,
              showBackButton = !isTopLevel,
              onMenuOrBackClick = {
                if (isTopLevel) {
                  scope.launch { drawerState.open() }
                } else {
                  navController.popBackStack()
                }
              },
              showFilterAction = currentRoute == Routes.PROGRESS,
              filterLabel = strings.filter,
              unreadCount = if (viewModel.isPrimaryAdmin) loginEvents.count { it.status == "unread" } else 0,
              onActionClick = {
                if (currentRoute == Routes.PROGRESS) {
                  showAnalyticsFilterSheet = true
                } else {
                  showNotificationsSheet = true
                }
              },
            )
          },
          bottomBar = {
            if (isTopLevel) {
              val ext = LocalAlMaqraColors.current
              NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
              ) {
                val navItems = listOf(
                  Triple(Routes.HOME, strings.navHome, Pair(Icons.Filled.Home, Icons.Outlined.Home)),
                  Triple(Routes.GROUPS, strings.navGroups, Pair(Icons.Filled.Groups, Icons.Outlined.Groups)),
                  Triple(Routes.COURSES, strings.navCourses, Pair(Icons.Filled.MenuBook, Icons.Outlined.MenuBook)),
                  Triple(Routes.PROFILE, strings.navProfile, Pair(Icons.Filled.Person, Icons.Outlined.Person)),
                )
                navItems.forEach { (route, label, icons) ->
                  val selected = currentRoute == route
                  NavigationBarItem(
                    selected = selected,
                    onClick = { navigateTopLevel(route) },
                    icon = {
                      Icon(
                        imageVector = if (selected) icons.first else icons.second,
                        contentDescription = label,
                      )
                    },
                    label = { Text(label, maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                      selectedIconColor = MaterialTheme.colorScheme.primary,
                      selectedTextColor = MaterialTheme.colorScheme.primary,
                      indicatorColor = if (ext.isDark) ForestGreen.copy(alpha = 0.55f) else ParchmentCream,
                      unselectedIconColor = ext.mutedLabel,
                      unselectedTextColor = ext.mutedLabel,
                    ),
                    modifier = Modifier.testTag("bottom_nav_$route"),
                  )
                }
              }
            }
          },
        ) { innerPadding ->
          NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding),
          ) {
            composable(Routes.HOME) {
              HomeScreen(
                profile = profile,
                dailyQuotes = dailyQuotes,
                onSubmitFeedback = { r, c -> viewModel.submitFeedback(r, c) },
                onNavigateToCourses = { navigateTopLevel(Routes.COURSES) },
                onOpenQuran = { openDrawerScreen(Routes.QURAN) },
                onOpenAiAssistant = { openDrawerScreen(Routes.AI_ASSISTANT) },
                onOpenWhatsApp = { openWhatsAppSupport(context) },
              )
            }
            composable(Routes.GROUPS) {
              GroupsScreen(
                enrollments = enrollments,
                onJoinGroup = { group -> viewModel.joinGroup(group) },
                onLeaveGroup = { id, title -> viewModel.leaveEnrollment(id, title) },
              )
            }
            composable(Routes.COURSES) {
              CoursesScreen(
                enrollments = enrollments,
                filterState = courseFilter,
                allCourses = mergedCourses,
                onSetMode = { viewModel.setCourseMode(it) },
                onApplyFilter = { l, lvl, t -> viewModel.applyCourseFilter(l, lvl, t) },
                onClearFilter = { viewModel.clearCourseFilter() },
                onRegisterCourse = { course, feePlan -> viewModel.registerCourse(course, feePlan) },
                onDropCourse = { id, title -> viewModel.leaveEnrollment(id, title) },
              )
            }
            composable(Routes.PROFILE) {
              ProfileScreen(
                profile = profile,
                juzList = juzList,
                enrollments = enrollments,
                tests = tests,
                certificates = certificates,
                notes = notes,
                onUpdatePersonalInfo = viewModel::updatePersonalInfo,
                onUpdateJuzStatus = viewModel::updateJuzStatus,
                onNavigateToCourses = { navigateTopLevel(Routes.COURSES) },
                onNavigateToGroups = { navigateTopLevel(Routes.GROUPS) },
                onNavigateToCalendar = { openDrawerScreen(Routes.CALENDAR) },
                onScheduleTest = viewModel::scheduleOralTest,
                onAddNote = { t, s, a, c, body -> viewModel.addNote(t, s, a, c, body) },
                onDeleteNote = viewModel::deleteNote,
              )
            }
            composable(Routes.PROGRESS) {
              ProgressAnalyticsScreen(
                profile = profile,
                sessionLogs = sessionLogs,
                filterRange = analyticsFilterRange,
                onSelectFilterRange = { analyticsFilterRange = it },
              )
            }
            composable(Routes.QURAN) {
              QuranScreen()
            }
            composable(Routes.RECITERS) {
              RecitersScreen(
                reciters = mergedReciters,
                onCallReciter = { rec ->
                  viewModel.openQuickCallReciter(rec.name, rec.languages.firstOrNull() ?: "Arabic")
                },
                onMessageReciter = { openDrawerScreen(Routes.MESSAGES) },
              )
            }
            composable(Routes.STUDY_PLANS) {
              StudyPlansScreen(
                plans = studyPlans,
                onAddPlan = viewModel::addStudyPlan,
                onTogglePlan = viewModel::toggleStudyPlan,
              )
            }
            composable(Routes.MESSAGES) {
              MessagesScreen(
                messages = messages,
                onSendMessage = viewModel::sendMessage,
              )
            }
            composable(Routes.CALENDAR) {
              CalendarScreen(enrollments = enrollments)
            }
            composable(Routes.AI_ASSISTANT) {
              AiAssistantScreen(
                messages = aiMessages,
                uiState = aiUiState,
                onSendPrompt = viewModel::sendAiPrompt,
                onClearChat = viewModel::clearAiChat,
                onDismissError = viewModel::dismissAiError,
              )
            }
            composable(Routes.ADMIN_PANEL) {
              AdminPanelScreen(
                currentUserEmail = viewModel.currentUserEmail,
                isPrimaryAdmin = viewModel.isPrimaryAdmin,
                allCourses = mergedCourses,
                allReciters = mergedReciters,
                adminCourses = adminCourses,
                adminTeachers = adminTeachers,
                adminManagers = adminManagers,
                dailyQuotes = dailyQuotes,
                loginEvents = loginEvents,
                onSaveCourse = viewModel::saveAdminCourse,
                onDeleteCourse = viewModel::deleteAdminCourse,
                onSaveTeacher = viewModel::saveAdminTeacher,
                onToggleTeacherAvailability = viewModel::toggleAdminTeacherAvailability,
                onDeleteTeacher = viewModel::deleteAdminTeacher,
                onAddManager = viewModel::addAdminManager,
                onToggleManagerStatus = viewModel::toggleAdminManagerStatus,
                onDeleteManager = viewModel::deleteAdminManager,
                onSaveQuote = viewModel::saveDailyQuote,
                onDeleteQuote = viewModel::deleteDailyQuote,
                onMarkLoginRead = viewModel::markLoginEventRead,
                onMarkAllLoginsRead = viewModel::markAllLoginsRead,
              )
            }
            composable(Routes.SETTINGS) {
              SettingsScreen(
                currentTheme = themeMode,
                currentLanguage = language,
                onSelectTheme = viewModel::updateThemeMode,
                onSelectLanguage = viewModel::updateLanguage,
              )
            }
            composable(Routes.INVITATION) {
              InvitationScreen(userName = profile?.displayName ?: "Student")
            }
            composable(Routes.ABOUT) {
              AboutAlMaqraScreen()
            }
          }
        }
      }

      if (showNotificationsSheet) {
        ModalBottomSheet(onDismissRequest = { showNotificationsSheet = false }) {
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            item {
              Text("Notifications", style = MaterialTheme.typography.titleLarge)
            }
            if (viewModel.isPrimaryAdmin) {
              item {
                val unreadCount = loginEvents.count { it.status == "unread" }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                  Text(
                    text = "Admin Login Notifications ($unreadCount unread)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = ForestGreen,
                  )
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                  ) {
                    OutlinedButton(
                      onClick = { viewModel.markAllLoginsRead(loginEvents) },
                      enabled = unreadCount > 0,
                      shape = RoundedCornerShape(10.dp),
                      modifier = Modifier.weight(1f),
                    ) {
                      Icon(Icons.Filled.DoneAll, null, modifier = Modifier.size(16.dp))
                      Spacer(Modifier.width(6.dp))
                      Text("Mark Read")
                    }
                    Button(
                      onClick = { emailAdminLoginDigest(context, loginEvents) },
                      shape = RoundedCornerShape(10.dp),
                      colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = ParchmentCream),
                      modifier = Modifier.weight(1f),
                    ) {
                      Icon(Icons.Filled.Email, null, modifier = Modifier.size(16.dp))
                      Spacer(Modifier.width(6.dp))
                      Text("Email Report")
                    }
                  }
                }
              }
              items(loginEvents.take(10), key = { "login_${it.id}" }) { ev ->
                val isUnread = ev.status == "unread"
                Surface(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable {
                      if (isUnread) viewModel.markLoginEventRead(ev.id)
                    },
                  shape = RoundedCornerShape(14.dp),
                  color = if (isUnread) LocalAlMaqraColors.current.cardBannerBg else MaterialTheme.colorScheme.surfaceVariant,
                  border = if (isUnread) BorderStroke(1.dp, ForestGreen) else null,
                ) {
                  Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Icon(Icons.Filled.Login, null, tint = ForestGreen, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                      ) {
                        Text(ev.userName.ifBlank { "Student" }, style = MaterialTheme.typography.titleSmall)
                        Text(
                          text = if (isUnread) "NEW" else "Read",
                          style = MaterialTheme.typography.labelSmall,
                          color = if (isUnread) LiveCrimson else LocalAlMaqraColors.current.mutedLabel,
                        )
                      }
                      Text(ev.userEmail, style = MaterialTheme.typography.bodySmall, color = ForestGreen)
                      Text(ev.deviceInfo, style = MaterialTheme.typography.labelSmall, color = LocalAlMaqraColors.current.mutedLabel)
                    }
                  }
                }
              }
              item { HorizontalDivider() }
            }
            items(CatalogData.notifications, key = { it.id }) { notif ->
              Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
              ) {
                Column(modifier = Modifier.padding(14.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                  ) {
                    Text(notif.title, style = MaterialTheme.typography.titleSmall)
                    Text(notif.timeAgo, style = MaterialTheme.typography.labelSmall)
                  }
                  Spacer(Modifier.height(4.dp))
                  Text(notif.body, style = MaterialTheme.typography.bodySmall)
                }
              }
            }
          }
        }
      }

      if (showCallReciterSheet) {
        ModalBottomSheet(onDismissRequest = { showCallReciterSheet = false }) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Text(strings.callReciter, style = MaterialTheme.typography.titleLarge)
            Text("Select an available certified Sheikh for an instant recitation session:", style = MaterialTheme.typography.bodySmall)
            mergedReciters.filter { it.availableNow }.forEach { rec ->
              Surface(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(14.dp))
                  .clickable {
                    showCallReciterSheet = false
                    viewModel.openQuickCallReciter(rec.name, rec.languages.firstOrNull() ?: "Arabic")
                  },
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
              ) {
                Row(
                  modifier = Modifier.padding(14.dp),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Icon(Icons.Filled.Call, null, tint = ForestGreen)
                  Spacer(Modifier.width(12.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(rec.arabicName, fontFamily = AmiriFontFamily, style = MaterialTheme.typography.titleSmall)
                    Text("${rec.name} • ${rec.languages.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
                  }
                }
              }
            }
            Spacer(Modifier.height(16.dp))
          }
        }
      }



      if (showAnalyticsFilterSheet) {
        ModalBottomSheet(onDismissRequest = { showAnalyticsFilterSheet = false }) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Text("Filter Analytics", style = MaterialTheme.typography.titleLarge)
            listOf("All Time", "30 Days", "7 Days").forEach { range ->
              Surface(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .clickable {
                    analyticsFilterRange = range
                    showAnalyticsFilterSheet = false
                  },
                color = if (analyticsFilterRange == range) ForestGreen else MaterialTheme.colorScheme.surfaceVariant,
              ) {
                Text(
                  text = range,
                  color = if (analyticsFilterRange == range) ParchmentCream else MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.padding(14.dp),
                )
              }
            }
            Spacer(Modifier.height(12.dp))
          }
        }
      }
    }
  }
}

@Composable
private fun DrawerContent(
  arabicName: String,
  englishName: String,
  onViewProfile: () -> Unit,
  onCallReciter: () -> Unit,
  onOpenWhatsApp: () -> Unit,
  onOpenQuran: () -> Unit,
  onNavigate: (String) -> Unit,
  onLogout: () -> Unit,
) {
  val strings = LocalAlMaqraStrings.current
  val ext = LocalAlMaqraColors.current

  ModalDrawerSheet(
    modifier = Modifier.width(304.dp),
    drawerContainerColor = MaterialTheme.colorScheme.background,
  ) {
    // Solid Forest Green Header
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(210.dp)
        .background(ext.headerBackground),
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .statusBarsPadding()
          .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
      ) {
        Surface(
          shape = CircleShape,
          border = BorderStroke(2.dp, ParchmentCream),
          modifier = Modifier.size(76.dp),
        ) {
          Image(
            painter = painterResource(id = R.drawable.al_maqra_logo),
            contentDescription = "User avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
          )
        }
        Spacer(Modifier.height(10.dp))
        Text(
          text = arabicName.ifBlank { englishName },
          fontFamily = AmiriFontFamily,
          style = MaterialTheme.typography.titleLarge,
          color = ParchmentCream,
        )
        Text(
          text = strings.viewProfile,
          style = MaterialTheme.typography.titleSmall,
          color = ParchmentCream.copy(alpha = 0.9f),
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onViewProfile)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        )
      }
    }

    LazyColumn(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .navigationBarsPadding(),
      contentPadding = PaddingValues(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      item {
        Text(
          text = strings.drawerLearning,
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onBackground,
          modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
        )
      }
      item {
        DrawerRowCard(
          icon = Icons.Filled.Call,
          label = strings.callReciter,
          showArrow = false,
          onClick = onCallReciter,
        )
      }
      item {
        DrawerRowCard(
          icon = Icons.Filled.AutoAwesome,
          label = strings.aiAssistant,
          onClick = { onNavigate(Routes.AI_ASSISTANT) },
        )
      }
      item {
        DrawerRowCard(
          icon = Icons.Filled.Chat,
          label = "${strings.whatsappSupport} • $WHATSAPP_SUPPORT_NUMBER",
          showArrow = false,
          onClick = onOpenWhatsApp,
        )
      }
      item { HorizontalDivider(color = ext.cardBorder, modifier = Modifier.padding(vertical = 2.dp)) }
      item {
        DrawerRowCard(
          icon = Icons.Filled.MenuBook,
          label = strings.quranReader,
          badgeText = strings.comingSoon,
          onClick = onOpenQuran,
        )
      }
      item {
        DrawerRowCard(
          icon = Icons.Filled.School,
          label = strings.reciters,
          onClick = { onNavigate(Routes.RECITERS) },
        )
      }
      item {
        DrawerRowCard(
          icon = Icons.Filled.Folder,
          label = strings.studyPlans,
          onClick = { onNavigate(Routes.STUDY_PLANS) },
        )
      }
      item {
        DrawerRowCard(
          icon = Icons.AutoMirrored.Outlined.Message,
          label = strings.messages,
          onClick = { onNavigate(Routes.MESSAGES) },
        )
      }
      item {
        DrawerRowCard(
          icon = Icons.Filled.Star,
          label = strings.progressAnalytics,
          onClick = { onNavigate(Routes.PROGRESS) },
        )
      }
      item {
        DrawerRowCard(
          icon = Icons.Filled.CalendarMonth,
          label = strings.calendar,
          onClick = { onNavigate(Routes.CALENDAR) },
        )
      }
      item { HorizontalDivider(color = ext.cardBorder, modifier = Modifier.padding(vertical = 2.dp)) }
      item {
        Text(
          text = strings.drawerApp,
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onBackground,
          modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
        )
      }
      item {
        DrawerRowCard(
          icon = Icons.Filled.AdminPanelSettings,
          label = strings.adminPanel,
          onClick = { onNavigate(Routes.ADMIN_PANEL) },
        )
      }
      item {
        DrawerRowCard(
          icon = Icons.Filled.Settings,
          label = strings.settings,
          onClick = { onNavigate(Routes.SETTINGS) },
        )
      }
      item {
        DrawerRowCard(
          icon = Icons.Filled.PersonAdd,
          label = strings.invitation,
          onClick = { onNavigate(Routes.INVITATION) },
        )
      }
      item {
        DrawerRowCard(
          icon = Icons.Filled.Info,
          label = strings.aboutApp,
          onClick = { onNavigate(Routes.ABOUT) },
        )
      }
      item { HorizontalDivider(color = ext.cardBorder, modifier = Modifier.padding(vertical = 2.dp)) }
      item {
        DrawerRowCard(
          icon = Icons.AutoMirrored.Filled.ExitToApp,
          label = strings.logout,
          isDestructive = true,
          onClick = onLogout,
        )
      }
    }
  }
}

@Composable
private fun DrawerRowCard(
  icon: ImageVector,
  label: String,
  badgeText: String? = null,
  showArrow: Boolean = true,
  isDestructive: Boolean = false,
  onClick: () -> Unit,
) {
  val ext = LocalAlMaqraColors.current
  val tint = if (isDestructive) LiveCrimson else MaterialTheme.colorScheme.primary
  val textColor = if (isDestructive) LiveCrimson else MaterialTheme.colorScheme.onSurface

  Box(modifier = Modifier.fillMaxWidth()) {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = if (badgeText != null) 8.dp else 0.dp)
        .clip(RoundedCornerShape(12.dp))
        .clickable(onClick = onClick),
      shape = RoundedCornerShape(12.dp),
      color = MaterialTheme.colorScheme.surface,
      border = BorderStroke(1.dp, ext.cardBorder),
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(
          text = label,
          style = MaterialTheme.typography.titleSmall,
          color = textColor,
          modifier = Modifier.weight(1f),
        )
        if (showArrow) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = ext.mutedLabel,
          )
        }
      }
    }

    if (badgeText != null) {
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = ForestGreen,
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(end = 10.dp),
      ) {
        Text(
          text = badgeText,
          style = MaterialTheme.typography.labelSmall,
          color = ParchmentCream,
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
        )
      }
    }
  }
}

private fun AdminCourse.toCatalogItem(): CourseCatalogItem =
  CourseCatalogItem(
    id = id,
    title = title,
    arabicTitle = arabicTitle,
    track = track,
    mode = mode,
    level = level,
    language = language,
    teacherName = teacherName,
    teacherInitial = teacherName.firstOrNull()?.uppercase() ?: "S",
    country = country,
    scheduleSummary = scheduleSummary,
    seatsLeft = seatsLeft,
    description = description,
  )

private fun AdminTeacher.toReciterProfile(): ReciterProfile =
  ReciterProfile(
    id = id,
    name = name,
    arabicName = arabicName,
    country = country,
    specialty = specialty,
    languages = languagesText.split(",").map { it.trim() }.filter { it.isNotEmpty() }.ifEmpty { listOf("Arabic") },
    rating = rating,
    availableNow = availableStatus == "available",
  )
