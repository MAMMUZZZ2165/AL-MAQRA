package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.CatalogData
import com.example.data.model.CourseCatalogItem
import com.example.data.model.Enrollment
import com.example.data.model.FeeTier
import com.example.ui.CourseFilterState
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.SegmentedPillToggle
import com.example.ui.components.SheikhAvatarBadge
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.GoldHighlight
import com.example.ui.theme.LocalAlMaqraColors
import com.example.ui.theme.ParchmentCream
import com.example.util.LocalAlMaqraStrings

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CoursesScreen(
  enrollments: List<Enrollment>,
  filterState: CourseFilterState,
  allCourses: List<CourseCatalogItem> = CatalogData.courses,
  onSetMode: (String) -> Unit,
  onApplyFilter: (String?, String?, String?) -> Unit,
  onClearFilter: () -> Unit,
  onRegisterCourse: (CourseCatalogItem, String) -> Unit,
  onDropCourse: (String, String) -> Unit,
) {
  val strings = LocalAlMaqraStrings.current
  val ext = LocalAlMaqraColors.current
  // Default to Browse Courses (1) so available courses, fee structure, and assessment note are immediately prominent
  var selectedTab by remember { mutableIntStateOf(1) }
  var selectedFeeTierId by remember { mutableStateOf("fee_trial") }
  var showFilterSheet by remember { mutableStateOf(false) }
  var coursePendingEnrollment by remember { mutableStateOf<CourseCatalogItem?>(null) }

  val registeredCourses = remember(enrollments, filterState) {
    enrollments.filter { enr ->
      enr.targetType == "course" &&
        enr.mode == filterState.mode &&
        (filterState.language == null || enr.language == filterState.language) &&
        (filterState.level == null || enr.level == filterState.level) &&
        (filterState.track == null || enr.track.contains(filterState.track!!, ignoreCase = true))
    }
  }

  val registeredIds = remember(enrollments) {
    enrollments.filter { it.targetType == "course" }.map { it.targetId }.toSet()
  }

  val browsableCourses = remember(allCourses, filterState) {
    allCourses.filter { c ->
      c.mode == filterState.mode &&
        (filterState.language == null || c.language == filterState.language) &&
        (filterState.level == null || c.level == filterState.level) &&
        (filterState.track == null || c.track == filterState.track)
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
  ) {
    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = MaterialTheme.colorScheme.surface,
      contentColor = MaterialTheme.colorScheme.primary,
      indicator = { tabPositions ->
        TabRowDefaults.SecondaryIndicator(
          modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
          color = MaterialTheme.colorScheme.primary,
        )
      },
    ) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        text = { Text(strings.registeredCourses, style = MaterialTheme.typography.titleSmall) },
        modifier = Modifier.testTag("tab_registered_courses"),
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        text = { Text(strings.browseCourses, style = MaterialTheme.typography.titleSmall) },
        modifier = Modifier.testTag("tab_browse_courses"),
      )
    }

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // Online / Offline Toggle + Filter Button
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(modifier = Modifier.weight(1f)) {
            SegmentedPillToggle(
              options = listOf(strings.online, strings.offline),
              selectedIndex = if (filterState.mode == "Online") 0 else 1,
              onSelect = { idx -> onSetMode(if (idx == 0) "Online" else "Offline") },
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Surface(
            modifier = Modifier
              .height(48.dp)
              .clickable { showFilterSheet = true }
              .testTag("courses_filter_button"),
            shape = RoundedCornerShape(14.dp),
            color = ext.segmentedBg,
            border = BorderStroke(1.dp, ext.cardBorder),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 14.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(
                imageVector = Icons.Filled.FilterAlt,
                contentDescription = strings.filter,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = strings.filter,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
              )
            }
          }
        }
      }

      if (selectedTab == 0) {
        item {
          Text(
            text = strings.myCourses,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
          )
        }

        if (registeredCourses.isEmpty()) {
          item {
            EmptyStateCard(
              message = strings.notRegisteredYet,
              ctaLabel = strings.browseCourses,
              onCtaClick = { selectedTab = 1 },
            )
          }
        } else {
          items(registeredCourses, key = { it.id }) { enr ->
            CourseCard(
              title = enr.title,
              arabicTitle = enr.arabicTitle,
              track = enr.track,
              mode = enr.mode,
              level = enr.level,
              language = enr.language,
              teacherName = enr.teacherName,
              teacherInitial = enr.teacherName.firstOrNull()?.uppercase() ?: "S",
              country = enr.teacherCountry,
              scheduleSummary = "${enr.completedSessions} sessions completed",
              description = "",
              isRegistered = true,
              onActionClick = { onDropCourse(enr.id, enr.title) },
            )
          }
        }
      } else {
        // Teacher Assessment / Interview Note (Mandatory Placement Notice)
        item {
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .animateContentSize(),
            shape = RoundedCornerShape(16.dp),
            color = ext.cardBannerBg,
            border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.22f)),
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.Top,
            ) {
              Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = "Assessment Notice",
                tint = ForestGreen,
                modifier = Modifier.size(22.dp),
              )
              Spacer(Modifier.width(10.dp))
              Column {
                Text(
                  text = "Teacher Assessment & Placement",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                  color = ForestGreen,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                  text = "Every student will attend a teacher assessment/interview before course allocation. The teacher will recommend the appropriate course based on the student's current level to avoid repeating previously learned lessons.",
                  style = MaterialTheme.typography.bodySmall,
                  color = ForestGreen.copy(alpha = 0.88f),
                )
              }
            }
          }
        }

        // Fee Structure Pricing Cards
        item {
          Text(
            text = "Fee Structure",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
          )
          Spacer(Modifier.height(10.dp))
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            CatalogData.feeTiers.forEach { tier ->
              PricingTierCard(
                tier = tier,
                selected = selectedFeeTierId == tier.id,
                onClick = { selectedFeeTierId = tier.id },
              )
            }
          }
        }

        // Available Courses Section Header
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "Available Courses (${browsableCourses.size})",
              style = MaterialTheme.typography.titleLarge,
              color = MaterialTheme.colorScheme.onBackground,
            )
            Icon(
              imageVector = Icons.Filled.School,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
            )
          }
        }

        if (browsableCourses.isEmpty()) {
          item {
            EmptyStateCard(
              message = strings.noSessionsFound,
              ctaLabel = strings.clearFilters,
              onCtaClick = onClearFilter,
            )
          }
        } else {
          items(browsableCourses, key = { it.id }) { course ->
            val isReg = course.id in registeredIds
            val enrId = enrollments.firstOrNull { it.targetType == "course" && it.targetId == course.id }?.id
            CourseCard(
              title = course.title,
              arabicTitle = course.arabicTitle,
              track = course.track,
              mode = course.mode,
              level = course.level,
              language = course.language,
              teacherName = course.teacherName,
              teacherInitial = course.teacherInitial,
              country = course.country,
              scheduleSummary = course.scheduleSummary,
              description = course.description,
              isRegistered = isReg,
              onActionClick = {
                if (isReg && enrId != null) {
                  onDropCourse(enrId, course.title)
                } else {
                  coursePendingEnrollment = course
                }
              },
            )
          }
        }
      }
    }
  }

  // Enrollment Confirmation Dialog (lets student select Free Trial / Group ₹1,000 / Individual ₹1,500)
  coursePendingEnrollment?.let { targetCourse ->
    var chosenTierId by remember(targetCourse.id) { mutableStateOf(selectedFeeTierId) }
    val chosenTier = CatalogData.feeTiers.firstOrNull { it.id == chosenTierId } ?: CatalogData.feeTiers.first()

    AlertDialog(
      onDismissRequest = { coursePendingEnrollment = null },
      title = { Text("Enroll in ${targetCourse.title}") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Choose your preferred class format. Every student attends a teacher assessment/interview before course allocation.",
            style = MaterialTheme.typography.bodySmall,
            color = ext.mutedLabel,
          )
          CatalogData.feeTiers.forEach { tier ->
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { chosenTierId = tier.id },
              shape = RoundedCornerShape(12.dp),
              color = if (chosenTierId == tier.id) ext.cardBannerBg else MaterialTheme.colorScheme.surfaceVariant,
              border = BorderStroke(
                width = if (chosenTierId == tier.id) 1.5.dp else 1.dp,
                color = if (chosenTierId == tier.id) ForestGreen else ext.cardBorder,
              ),
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                RadioButton(
                  selected = chosenTierId == tier.id,
                  onClick = { chosenTierId = tier.id },
                )
                Spacer(Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(tier.title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                  Text("${tier.price} ${tier.period}", style = MaterialTheme.typography.labelMedium, color = ForestGreen)
                }
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            selectedFeeTierId = chosenTier.id
            onRegisterCourse(targetCourse, "${chosenTier.title} (${chosenTier.price})")
            coursePendingEnrollment = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = ParchmentCream),
          modifier = Modifier.testTag("confirm_enroll_btn"),
        ) {
          Text("Confirm Enrollment")
        }
      },
      dismissButton = {
        TextButton(onClick = { coursePendingEnrollment = null }) {
          Text(strings.cancel)
        }
      },
    )
  }

  if (showFilterSheet) {
    var draftLang by remember(filterState) { mutableStateOf(filterState.language) }
    var draftLevel by remember(filterState) { mutableStateOf(filterState.level) }
    var draftTrack by remember(filterState) { mutableStateOf(filterState.track) }

    ModalBottomSheet(onDismissRequest = { showFilterSheet = false }) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        Text(
          text = strings.filter,
          style = MaterialTheme.typography.titleLarge,
        )
        Text("Language", style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf("Arabic", "English", "Urdu").forEach { lang ->
            FilterChip(
              selected = draftLang == lang,
              onClick = { draftLang = if (draftLang == lang) null else lang },
              label = { Text(lang) },
            )
          }
        }
        Text("Level", style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf("Beginner", "Intermediate", "Expert").forEach { lvl ->
            FilterChip(
              selected = draftLevel == lvl,
              onClick = { draftLevel = if (draftLevel == lvl) null else lvl },
              label = { Text(lvl) },
            )
          }
        }
        Text("Track", style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf("Memorize the Holy Quran", "Review of the Holy Quran", "Recitation & Tajweed").forEach { trk ->
            FilterChip(
              selected = draftTrack == trk,
              onClick = { draftTrack = if (draftTrack == trk) null else trk },
              label = { Text(trk) },
            )
          }
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
        ) {
          TextButton(
            onClick = {
              onClearFilter()
              showFilterSheet = false
            },
          ) {
            Text(strings.clearFilters)
          }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = {
              onApplyFilter(draftLang, draftLevel, draftTrack)
              showFilterSheet = false
            },
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = ParchmentCream),
          ) {
            Text(strings.applyFilters)
          }
        }
        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }
}

