package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class ThemeMode(val raw: String) {
  SYSTEM("system"),
  LIGHT("light"),
  DARK("dark");

  companion object {
    fun fromRaw(raw: String?): ThemeMode =
      entries.firstOrNull { it.raw == raw } ?: SYSTEM
  }
}

enum class AppLanguage(val raw: String, val label: String) {
  ENGLISH("en", "English"),
  ARABIC("ar", "العربية"),
  URDU("ur", "اردو");

  companion object {
    fun fromRaw(raw: String?): AppLanguage =
      entries.firstOrNull { it.raw == raw } ?: ENGLISH
  }
}

enum class JuzStatus(val raw: String) {
  COMPLETED("completed"),
  IN_PROGRESS("in_progress"),
  INCOMPLETE("incomplete");

  companion object {
    fun fromRaw(raw: String?): JuzStatus =
      entries.firstOrNull { it.raw == raw } ?: INCOMPLETE
  }
}

@Keep
@IgnoreExtraProperties
data class UserProfile(
  @DocumentId val id: String = "",
  val userId: String = "",
  val displayName: String = "",
  val arabicName: String = "",
  val email: String = "",
  val phone: String = "+918547182165",
  val country: String = "India",
  val jobTitle: String = "student",
  val dateOfBirth: String = "5 April 2004",
  val gender: String = "Male",
  val preferredLanguage: String = "en",
  val themeMode: String = "system",
  val readingHours: Double = 144.0,
  val avgRating: Double = 0.0,
  val oneToOneCount: Int = 6,
  val attendedSessionsCount: Int = 36,
  val testsCompletedCount: Int = 0,
  val memorizedPages: Int = 64,
  val memorizedSurahs: Int = 38,
  val memorizedJuz: Int = 3,
  val memorizedAyahs: Int = 820,
  val memorizedLetters: Int = 34200,
  val gainedDeeds: Int = 342000,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class JuzProgress(
  @DocumentId val id: String = "",
  val juzNumber: Int = 1,
  val status: String = JuzStatus.INCOMPLETE.raw,
  val surahSummary: String = "",
  val completedAyahs: Int = 0,
  val totalAyahs: Int = 148,
  val teacherRating: Double = 0.0,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class Enrollment(
  @DocumentId val id: String = "",
  val targetId: String = "",
  val targetType: String = "course",
  val title: String = "",
  val arabicTitle: String = "",
  val track: String = "",
  val mode: String = "Online",
  val level: String = "Beginner",
  val language: String = "Arabic",
  val teacherName: String = "",
  val teacherCountry: String = "Saudi Arabia",
  val completedSessions: Int = 0,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class StudentNote(
  @DocumentId val id: String = "",
  val title: String = "",
  val surahRef: String = "",
  val ayahRange: String = "",
  val category: String = "tajweed",
  val content: String = "",
  val teacherName: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class StudyPlan(
  @DocumentId val id: String = "",
  val title: String = "",
  val dailyTargetPages: Int = 2,
  val scheduleDays: String = "",
  val preferredTime: String = "",
  val status: String = "active",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class OralTest(
  @DocumentId val id: String = "",
  val title: String = "",
  val juzRange: String = "",
  val sheikhName: String = "",
  val scheduledDate: String = "",
  val status: String = "scheduled",
  val score: Int = 0,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class Certificate(
  @DocumentId val id: String = "",
  val title: String = "",
  val track: String = "",
  val issuedBy: String = "",
  val verificationCode: String = "",
  val issuedDate: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class DirectMessage(
  @DocumentId val id: String = "",
  val reciterId: String = "",
  val reciterName: String = "",
  val senderRole: String = "student",
  val body: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class SessionLog(
  @DocumentId val id: String = "",
  val courseTitle: String = "",
  val arabicTitle: String = "",
  val teacherName: String = "",
  val teacherCountry: String = "",
  val level: String = "Intermediate",
  val sessionCategory: String = "registered",
  val completedSessions: Int = 0,
  val durationMinutes: Int = 30,
  @ServerTimestamp val createdAt: Timestamp? = null,
)

// Catalog & Static Models (Curated AL MAQRA Directory)
data class LiveSessionItem(
  val id: String,
  val track: String,
  val arabicTitle: String,
  val teacherName: String,
  val teacherInitial: String,
  val country: String,
  val mode: String,
  val level: String,
  val language: String,
  val studentCount: Int,
  val remainingSeconds: Long,
  val timeSlot: String,
  val ongoing: Boolean = true,
)

data class GroupCatalogItem(
  val id: String,
  val arabicTitle: String,
  val englishTitle: String,
  val subject: String,
  val track: String,
  val teacherName: String,
  val teacherInitial: String,
  val country: String,
  val currentMembers: Int,
  val maxMembers: Int,
  val level: String,
  val language: String,
)

data class CourseCatalogItem(
  val id: String,
  val title: String,
  val arabicTitle: String,
  val track: String,
  val mode: String,
  val level: String,
  val language: String,
  val teacherName: String,
  val teacherInitial: String,
  val country: String,
  val scheduleSummary: String,
  val seatsLeft: Int,
  val description: String = "",
  val feeSummary: String = "Free Trial • ₹1,000 Group • ₹1,500 Individual",
)

data class FeeTier(
  val id: String,
  val title: String,
  val price: String,
  val period: String,
  val subtitle: String,
  val highlighted: Boolean = false,
)

data class ReciterProfile(
  val id: String,
  val name: String,
  val arabicName: String,
  val country: String,
  val specialty: String,
  val languages: List<String>,
  val rating: Double,
  val availableNow: Boolean,
)

data class JuzMetadata(
  val number: Int,
  val arabicName: String,
  val surahSpan: String,
  val totalAyahs: Int,
)

data class BadgeItem(
  val id: String,
  val title: String,
  val subtitle: String,
  val unlocked: Boolean,
)

data class NotificationItem(
  val id: String,
  val title: String,
  val body: String,
  val timeAgo: String,
)

@Keep
@IgnoreExtraProperties
data class AdminCourse(
  @DocumentId val id: String = "",
  val title: String = "",
  val arabicTitle: String = "",
  val track: String = "Memorize the Holy Quran",
  val mode: String = "Online",
  val level: String = "Beginner",
  val language: String = "Arabic",
  val teacherName: String = "",
  val country: String = "Saudi Arabia",
  val scheduleSummary: String = "",
  val seatsLeft: Int = 20,
  val description: String = "",
  val visibility: String = "public",
  val createdBy: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class AdminTeacher(
  @DocumentId val id: String = "",
  val name: String = "",
  val arabicName: String = "",
  val country: String = "Saudi Arabia",
  val specialty: String = "",
  val languagesText: String = "Arabic",
  val rating: Double = 4.9,
  val availableStatus: String = "available",
  val visibility: String = "public",
  val createdBy: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class AdminManager(
  @DocumentId val id: String = "",
  val name: String = "",
  val email: String = "",
  val phone: String = "",
  val role: String = "Academic Manager",
  val status: String = "active",
  val createdBy: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class DailyQuote(
  @DocumentId val id: String = "",
  val quoteText: String = "",
  val arabicText: String = "",
  val source: String = "",
  val category: String = "Tajweed",
  val dateLabel: String = "",
  val visibility: String = "public",
  val createdBy: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class LoginEvent(
  @DocumentId val id: String = "",
  val userId: String = "",
  val userName: String = "",
  val userEmail: String = "",
  val deviceInfo: String = "",
  val status: String = "unread",
  @ServerTimestamp val loginAt: Timestamp? = null,
)

data class QuranSurahMeta(
  val number: Int,
  val arabicName: String,
  val englishName: String,
  val englishTranslation: String,
  val ayahCount: Int,
  val revelationType: String,
)

data class QuranAyah(
  val globalNumber: Int,
  val numberInSurah: Int,
  val arabicText: String,
  val englishTranslation: String,
)

data class QuranReciterAudio(
  val id: String,
  val name: String,
  val style: String,
)

data class AiChatMessage(
  val id: String,
  val isFromUser: Boolean,
  val text: String,
  val isStreaming: Boolean = false,
)


