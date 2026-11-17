package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AlMaqraRepository
import com.example.data.BOOTSTRAPPED_ADMIN_EMAIL
import com.example.data.CatalogData
import com.example.data.model.AdminCourse
import com.example.data.model.AdminManager
import com.example.data.model.AdminTeacher
import com.example.data.model.AiChatMessage
import com.example.data.model.AppLanguage
import com.example.data.model.Certificate
import com.example.data.model.CourseCatalogItem
import com.example.data.model.DailyQuote
import com.example.data.model.DirectMessage
import com.example.data.model.Enrollment
import com.example.data.model.GroupCatalogItem
import com.example.data.model.JuzProgress
import com.example.data.model.JuzStatus
import com.example.data.model.LiveSessionItem
import com.example.data.model.LoginEvent
import com.example.data.model.OralTest
import com.example.data.model.ReciterProfile
import com.example.data.model.SessionLog
import com.example.data.model.StudentNote
import com.example.data.model.StudyPlan
import com.example.data.model.ThemeMode
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

private const val AI_TAG = "AlMaqraAiAssistant"

sealed interface UiState<out T> {
  data object Loading : UiState<Nothing>
  data class Success<T>(val data: T) : UiState<T>
  data class Error(val message: String) : UiState<Nothing>
}

sealed interface AiUiState {
  data object Idle : AiUiState
  data object Loading : AiUiState
  data class Streaming(val text: String) : AiUiState
  data class Success(val text: String) : AiUiState
  data class Error(val message: String) : AiUiState
}

data class CourseFilterState(
  val mode: String = "Online",
  val language: String? = null,
  val level: String? = null,
  val track: String? = null,
)

