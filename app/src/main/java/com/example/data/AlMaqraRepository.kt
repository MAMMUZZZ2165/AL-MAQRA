package com.example.data

import android.content.Context
import android.os.Build
import com.example.R
import com.example.data.model.AdminCourse
import com.example.data.model.AdminManager
import com.example.data.model.AdminTeacher
import com.example.data.model.AppLanguage
import com.example.data.model.Certificate
import com.example.data.model.CourseCatalogItem
import com.example.data.model.DailyQuote
import com.example.data.model.DirectMessage
import com.example.data.model.Enrollment
import com.example.data.model.GroupCatalogItem
import com.example.data.model.JuzProgress
import com.example.data.model.JuzStatus
import com.example.data.model.LoginEvent
import com.example.data.model.OralTest
import com.example.data.model.SessionLog
import com.example.data.model.StudentNote
import com.example.data.model.StudyPlan
import com.example.data.model.ThemeMode
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

const val BOOTSTRAPPED_ADMIN_EMAIL = "muhammadkerala2165@gmail.com"
const val WHATSAPP_SUPPORT_NUMBER = "9497577616"
const val WHATSAPP_SUPPORT_URI = "https://wa.me/919497577616"

class AlMaqraRepository(private val db: FirebaseFirestore) {

  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    )
  )

  private val auth = Firebase.auth

  private fun requireUserId(): String {
    return auth.currentUser?.uid?.takeIf { it.isNotBlank() }
      ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
  }

  private fun userDoc(uid: String) = db.collection("users").document(uid)

  suspend fun ensureUserInitialized(
    displayName: String?,
    email: String?,
  ): Result<Unit> {
    val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
    val docRef = userDoc(uid)
    return try {
      val snapshot = docRef.get().await()
      if (!snapshot.exists()) {
        val safeName = displayName?.takeIf { it.isNotBlank() } ?: "Student"
        val profilePayload = mapOf(
          "userId" to uid,
          "displayName" to safeName,
          "arabicName" to "محمد - صالح",
          "email" to (email ?: ""),
          "phone" to "+918547182165",
          "country" to "India",
          "jobTitle" to "student",
          "dateOfBirth" to "5 April 2004",
          "gender" to "Male",
          "preferredLanguage" to AppLanguage.ENGLISH.raw,
          "themeMode" to ThemeMode.SYSTEM.raw,
          "readingHours" to 144.0,
          "avgRating" to 0.0,
          "oneToOneCount" to 6,
          "attendedSessionsCount" to 36,
          "testsCompletedCount" to 0,
          "memorizedPages" to 64,
          "memorizedSurahs" to 38,
          "memorizedJuz" to 3,
          "memorizedAyahs" to 820,
          "memorizedLetters" to 34200,
          "gainedDeeds" to 342000,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        )
        val batch = db.batch()
        batch.set(docRef, profilePayload)

        // Seed 30 Juz tiles (Juz 30 & 29 & 1 completed/in-progress for vivid initial state, rest incomplete)
        CatalogData.juzMetadata.forEach { meta ->
          val status = when (meta.number) {
            1, 29, 30 -> JuzStatus.COMPLETED.raw
            2, 28 -> JuzStatus.IN_PROGRESS.raw
            else -> JuzStatus.INCOMPLETE.raw
          }
          val completedAyahs = when (status) {
            JuzStatus.COMPLETED.raw -> meta.totalAyahs
            JuzStatus.IN_PROGRESS.raw -> meta.totalAyahs / 2
            else -> 0
          }
          val juzRef = docRef.collection("juz_progress").document("juz_${meta.number}")
          batch.set(
            juzRef,
            mapOf(
              "juzNumber" to meta.number,
              "status" to status,
              "surahSummary" to meta.surahSpan,
              "completedAyahs" to completedAyahs,
              "totalAyahs" to meta.totalAyahs,
              "teacherRating" to if (status == JuzStatus.COMPLETED.raw) 4.9 else 0.0,
              "updatedAt" to FieldValue.serverTimestamp(),
            ),
          )
        }

        // Seed 1 joined group matching the screenshot ("القرآن العظيم" - Ibrahim Rahmani)
        val defaultGroup = CatalogData.groups.first()
        batch.set(
          docRef.collection("enrollments").document("grp_${defaultGroup.id}"),
          mapOf(
            "targetId" to defaultGroup.id,
            "targetType" to "group",
            "title" to defaultGroup.englishTitle,
            "arabicTitle" to defaultGroup.arabicTitle,
            "track" to defaultGroup.track,
            "mode" to "Online",
            "level" to defaultGroup.level,
            "language" to defaultGroup.language,
            "teacherName" to defaultGroup.teacherName,
            "teacherCountry" to defaultGroup.country,
            "completedSessions" to 14,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
          ),
        )

        // Seed Progress Analytics session history matching the screenshot
        batch.set(
          docRef.collection("session_logs").document("log_seed_1"),
          mapOf(
            "courseTitle" to "Review of the Holy Quran",
            "arabicTitle" to "حلقة تلاوة - الشيخ / علي حبيب الله",
            "teacherName" to "علي عبدالرحمن حبيب الله",
            "teacherCountry" to "Saudi Arabia",
            "level" to "Expert",
            "sessionCategory" to "registered",
            "completedSessions" to 22,
            "durationMinutes" to 990,
            "createdAt" to FieldValue.serverTimestamp(),
          ),
        )
        batch.set(
          docRef.collection("session_logs").document("log_seed_2"),
          mapOf(
            "courseTitle" to "Review of the Holy Quran",
            "arabicTitle" to "حلقة تلاوة - الشيخ / محمد البركاتي",
            "teacherName" to "محمد عبدالله البركاتي",
            "teacherCountry" to "Saudi Arabia",
            "level" to "Intermediate",
            "sessionCategory" to "registered",
            "completedSessions" to 14,
            "durationMinutes" to 630,
            "createdAt" to FieldValue.serverTimestamp(),
          ),
        )
        batch.set(
          docRef.collection("session_logs").document("log_seed_3"),
          mapOf(
            "courseTitle" to "One-to-One Tajweed Correction",
            "arabicTitle" to "جلسة فردية - تصحيح سورة البقرة",
            "teacherName" to "Ibrahim Rahmani",
            "teacherCountry" to "Saudi Arabia",
            "level" to "Intermediate",
            "sessionCategory" to "one_to_one",
            "completedSessions" to 6,
            "durationMinutes" to 180,
            "createdAt" to FieldValue.serverTimestamp(),
          ),
        )
        batch.commit().await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      Result.failure(e)
    }
  }

  private fun <T> observeSubcollection(
    collectionName: String,
    orderByField: String? = null,
    descending: Boolean = false,
    mapper: (DocumentSnapshot) -> T?,
  ): Flow<List<T>> = flow {
    val uid = requireUserId()
    val path = "users/$uid/$collectionName"
    val baseQuery: Query = userDoc(uid).collection(collectionName).let { ref ->
      if (orderByField != null) {
        ref.orderBy(
          orderByField,
          if (descending) Query.Direction.DESCENDING else Query.Direction.ASCENDING,
        )
      } else {
        ref
      }
    }
    emitAll(
      baseQuery.snapshots()
        .map { snap -> snap.documents.mapNotNull(mapper) }
        .catch { error ->
          val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
          if (firestoreError == null && error is CancellationException) throw error
          val exceptionToLog = firestoreError ?: (error as? Exception)
          if (exceptionToLog != null && auth.currentUser?.uid == uid) {
            handleFirestoreError(exceptionToLog, OperationType.LIST, path)
          }
          throw error
        },
    )
  }

  fun observeUserProfile(): Flow<UserProfile?> = flow {
    val uid = requireUserId()
    val path = "users/$uid"
    emitAll(
      userDoc(uid).snapshots()
        .map { snap ->
          if (snap.exists()) {
            snap.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
          } else null
        }
        .catch { error ->
          val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
          if (firestoreError == null && error is CancellationException) throw error
          val exceptionToLog = firestoreError ?: (error as? Exception)
          if (exceptionToLog != null && auth.currentUser?.uid == uid) {
            handleFirestoreError(exceptionToLog, OperationType.GET, path)
          }
          throw error
        },
    )
  }

  fun observeJuzProgress(): Flow<List<JuzProgress>> =
    observeSubcollection("juz_progress", "juzNumber") { doc ->
      doc.toObject(JuzProgress::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
    }

  fun observeEnrollments(): Flow<List<Enrollment>> =
    observeSubcollection("enrollments", "createdAt", descending = true) { doc ->
      doc.toObject(Enrollment::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
    }

  fun observeNotes(): Flow<List<StudentNote>> =
    observeSubcollection("notes", "createdAt", descending = true) { doc ->
      doc.toObject(StudentNote::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
    }

  fun observeStudyPlans(): Flow<List<StudyPlan>> =
    observeSubcollection("study_plans", "createdAt", descending = true) { doc ->
      doc.toObject(StudyPlan::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
    }

  fun observeTests(): Flow<List<OralTest>> =
    observeSubcollection("tests", "createdAt", descending = true) { doc ->
      doc.toObject(OralTest::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
    }

  fun observeCertificates(): Flow<List<Certificate>> =
    observeSubcollection("certificates", "createdAt", descending = true) { doc ->
      doc.toObject(Certificate::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
    }

  fun observeMessages(): Flow<List<DirectMessage>> =
    observeSubcollection("messages", "createdAt", descending = false) { doc ->
      doc.toObject(DirectMessage::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
    }

  fun observeSessionLogs(): Flow<List<SessionLog>> =
    observeSubcollection("session_logs", "createdAt", descending = true) { doc ->
      doc.toObject(SessionLog::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
    }

  suspend fun updateUserSettings(
    language: AppLanguage? = null,
    themeMode: ThemeMode? = null,
  ): Result<Unit> {
    var path = "users"
    return try {
      val uid = requireUserId()
      val ref = userDoc(uid)
      path = ref.path
      val updates = buildMap<String, Any> {
        language?.let { put("preferredLanguage", it.raw) }
        themeMode?.let { put("themeMode", it.raw) }
        put("updatedAt", FieldValue.serverTimestamp())
      }
      ref.update(updates).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, path)
      Result.failure(e)
    }
  }

  suspend fun updatePersonalInfo(
    displayName: String,
    arabicName: String,
    country: String,
    phone: String,
    jobTitle: String,
    dateOfBirth: String,
    gender: String,
  ): Result<Unit> {
    var path = "users"
    return try {
      val uid = requireUserId()
      val ref = userDoc(uid)
      path = ref.path
      ref.update(
        mapOf(
          "displayName" to displayName.trim().take(100).ifBlank { "Student" },
          "arabicName" to arabicName.trim().take(100).ifBlank { "طالب" },
          "country" to country.trim().take(80).ifBlank { "India" },
          "phone" to phone.trim().take(32),
          "jobTitle" to jobTitle.trim().take(80).ifBlank { "student" },
          "dateOfBirth" to dateOfBirth.trim().take(40),
          "gender" to if (gender == "Female") "Female" else "Male",
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, path)
      Result.failure(e)
    }
  }

  suspend fun updateJuzStatus(
    juzNumber: Int,
    status: JuzStatus,
    completedAyahs: Int,
    totalAyahs: Int,
  ): Result<Unit> {
    var path = "users/juz_progress"
    return try {
      val uid = requireUserId()
      val clampedCompleted = completedAyahs.coerceIn(0, totalAyahs)
      val ref = userDoc(uid).collection("juz_progress").document("juz_$juzNumber")
      path = ref.path
      ref.update(
        mapOf(
          "status" to status.raw,
          "completedAyahs" to clampedCompleted,
          "teacherRating" to if (status == JuzStatus.COMPLETED) 5.0 else 0.0,
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, path)
      Result.failure(e)
    }
  }

  suspend fun joinGroup(group: GroupCatalogItem): Result<Unit> {
    var path = "users/enrollments"
    return try {
      val uid = requireUserId()
      val ref = userDoc(uid).collection("enrollments").document("grp_${group.id}")
      path = ref.path
      ref.set(
        mapOf(
          "targetId" to group.id,
          "targetType" to "group",
          "title" to group.englishTitle,
          "arabicTitle" to group.arabicTitle,
          "track" to group.track,
          "mode" to "Online",
          "level" to group.level,
          "language" to group.language,
          "teacherName" to group.teacherName,
          "teacherCountry" to group.country,
          "completedSessions" to 0,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, path)
      Result.failure(e)
    }
  }

  suspend fun registerCourse(
    course: CourseCatalogItem,
    feePlan: String = "Free Trial Class",
  ): Result<Unit> {
    var path = "users/enrollments"
    return try {
      val uid = requireUserId()
      val ref = userDoc(uid).collection("enrollments").document("crs_${course.id}")
      path = ref.path
      val trackWithPlan = "${course.track} • $feePlan".take(100)
      ref.set(
        mapOf(
          "targetId" to course.id,
          "targetType" to "course",
          "title" to course.title,
          "arabicTitle" to course.arabicTitle,
          "track" to trackWithPlan,
          "mode" to course.mode,
          "level" to course.level,
          "language" to course.language,
          "teacherName" to course.teacherName,
          "teacherCountry" to course.country,
          "completedSessions" to 0,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, path)
      Result.failure(e)
    }
  }

  suspend fun removeEnrollment(enrollmentId: String): Result<Unit> {
    var path = "users/enrollments/$enrollmentId"
    return try {
      val uid = requireUserId()
      val ref = userDoc(uid).collection("enrollments").document(enrollmentId)
      path = ref.path
      ref.delete().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, path)
      Result.failure(e)
    }
  }

  suspend fun addNote(
    title: String,
    surahRef: String,
    ayahRange: String,
    category: String,
    content: String,
    teacherName: String,
  ): Result<Unit> {
    var path = "users/notes"
    return try {
      val uid = requireUserId()
      val ref = userDoc(uid).collection("notes").document()
      path = ref.path
      ref.set(
        mapOf(
          "title" to title.trim().take(120).ifBlank { "Tajweed Note" },
          "surahRef" to surahRef.trim().take(80).ifBlank { "Al-Fatihah" },
          "ayahRange" to ayahRange.trim().take(40).ifBlank { "1-7" },
          "category" to category.takeIf { it in listOf("tajweed", "feedback", "personal") }.orEmpty().ifBlank { "personal" },
          "content" to content.trim().take(1000).ifBlank { "Recitation note" },
          "teacherName" to teacherName.trim().take(100),
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, path)
      Result.failure(e)
    }
  }

  suspend fun deleteNote(noteId: String): Result<Unit> {
    var path = "users/notes/$noteId"
    return try {
      val uid = requireUserId()
      val ref = userDoc(uid).collection("notes").document(noteId)
      path = ref.path
      ref.delete().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, path)
      Result.failure(e)
    }
  }

  suspend fun addStudyPlan(
    title: String,
    dailyTargetPages: Int,
    scheduleDays: String,
    preferredTime: String,
  ): Result<Unit> {
    var path = "users/study_plans"
    return try {
      val uid = requireUserId()
      val ref = userDoc(uid).collection("study_plans").document()
      path = ref.path
      ref.set(
        mapOf(
          "title" to title.trim().take(120).ifBlank { "Daily Hifz Plan" },
          "dailyTargetPages" to dailyTargetPages.coerceIn(1, 100),
          "scheduleDays" to scheduleDays.trim().take(100).ifBlank { "Daily" },
          "preferredTime" to preferredTime.trim().take(60).ifBlank { "6:00 AM" },
          "status" to "active",
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, path)
      Result.failure(e)
    }
  }

  suspend fun toggleStudyPlanStatus(plan: StudyPlan): Result<Unit> {
    var path = "users/study_plans/${plan.id}"
    return try {
      val uid = requireUserId()
      val ref = userDoc(uid).collection("study_plans").document(plan.id)
      path = ref.path
      val nextStatus = if (plan.status == "active") "paused" else "active"
      ref.update(
        mapOf(
          "status" to nextStatus,
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, path)
      Result.failure(e)
    }
  }

  suspend fun scheduleOralTest(
    title: String,
    juzRange: String,
    sheikhName: String,
    scheduledDate: String,
  ): Result<Unit> {
    var path = "users/tests"
    return try {
      val uid = requireUserId()
      val ref = userDoc(uid).collection("tests").document()
      path = ref.path
      ref.set(
        mapOf(
          "title" to title.trim().take(120).ifBlank { "Oral Quran Evaluation" },
          "juzRange" to juzRange.trim().take(60).ifBlank { "Juz 30" },
          "sheikhName" to sheikhName.trim().take(100).ifBlank { "Sheikh Ali Habibullah" },
          "scheduledDate" to scheduledDate.trim().take(60).ifBlank { "Tomorrow 6:00 PM" },
          "status" to "scheduled",
          "score" to 0,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, path)
      Result.failure(e)
    }
  }

  suspend fun sendMessage(
    reciterId: String,
    reciterName: String,
    body: String,
  ): Result<Unit> {
    var path = "users/messages"
    return try {
      val uid = requireUserId()
      val ref = userDoc(uid).collection("messages").document()
      path = ref.path
      ref.set(
        mapOf(
          "reciterId" to reciterId.take(128),
          "reciterName" to reciterName.take(100),
          "senderRole" to "student",
          "body" to body.trim().take(1000),
          "createdAt" to FieldValue.serverTimestamp(),
        ),
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, path)
      Result.failure(e)
    }
  }

  suspend fun logLiveSessionAttendance(
    courseTitle: String,
    arabicTitle: String,
    teacherName: String,
    teacherCountry: String,
    level: String,
    sessionCategory: String,
    durationMinutes: Int,
  ): Result<Unit> {
    var path = "users/session_logs"
    return try {
      val uid = requireUserId()
      val userRef = userDoc(uid)
      val logRef = userRef.collection("session_logs").document()
      path = logRef.path
      val safeMinutes = durationMinutes.coerceAtLeast(1)
      val deedsEarned = safeMinutes * 50

      val batch = db.batch()
      batch.set(
        logRef,
        mapOf(
          "courseTitle" to courseTitle.take(140),
          "arabicTitle" to arabicTitle.take(140),
          "teacherName" to teacherName.take(100),
          "teacherCountry" to teacherCountry.take(80),
          "level" to if (level in listOf("Beginner", "Intermediate", "Expert")) level else "Intermediate",
          "sessionCategory" to if (sessionCategory in listOf("guest", "registered", "one_to_one")) sessionCategory else "guest",
          "completedSessions" to 1,
          "durationMinutes" to safeMinutes,
          "createdAt" to FieldValue.serverTimestamp(),
        ),
      )
      batch.update(
        userRef,
        mapOf(
          "attendedSessionsCount" to FieldValue.increment(1),
          "gainedDeeds" to FieldValue.increment(deedsEarned.toLong()),
          "readingHours" to FieldValue.increment(safeMinutes / 60.0),
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      )
      batch.commit().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, path)
      Result.failure(e)
    }
  }

  suspend fun submitFeedback(rating: Int, comment: String): Result<Unit> {
    var path = "users/feedback"
    return try {
      val uid = requireUserId()
      val ref = userDoc(uid).collection("feedback").document()
      path = ref.path
      ref.set(
        mapOf(
          "rating" to rating.coerceIn(1, 5),
          "comment" to comment.trim().take(500),
          "createdAt" to FieldValue.serverTimestamp(),
        ),
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, path)
      Result.failure(e)
    }
  }

  fun observeAdminCourses(): Flow<List<AdminCourse>> = flow {
    requireUserId()
    val query = db.collection("admin_courses").whereEqualTo("visibility", "public")
    emitAll(
      query.snapshots()
        .map { snap ->
          snap.documents
            .mapNotNull { it.toObject(AdminCourse::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            .sortedByDescending { it.createdAt?.seconds ?: Long.MAX_VALUE }
        }
        .catch { error ->
          val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
          if (firestoreError == null && error is CancellationException) throw error
          val exceptionToLog = firestoreError ?: (error as? Exception)
          if (exceptionToLog != null && auth.currentUser != null) {
            handleFirestoreError(exceptionToLog, OperationType.LIST, "admin_courses")
          }
          throw error
        },
    )
  }

  fun observeAdminTeachers(): Flow<List<AdminTeacher>> = flow {
    requireUserId()
    val query = db.collection("admin_teachers").whereEqualTo("visibility", "public")
    emitAll(
      query.snapshots()
        .map { snap ->
          snap.documents
            .mapNotNull { it.toObject(AdminTeacher::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            .sortedByDescending { it.createdAt?.seconds ?: Long.MAX_VALUE }
        }
        .catch { error ->
          val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
          if (firestoreError == null && error is CancellationException) throw error
          val exceptionToLog = firestoreError ?: (error as? Exception)
          if (exceptionToLog != null && auth.currentUser != null) {
            handleFirestoreError(exceptionToLog, OperationType.LIST, "admin_teachers")
          }
          throw error
        },
    )
  }

  fun observeAdminManagers(): Flow<List<AdminManager>> = flow {
    val user = auth.currentUser
      ?: throw IllegalStateException("User must be signed in before accessing managers.")
    val email = user.email?.trim()?.lowercase().orEmpty()
    val query: Query? = when {
      email == BOOTSTRAPPED_ADMIN_EMAIL -> db.collection("admin_managers")
      email.isNotBlank() -> db.collection("admin_managers").whereEqualTo("email", email)
      else -> null
    }
    if (query == null) {
      emitAll(flowOf(emptyList()))
    } else {
      emitAll(
        query.snapshots()
          .map { snap ->
            snap.documents
              .mapNotNull { it.toObject(AdminManager::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
              .sortedByDescending { it.createdAt?.seconds ?: Long.MAX_VALUE }
          }
          .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
              .filterIsInstance<FirebaseFirestoreException>()
              .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val exceptionToLog = firestoreError ?: (error as? Exception)
            if (exceptionToLog != null && auth.currentUser != null) {
              handleFirestoreError(exceptionToLog, OperationType.LIST, "admin_managers")
            }
            throw error
          },
      )
    }
  }

  suspend fun addAdminCourse(
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
  ): Result<Unit> {
    return saveAdminCourse(
      courseId = null,
      title = title,
      arabicTitle = arabicTitle,
      track = track,
      mode = mode,
      level = level,
      language = language,
      teacherName = teacherName,
      country = country,
      scheduleSummary = scheduleSummary,
      seatsLeft = seatsLeft,
      description = description,
    )
  }

  suspend fun saveAdminCourse(
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
  ): Result<Unit> {
    var path = "admin_courses"
    return try {
      val uid = requireUserId()
      val ref = if (courseId.isNullOrBlank()) {
        db.collection("admin_courses").document()
      } else {
        db.collection("admin_courses").document(courseId)
      }
      path = ref.path
      val safeTitle = title.trim().take(140).ifBlank { "Quran Mastery Course" }
      val safeArabic = arabicTitle.trim().take(140).ifBlank { "دورة القرآن الكريم" }
      val safeTrack = track.trim().take(100).ifBlank { "Memorize the Holy Quran" }
      val safeMode = if (mode == "Offline") "Offline" else "Online"
      val safeLevel = if (level in listOf("Beginner", "Intermediate", "Expert")) level else "Beginner"
      val safeLanguage = language.trim().take(40).ifBlank { "Arabic" }
      val safeTeacher = teacherName.trim().take(100).ifBlank { "Sheikh Ali Habibullah" }
      val safeCountry = country.trim().take(80).ifBlank { "India" }
      val safeSchedule = scheduleSummary.trim().take(100).ifBlank { "Flexible Schedule" }
      val safeSeats = seatsLeft.coerceIn(0, 1000)
      val safeDescription = description.trim().take(500)

      val exists = !courseId.isNullOrBlank() && ref.get().await().exists()
      if (exists) {
        ref.update(
          mapOf(
            "title" to safeTitle,
            "arabicTitle" to safeArabic,
            "track" to safeTrack,
            "mode" to safeMode,
            "level" to safeLevel,
            "language" to safeLanguage,
            "teacherName" to safeTeacher,
            "country" to safeCountry,
            "scheduleSummary" to safeSchedule,
            "seatsLeft" to safeSeats,
            "description" to safeDescription,
            "updatedAt" to FieldValue.serverTimestamp(),
          ),
        ).await()
      } else {
        ref.set(
          mapOf(
            "title" to safeTitle,
            "arabicTitle" to safeArabic,
            "track" to safeTrack,
            "mode" to safeMode,
            "level" to safeLevel,
            "language" to safeLanguage,
            "teacherName" to safeTeacher,
            "country" to safeCountry,
            "scheduleSummary" to safeSchedule,
            "seatsLeft" to safeSeats,
            "description" to safeDescription,
            "visibility" to "public",
            "createdBy" to uid,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
          ),
        ).await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, path)
      Result.failure(e)
    }
  }

  suspend fun deleteAdminCourse(courseId: String): Result<Unit> {
    var path = "admin_courses/$courseId"
    return try {
      requireUserId()
      val ref = db.collection("admin_courses").document(courseId)
      path = ref.path
      ref.delete().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, path)
      Result.failure(e)
    }
  }

  suspend fun addAdminTeacher(
    name: String,
    arabicName: String,
    country: String,
    specialty: String,
    languagesText: String,
    rating: Double,
    availableNow: Boolean,
  ): Result<Unit> {
    return saveAdminTeacher(
      teacherId = null,
      name = name,
      arabicName = arabicName,
      country = country,
      specialty = specialty,
      languagesText = languagesText,
      rating = rating,
      availableNow = availableNow,
    )
  }

  suspend fun saveAdminTeacher(
    teacherId: String?,
    name: String,
    arabicName: String,
    country: String,
    specialty: String,
    languagesText: String,
    rating: Double,
    availableNow: Boolean,
  ): Result<Unit> {
    var path = "admin_teachers"
    return try {
      val uid = requireUserId()
      val ref = if (teacherId.isNullOrBlank()) {
        db.collection("admin_teachers").document()
      } else {
        db.collection("admin_teachers").document(teacherId)
      }
      path = ref.path
      val safeName = name.trim().take(100).ifBlank { "Sheikh New Reciter" }
      val safeArabic = arabicName.trim().take(100).ifBlank { "الشيخ المقرئ" }
      val safeCountry = country.trim().take(80).ifBlank { "India" }
      val safeSpecialty = specialty.trim().take(160).ifBlank { "Hafs 'an Asim • Tajweed & Hifz" }
      val safeLangs = languagesText.trim().take(100).ifBlank { "Arabic, English" }
      val safeRating = rating.coerceIn(0.0, 5.0)
      val safeStatus = if (availableNow) "available" else "busy"

      val exists = !teacherId.isNullOrBlank() && ref.get().await().exists()
      if (exists) {
        ref.update(
          mapOf(
            "name" to safeName,
            "arabicName" to safeArabic,
            "country" to safeCountry,
            "specialty" to safeSpecialty,
            "languagesText" to safeLangs,
            "rating" to safeRating,
            "availableStatus" to safeStatus,
            "updatedAt" to FieldValue.serverTimestamp(),
          ),
        ).await()
      } else {
        ref.set(
          mapOf(
            "name" to safeName,
            "arabicName" to safeArabic,
            "country" to safeCountry,
            "specialty" to safeSpecialty,
            "languagesText" to safeLangs,
            "rating" to safeRating,
            "availableStatus" to safeStatus,
            "visibility" to "public",
            "createdBy" to uid,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
          ),
        ).await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, path)
      Result.failure(e)
    }
  }

  suspend fun toggleAdminTeacherAvailability(teacher: AdminTeacher): Result<Unit> {
    var path = "admin_teachers/${teacher.id}"
    return try {
      requireUserId()
      val ref = db.collection("admin_teachers").document(teacher.id)
      path = ref.path
      val next = if (teacher.availableStatus == "available") "busy" else "available"
      ref.update(
        mapOf(
          "availableStatus" to next,
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, path)
      Result.failure(e)
    }
  }

  suspend fun deleteAdminTeacher(teacherId: String): Result<Unit> {
    var path = "admin_teachers/$teacherId"
    return try {
      requireUserId()
      val ref = db.collection("admin_teachers").document(teacherId)
      path = ref.path
      ref.delete().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, path)
      Result.failure(e)
    }
  }

  suspend fun addAdminManager(
    name: String,
    email: String,
    phone: String,
    role: String,
  ): Result<Unit> {
    val cleanEmail = email.trim().lowercase()
    if (!cleanEmail.matches(Regex("^[a-z0-9._%+\\-]+@[a-z0-9.\\-]+\\.[a-z]{2,}$"))) {
      return Result.failure(IllegalArgumentException("Please enter a valid manager email address."))
    }
    var path = "admin_managers/$cleanEmail"
    return try {
      val uid = requireUserId()
      val ref = db.collection("admin_managers").document(cleanEmail)
      path = ref.path
      ref.set(
        mapOf(
          "name" to name.trim().take(100).ifBlank { "Manager" },
          "email" to cleanEmail,
          "phone" to phone.trim().take(32),
          "role" to role.trim().take(80).ifBlank { "Academic Manager" },
          "status" to "active",
          "createdBy" to uid,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, path)
      Result.failure(e)
    }
  }

  suspend fun toggleAdminManagerStatus(manager: AdminManager): Result<Unit> {
    var path = "admin_managers/${manager.id}"
    return try {
      requireUserId()
      val ref = db.collection("admin_managers").document(manager.id)
      path = ref.path
      val next = if (manager.status == "active") "paused" else "active"
      ref.update(
        mapOf(
          "status" to next,
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, path)
      Result.failure(e)
    }
  }

  suspend fun deleteAdminManager(managerId: String): Result<Unit> {
    var path = "admin_managers/$managerId"
    return try {
      requireUserId()
      val ref = db.collection("admin_managers").document(managerId)
      path = ref.path
      ref.delete().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, path)
      Result.failure(e)
    }
  }

  fun observeDailyQuotes(): Flow<List<DailyQuote>> = flow {
    requireUserId()
    val query = db.collection("daily_quotes").whereEqualTo("visibility", "public")
    emitAll(
      query.snapshots()
        .map { snap ->
          snap.documents
            .mapNotNull { it.toObject(DailyQuote::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            .sortedByDescending { it.createdAt?.seconds ?: Long.MAX_VALUE }
        }
        .catch { error ->
          val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
          if (firestoreError == null && error is CancellationException) throw error
          val exceptionToLog = firestoreError ?: (error as? Exception)
          if (exceptionToLog != null && auth.currentUser != null) {
            handleFirestoreError(exceptionToLog, OperationType.LIST, "daily_quotes")
          }
          throw error
        },
    )
  }

  suspend fun saveDailyQuote(
    quoteId: String?,
    quoteText: String,
    arabicText: String,
    source: String,
    category: String,
    dateLabel: String,
  ): Result<Unit> {
    var path = "daily_quotes"
    return try {
      val uid = requireUserId()
      val ref = if (quoteId.isNullOrBlank()) {
        db.collection("daily_quotes").document()
      } else {
        db.collection("daily_quotes").document(quoteId)
      }
      path = ref.path
      val safeQuote = quoteText.trim().take(600).ifBlank { "Recite the Qur'an with measured Tajweed." }
      val safeArabic = arabicText.trim().take(600)
      val safeSource = source.trim().take(160).ifBlank { "AL MAQRA Daily Wisdom" }
      val safeCategory = category.trim().take(60).ifBlank { "Tajweed" }
      val safeDateLabel = dateLabel.trim().take(60).ifBlank { "Daily Quote" }

      val exists = !quoteId.isNullOrBlank() && ref.get().await().exists()
      if (exists) {
        ref.update(
          mapOf(
            "quoteText" to safeQuote,
            "arabicText" to safeArabic,
            "source" to safeSource,
            "category" to safeCategory,
            "dateLabel" to safeDateLabel,
            "updatedAt" to FieldValue.serverTimestamp(),
          ),
        ).await()
      } else {
        ref.set(
          mapOf(
            "quoteText" to safeQuote,
            "arabicText" to safeArabic,
            "source" to safeSource,
            "category" to safeCategory,
            "dateLabel" to safeDateLabel,
            "visibility" to "public",
            "createdBy" to uid,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
          ),
        ).await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, path)
      Result.failure(e)
    }
  }

  suspend fun deleteDailyQuote(quoteId: String): Result<Unit> {
    var path = "daily_quotes/$quoteId"
    return try {
      requireUserId()
      val ref = db.collection("daily_quotes").document(quoteId)
      path = ref.path
      ref.delete().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, path)
      Result.failure(e)
    }
  }

  suspend fun recordUserLogin(
    displayName: String?,
    email: String?,
  ): Result<Unit> {
    var path = "login_events"
    return try {
      val user = auth.currentUser ?: return Result.failure(IllegalStateException("Not signed in"))
      val ref = db.collection("login_events").document()
      path = ref.path
      val safeName = displayName?.takeIf { it.isNotBlank() }?.take(120) ?: "Student"
      val safeEmail = (email ?: user.email ?: "").trim().take(160)
      val device = "${Build.MANUFACTURER} ${Build.MODEL}".trim().take(120).ifBlank { "Android Device" }
      ref.set(
        mapOf(
          "userId" to user.uid,
          "userName" to safeName,
          "userEmail" to safeEmail,
          "deviceInfo" to device,
          "status" to "unread",
          "loginAt" to FieldValue.serverTimestamp(),
        ),
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, path)
      Result.failure(e)
    }
  }

  fun observeLoginEvents(): Flow<List<LoginEvent>> = flow {
    val user = auth.currentUser
      ?: throw IllegalStateException("User must be signed in before accessing login events.")
    val email = user.email?.trim()?.lowercase().orEmpty()
    if (email != BOOTSTRAPPED_ADMIN_EMAIL) {
      emitAll(flowOf(emptyList()))
    } else {
      emitAll(
        db.collection("login_events").snapshots()
          .map { snap ->
            snap.documents
              .mapNotNull { it.toObject(LoginEvent::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
              .sortedByDescending { it.loginAt?.seconds ?: Long.MAX_VALUE }
          }
          .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
              .filterIsInstance<FirebaseFirestoreException>()
              .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val exceptionToLog = firestoreError ?: (error as? Exception)
            if (exceptionToLog != null && auth.currentUser != null) {
              handleFirestoreError(exceptionToLog, OperationType.LIST, "login_events")
            }
            throw error
          },
      )
    }
  }

  suspend fun markLoginEventRead(eventId: String): Result<Unit> {
    var path = "login_events/$eventId"
    return try {
      requireUserId()
      val ref = db.collection("login_events").document(eventId)
      path = ref.path
      ref.update(mapOf("status" to "read")).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, path)
      Result.failure(e)
    }
  }

  suspend fun markAllLoginsRead(events: List<LoginEvent>): Result<Unit> {
    val unread = events.filter { it.status == "unread" && it.id.isNotBlank() }
    if (unread.isEmpty()) return Result.success(Unit)
    return try {
      requireUserId()
      val batch = db.batch()
      unread.take(200).forEach { ev ->
        batch.update(db.collection("login_events").document(ev.id), "status", "read")
      }
      batch.commit().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, "login_events")
      Result.failure(e)
    }
  }
}