@Composable
private fun PricingTierCard(
  tier: FeeTier,
  selected: Boolean,
  onClick: () -> Unit,
) {
  val ext = LocalAlMaqraColors.current
  val scale by animateFloatAsState(
    targetValue = if (selected) 1.015f else 1f,
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    label = "pricing_scale",
  )
  val containerColor by animateColorAsState(
    targetValue = when {
      tier.highlighted -> ForestGreen
      selected -> ext.cardBannerBg
      else -> MaterialTheme.colorScheme.surface
    },
    label = "pricing_bg",
  )
  val titleColor = if (tier.highlighted) ParchmentCream else MaterialTheme.colorScheme.onSurface
  val subColor = if (tier.highlighted) ParchmentCream.copy(alpha = 0.85f) else ext.mutedLabel

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .scale(scale)
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onClick)
      .testTag("pricing_card_${tier.id}"),
    shape = RoundedCornerShape(16.dp),
    color = containerColor,
    border = BorderStroke(
      width = if (selected) 2.dp else 1.dp,
      color = if (selected) GoldHighlight else ext.cardBorder,
    ),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = tier.title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = titleColor,
          )
          if (selected) {
            Spacer(Modifier.width(6.dp))
            Icon(
              imageVector = Icons.Filled.CheckCircle,
              contentDescription = "Selected plan",
              tint = if (tier.highlighted) GoldHighlight else ForestGreen,
              modifier = Modifier.size(18.dp),
            )
          }
        }
        Spacer(Modifier.height(4.dp))
        Text(
          text = tier.subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = subColor,
        )
      }
      Spacer(Modifier.width(12.dp))
      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = tier.price,
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
          color = if (tier.highlighted) GoldHighlight else ForestGreen,
        )
        Text(
          text = tier.period,
          style = MaterialTheme.typography.labelSmall,
          color = subColor,
        )
      }
    }
  }
}