class AlMaqraViewModel(
  private val repository: AlMaqraRepository,
  val currentUserId: String,
  initialDisplayName: String?,
  val currentUserEmail: String?,
) : ViewModel() {

  private val _toastMessages = MutableSharedFlow<String>(extraBufferCapacity = 4)
  val toastMessages: SharedFlow<String> = _toastMessages.asSharedFlow()

  private val _elapsedSeconds = MutableStateFlow(0L)
  val elapsedSeconds: StateFlow<Long> = _elapsedSeconds.asStateFlow()

  private val _courseFilter = MutableStateFlow(CourseFilterState())
  val courseFilter: StateFlow<CourseFilterState> = _courseFilter.asStateFlow()

  private val _activeClassroom = MutableStateFlow<LiveSessionItem?>(null)
  val activeClassroom: StateFlow<LiveSessionItem?> = _activeClassroom.asStateFlow()

  val isPrimaryAdmin: Boolean =
    currentUserEmail?.trim()?.lowercase() == BOOTSTRAPPED_ADMIN_EMAIL

  val userProfileState: StateFlow<UiState<UserProfile?>> =
    repository.observeUserProfile().asUiState("Failed to load student profile")

  val juzProgressState: StateFlow<UiState<List<JuzProgress>>> =
    repository.observeJuzProgress().asUiState("Failed to load Juz progress")

  val enrollmentsState: StateFlow<UiState<List<Enrollment>>> =
    repository.observeEnrollments().asUiState("Failed to load enrollments")

  val notesState: StateFlow<UiState<List<StudentNote>>> =
    repository.observeNotes().asUiState("Failed to load notes")

  val studyPlansState: StateFlow<UiState<List<StudyPlan>>> =
    repository.observeStudyPlans().asUiState("Failed to load study plans")

  val testsState: StateFlow<UiState<List<OralTest>>> =
    repository.observeTests().asUiState("Failed to load oral tests")

  val certificatesState: StateFlow<UiState<List<Certificate>>> =
    repository.observeCertificates().asUiState("Failed to load certificates")

  val messagesState: StateFlow<UiState<List<DirectMessage>>> =
    repository.observeMessages().asUiState("Failed to load messages")

  val sessionLogsState: StateFlow<UiState<List<SessionLog>>> =
    repository.observeSessionLogs().asUiState("Failed to load session history")

  val adminCoursesState: StateFlow<UiState<List<AdminCourse>>> =
    repository.observeAdminCourses().asUiState("Failed to load admin courses")

  val adminTeachersState: StateFlow<UiState<List<AdminTeacher>>> =
    repository.observeAdminTeachers().asUiState("Failed to load admin teachers")

  val adminManagersState: StateFlow<UiState<List<AdminManager>>> =
    repository.observeAdminManagers().asUiState("Failed to load admin managers")

  val dailyQuotesState: StateFlow<UiState<List<DailyQuote>>> =
    repository.observeDailyQuotes().asUiState("Failed to load daily quotes")

  val loginEventsState: StateFlow<UiState<List<LoginEvent>>> =
    repository.observeLoginEvents().asUiState("Failed to load login events")

  // Firebase AI Logic Assistant (lazy initialization per SDK guidelines)
  private val aiModel by lazy {
    Firebase.ai.generativeModel(
      modelName = "gemini-3.8-flash",
      systemInstruction = content {
        text(
          "You are AL MAQRA AI Study Assistant, a respectful, knowledgeable Quranic education, Tajweed, and Hifz mentor. " +
            "Help students with Tajweed rules (Makharij, Madd, Idgham, Ikhfa, Qalqalah), personalized Hifz memorization and Muraja'ah plans, " +
            "Surah overviews, and study discipline. Reply clearly in the user's language (English, Arabic, or Urdu) with accurate, encouraging guidance.",
        )
      },
    )
  }

  private val _aiMessages = MutableStateFlow<List<AiChatMessage>>(emptyList())
  val aiMessages: StateFlow<List<AiChatMessage>> = _aiMessages.asStateFlow()

  private val _aiUiState = MutableStateFlow<AiUiState>(AiUiState.Idle)
  val aiUiState: StateFlow<AiUiState> = _aiUiState.asStateFlow()

  init {
    viewModelScope.launch {
      repository.ensureUserInitialized(initialDisplayName, currentUserEmail)
        .onFailure { err ->
          _toastMessages.tryEmit(err.localizedMessage ?: "Cloud initialization failed")
        }
      repository.recordUserLogin(initialDisplayName, currentUserEmail)
    }
    viewModelScope.launch {
      while (isActive) {
        delay(1000L)
        _elapsedSeconds.value += 1L
      }
    }
  }

  private fun <T> Flow<T>.asUiState(fallbackError: String): StateFlow<UiState<T>> {
    return this
      .map<T, UiState<T>> { UiState.Success(it) }
      .catch { error ->
        val firestoreError = generateSequence(error) { it.cause }
          .filterIsInstance<FirebaseFirestoreException>()
          .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val rootError = firestoreError ?: error
        emit(UiState.Error(rootError.message ?: fallbackError))
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = UiState.Loading,
      )
  }

  fun sendAiPrompt(prompt: String) {
    val trimmed = prompt.trim()
    if (trimmed.isBlank()) return
    if (_aiUiState.value is AiUiState.Loading || _aiUiState.value is AiUiState.Streaming) return

    val previousHistory = _aiMessages.value
      .filter { !it.isStreaming && it.text.isNotBlank() }
      .map { msg ->
        content(role = if (msg.isFromUser) "user" else "model") {
          text(msg.text)
        }
      }

    val userMsgId = "usr_${System.currentTimeMillis()}"
    val modelMsgId = "ai_${System.currentTimeMillis() + 1}"

    _aiMessages.value = _aiMessages.value +
      AiChatMessage(id = userMsgId, isFromUser = true, text = trimmed) +
      AiChatMessage(id = modelMsgId, isFromUser = false, text = "", isStreaming = true)

    _aiUiState.value = AiUiState.Loading

    viewModelScope.launch {
      val accumulated = StringBuilder()
      try {
        withTimeout(60_000L) {
          val chat = aiModel.startChat(history = previousHistory)
          chat.sendMessageStream(trimmed).collect { chunk ->
            val chunkText = chunk.text.orEmpty()
            if (chunkText.isNotEmpty()) {
              accumulated.append(chunkText)
              val currentText = accumulated.toString()
              _aiUiState.value = AiUiState.Streaming(currentText)
              _aiMessages.value = _aiMessages.value.map { m ->
                if (m.id == modelMsgId) m.copy(text = currentText, isStreaming = true) else m
              }
            }
          }
        }
        val finalText = accumulated.toString().trim()
        if (finalText.isNotBlank()) {
          _aiUiState.value = AiUiState.Success(finalText)
          _aiMessages.value = _aiMessages.value.map { m ->
            if (m.id == modelMsgId) m.copy(text = finalText, isStreaming = false) else m
          }
        } else {
          _aiMessages.value = _aiMessages.value.filterNot { it.id == modelMsgId }
          _aiUiState.value = AiUiState.Error("The AI assistant returned an empty response.")
        }
      } catch (e: TimeoutCancellationException) {
        Log.e(AI_TAG, "AI generation timed out", e)
        _aiMessages.value = _aiMessages.value.filterNot { it.id == modelMsgId && it.text.isBlank() }
          .map { if (it.id == modelMsgId) it.copy(isStreaming = false) else it }
        _aiUiState.value = AiUiState.Error("Request timed out after 60 seconds. Please try again.")
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        Log.e(AI_TAG, "Firebase AI generation failed", e)
        _aiMessages.value = _aiMessages.value.filterNot { it.id == modelMsgId && it.text.isBlank() }
          .map { if (it.id == modelMsgId) it.copy(isStreaming = false) else it }
        _aiUiState.value = AiUiState.Error(e.localizedMessage ?: "Failed to reach AI Assistant.")
      }
    }
  }

  fun clearAiChat() {
    _aiMessages.value = emptyList()
    _aiUiState.value = AiUiState.Idle
  }

  fun dismissAiError() {
    if (_aiUiState.value is AiUiState.Error) {
      _aiUiState.value = AiUiState.Idle
    }
  }

  fun setCourseMode(mode: String) {
    _courseFilter.value = _courseFilter.value.copy(mode = mode)
  }

  fun applyCourseFilter(language: String?, level: String?, track: String?) {
    _courseFilter.value = _courseFilter.value.copy(
      language = language,
      level = level,
      track = track,
    )
  }

  fun clearCourseFilter() {
    _courseFilter.value = CourseFilterState(mode = _courseFilter.value.mode)
  }

  fun openLiveClassroom(session: LiveSessionItem) {
    _activeClassroom.value = session
  }

  fun openQuickCallReciter(reciterName: String, language: String) {
    _activeClassroom.value = LiveSessionItem(
      id = "call_${reciterName.hashCode()}",
      track = "One-to-One Recitation Call",
      arabicTitle = "اتصال مباشر - $reciterName",
      teacherName = reciterName,
      teacherInitial = reciterName.firstOrNull()?.uppercase() ?: "S",
      country = "Saudi Arabia",
      mode = "Online",
      level = "Intermediate",
      language = language,
      studentCount = 1,
      remainingSeconds = 1800L,
      timeSlot = "Live Audio Call",
      ongoing = true,
    )
  }

  fun closeLiveClassroom() {
    _activeClassroom.value = null
  }

  fun completeLiveClassroom(session: LiveSessionItem, minutesSpent: Int) {
    _activeClassroom.value = null
    viewModelScope.launch {
      val category = if (session.id.startsWith("call_")) "one_to_one" else "guest"
      repository.logLiveSessionAttendance(
        courseTitle = session.track,
        arabicTitle = session.arabicTitle,
        teacherName = session.teacherName,
        teacherCountry = session.country,
        level = session.level,
        sessionCategory = category,
        durationMinutes = minutesSpent.coerceAtLeast(1),
      ).onSuccess {
        _toastMessages.tryEmit("Session logged! +${minutesSpent.coerceAtLeast(1) * 50} deeds earned.")
      }.onFailure {
        _toastMessages.tryEmit(it.localizedMessage ?: "Failed to log session")
      }
    }
  }

  fun updateThemeMode(themeMode: ThemeMode) {
    viewModelScope.launch {
      repository.updateUserSettings(themeMode = themeMode)
        .onFailure { _toastMessages.tryEmit(it.localizedMessage ?: "Failed to update theme") }
    }
  }

  fun updateLanguage(language: AppLanguage) {
    viewModelScope.launch {
      repository.updateUserSettings(language = language)
        .onFailure { _toastMessages.tryEmit(it.localizedMessage ?: "Failed to update language") }
    }
  }

  fun updatePersonalInfo(
    displayName: String,
    arabicName: String,
    country: String,
    phone: String,
    jobTitle: String,
    dateOfBirth: String,
    gender: String,
  ) {
    viewModelScope.launch {
      repository.updatePersonalInfo(displayName, arabicName, country, phone, jobTitle, dateOfBirth, gender)
        .onSuccess { _toastMessages.tryEmit("Profile updated") }
        .onFailure { _toastMessages.tryEmit(it.localizedMessage ?: "Failed to update profile") }
    }
  }

  fun updateJuzStatus(juzNumber: Int, status: JuzStatus) {
    val meta = CatalogData.juzMetadata.firstOrNull { it.number == juzNumber } ?: return
    val ayahs = when (status) {
      JuzStatus.COMPLETED -> meta.totalAyahs
      JuzStatus.IN_PROGRESS -> meta.totalAyahs / 2
      JuzStatus.INCOMPLETE -> 0
    }
    viewModelScope.launch {
      repository.updateJuzStatus(juzNumber, status, ayahs, meta.totalAyahs)
        .onSuccess { _toastMessages.tryEmit("Juz $juzNumber marked ${status.raw.replace('_', ' ')}") }
        .onFailure { _toastMessages.tryEmit(it.localizedMessage ?: "Failed to update Juz") }
    }
  }

  fun joinGroup(group: GroupCatalogItem) {
    viewModelScope.launch {
      repository.joinGroup(group)
        .onSuccess { _toastMessages.tryEmit("Joined ${group.englishTitle}") }
        .onFailure { _toastMessages.tryEmit(it.localizedMessage ?: "Failed to join group") }
    }
  }

  fun leaveEnrollment(enrollmentId: String, title: String) {
    viewModelScope.launch {
      repository.removeEnrollment(enrollmentId)
        .onSuccess { _toastMessages.tryEmit("Removed $title") }
        .onFailure { _toastMessages.tryEmit(it.localizedMessage ?: "Failed to update enrollment") }
    }
  }

  fun registerCourse(course: CourseCatalogItem, feePlan: String = "Free Trial Class") {
    viewModelScope.launch {
      repository.registerCourse(course, feePlan)
        .onSuccess { _toastMessages.tryEmit("Enrolled in ${course.title} ($feePlan). Teacher assessment will be scheduled.") }
        .onFailure { _toastMessages.tryEmit(it.localizedMessage ?: "Failed to enroll") }
    }
  }

  fun addNote(
    title: String,
    surahRef: String,
    ayahRange: String,
    category: String,
    content: String,
    teacherName: String = "",
  ) {
    viewModelScope.launch {
      repository.addNote(title, surahRef, ayahRange, category, content, teacherName)
        .onSuccess { _toastMessages.tryEmit("Note saved") }
        .onFailure { _toastMessages.tryEmit(it.localizedMessage ?: "Failed to save note") }
    }
  }

  fun deleteNote(noteId: String) {
    viewModelScope.launch {
      repository.deleteNote(noteId)
        .onFailure { _toastMessages.tryEmit(it.localizedMessage ?: "Failed to delete note") }
    }
  }

  fun addStudyPlan(
    title: String,
    dailyTargetPages: Int,
    scheduleDays: String,
    preferredTime: String,
  ) {
    viewModelScope.launch {
      repository.addStudyPlan(title, dailyTargetPages, scheduleDays, preferredTime)
        .onSuccess { _toastMessages.tryEmit("Study plan created") }
        .onFailure { _toastMessages.tryEmit(it.localizedMessage ?: "Failed to save study plan") }
    }
  }

  fun toggleStudyPlan(plan: StudyPlan) {
    viewModelScope.launch {
      repository.toggleStudyPlanStatus(plan)
        .onFailure { _toastMessages.tryEmit(it.localizedMessage ?: "Failed to update study plan") }
    }
  }

  fun scheduleOralTest(
    title: String,
    juzRange: String,
    sheikhName: String,
    scheduledDate: String,
  ) {
    viewModelScope.launch {
      repository.scheduleOralTest(title, juzRange, sheikhName, scheduledDate)
        .onSuccess { _toastMessages.tryEmit("Oral test scheduled") }
        .onFailure { _toastMessages.tryEmit(it.localizedMessage ?: "Failed to schedule test") }
    }
  }

  fun sendMessage(reciterId: String, reciterName: String, body: String) {
    if (body.isBlank()) return
    viewModelScope.launch {
      repository.sendMessage(reciterId, reciterName, body)
        .onFailure { _toastMessages.tryEmit(it.localizedMessage ?: "Failed to send message") }
    }
  }

  fun submitFeedback(rating: Int, comment: String) {
    viewModelScope.launch {
      repository.submitFeedback(rating, comment)
        .onSuccess { _toastMessages.tryEmit("JazakAllahu Khayran! Feedback submitted.") }
        .onFailure { _toastMessages.tryEmit(it.localizedMessage ?: "Failed to submit feedback") }
    }
  }

  // Admin Panel Actions
  fun saveAdminCourse(
    courseId: String?,
    title: String,
    arabicTitle: String,
    track: String,
    mode: String,
    level: String,
    language: String,
    teacherName: String,
    country: String,
    scheduleSummary: String,
    seatsLeft: Int,
    description: String = "",
  ) {
    viewModelScope.launch {
      repository.saveAdminCourse(
        courseId, title, arabicTitle, track, mode, level, language,
        teacherName, country, scheduleSummary, seatsLeft, description,
      )
        .onSuccess { _toastMessages.tryEmit(if (courseId == null) "Course published to catalog" else "Course updated") }
        .onFailure { _toastMessages.tryEmit(it.localizedMessage ?: "Permission denied or failed to save course") }
    }
  }

  fun deleteAdminCourse(courseId: String) {
    viewModelScope.launch {
      repository.deleteAdminCourse(courseId)
        .onSuccess { _toastMessages.tryEmit("Course removed") }
        .onFailure { _toastMessages.tryEmit(it.localizedMessage ?: "Failed to delete course") }
    }
  }

  fun saveAdminTeacher(
    teacherId: String?,
    name: String,
    arabicName: String,
    country: String,
    specialty: String,
    languagesText: String,
    rating: Double,
    availableNow: Boolean,
  ) {
    viewModelScope.launch {
      repository.saveAdminTeacher(
        teacherId, name, arabicName, country, specialty, languagesText, rating, availableNow,
      )
        .onSuccess { _toastMessages.tryEmit(if (teacherId == null) "Teacher added to Reciters directory" else "Teacher updated") }
        .onFailure { _toastMessages.tryEmit(it.localizedMessage ?: "Permission denied or failed to save teacher") }
    }
  }

  fun toggleAdminTeacherAvailability(teacher: AdminTeacher) {
    viewModelScope.launch {
      repository.toggleAdminTeacherAvailability(teacher)
        .onFailure { _toastMessages.tryEmit(it.localizedMessage ?: "Failed to update availability") }
    }
  }

  fun deleteAdminTeacher(teacherId: String) {
    viewModelScope.launch {
      repository.deleteAdminTeacher(teacherId)
        .onSuccess { _toastMessages.tryEmit("Teacher removed") }
        .onFailure { _toastMessages.tryEmit(it.localizedMessage ?: "Failed to delete teacher") }
    }
  }

  fun addAdminManager(
    name: String,
    email: String,
    phone: String,
    role: String,
  ) {
    viewModelScope.launch {
      repository.addAdminManager(name, email, phone, role)
        .onSuccess { _toastMessages.tryEmit("Manager appointed: $email") }
        .onFailure { _toastMessages.tryEmit(it.localizedMessage ?: "Only primary admin can add managers") }
    }
  }

  fun toggleAdminManagerStatus(manager: AdminManager) {
    viewModelScope.launch {
      repository.toggleAdminManagerStatus(manager)
        .onFailure { _toastMessages.tryEmit(it.localizedMessage ?: "Failed to update manager status") }
    }
  }

  fun deleteAdminManager(managerId: String) {
    viewModelScope.launch {
      repository.deleteAdminManager(managerId)
        .onSuccess { _toastMessages.tryEmit("Manager removed") }
        .onFailure { _toastMessages.tryEmit(it.localizedMessage ?: "Failed to remove manager") }
    }
  }

  fun saveDailyQuote(
    quoteId: String?,
    quoteText: String,
    arabicText: String,
    source: String,
    category: String,
    dateLabel: String,
  ) {
    viewModelScope.launch {
      repository.saveDailyQuote(quoteId, quoteText, arabicText, source, category, dateLabel)
        .onSuccess { _toastMessages.tryEmit(if (quoteId == null) "Daily quote added" else "Daily quote updated") }
        .onFailure { _toastMessages.tryEmit(it.localizedMessage ?: "Only admin can manage daily quotes") }
    }
  }

  fun deleteDailyQuote(quoteId: String) {
    viewModelScope.launch {
      repository.deleteDailyQuote(quoteId)
        .onSuccess { _toastMessages.tryEmit("Quote removed") }
        .onFailure { _toastMessages.tryEmit(it.localizedMessage ?: "Failed to remove quote") }
    }
  }

  fun markLoginEventRead(eventId: String) {
    viewModelScope.launch {
      repository.markLoginEventRead(eventId)
    }
  }

  fun markAllLoginsRead(events: List<LoginEvent>) {
    viewModelScope.launch {
      repository.markAllLoginsRead(events)
        .onSuccess { _toastMessages.tryEmit("All login notifications marked read") }
    }
  }

  class Factory(
    private val repository: AlMaqraRepository,
    private val currentUserId: String,
    private val displayName: String?,
    private val email: String?,
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return AlMaqraViewModel(repository, currentUserId, displayName, email) as T
    }
  }
}