@Composable
private fun CourseCard(
  title: String,
  arabicTitle: String,
  track: String,
  mode: String,
  level: String,
  language: String,
  teacherName: String,
  teacherInitial: String,
  country: String,
  scheduleSummary: String,
  description: String,
  isRegistered: Boolean,
  onActionClick: () -> Unit,
) {
  val strings = LocalAlMaqraStrings.current
  val ext = LocalAlMaqraColors.current
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .animateContentSize(),
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, ext.cardBorder),
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = ext.cardBannerBg,
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = track,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = ext.cardBannerText,
          )
          Text(
            text = mode,
            style = MaterialTheme.typography.labelMedium,
            color = ForestGreen,
          )
        }
      }
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = arabicTitle,
        fontFamily = AmiriFontFamily,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
      )
      Text(
        text = title,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface,
      )
      if (description.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = description,
          style = MaterialTheme.typography.bodySmall,
          color = ext.mutedLabel,
        )
      }
      Spacer(modifier = Modifier.height(10.dp))
      Row(verticalAlignment = Alignment.CenterVertically) {
        SheikhAvatarBadge(initial = teacherInitial, size = 36.dp)
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = teacherName,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = country,
            style = MaterialTheme.typography.bodySmall,
            color = ext.mutedLabel,
          )
        }
      }
      Spacer(modifier = Modifier.height(10.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Filled.SignalCellularAlt, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
          Spacer(Modifier.width(4.dp))
          Text(level, style = MaterialTheme.typography.bodySmall)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Filled.Language, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
          Spacer(Modifier.width(4.dp))
          Text(language, style = MaterialTheme.typography.bodySmall)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Filled.Schedule, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
          Spacer(Modifier.width(4.dp))
          Text(scheduleSummary, style = MaterialTheme.typography.bodySmall, color = ext.mutedLabel)
        }
      }
      Spacer(modifier = Modifier.height(12.dp))
      if (isRegistered) {
        OutlinedButton(
          onClick = onActionClick,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text("${strings.registeredBadge} • ${strings.unregister}")
        }
      } else {
        Button(
          onClick = onActionClick,
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = ParchmentCream),
          modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .testTag("enroll_btn_${title.lowercase().replace(' ', '_')}"),
        ) {
          Text(strings.registerCourse, style = MaterialTheme.typography.labelLarge)
        }
      }
    }
  }
}
