package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.CatalogData
import com.example.data.model.Enrollment
import com.example.data.model.GroupCatalogItem
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.SheikhAvatarBadge
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.LocalAlMaqraColors
import com.example.ui.theme.ParchmentCream
import com.example.util.LocalAlMaqraStrings

@Composable
fun GroupsScreen(
  enrollments: List<Enrollment>,
  onJoinGroup: (GroupCatalogItem) -> Unit,
  onLeaveGroup: (String, String) -> Unit,
) {
  val strings = LocalAlMaqraStrings.current
  val ext = LocalAlMaqraColors.current
  var selectedTab by remember { mutableIntStateOf(0) }

  val joinedGroupEnrollments = remember(enrollments) {
    enrollments.filter { it.targetType == "group" }
  }
  val joinedTargetIds = remember(joinedGroupEnrollments) {
    joinedGroupEnrollments.map { it.targetId }.toSet()
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
        text = { Text(strings.myGroups, style = MaterialTheme.typography.titleSmall) },
        modifier = Modifier.testTag("tab_my_groups"),
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        text = { Text(strings.discoverGroups, style = MaterialTheme.typography.titleSmall) },
        modifier = Modifier.testTag("tab_discover_groups"),
      )
    }

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      if (selectedTab == 0) {
        if (joinedGroupEnrollments.isEmpty()) {
          item {
            EmptyStateCard(
              message = "You haven't joined any study groups yet.",
              ctaLabel = strings.discoverGroups,
              onCtaClick = { selectedTab = 1 },
            )
          }
        } else {
          items(joinedGroupEnrollments, key = { it.id }) { enrollment ->
            val catalogItem = CatalogData.groups.firstOrNull { it.id == enrollment.targetId }
            GroupCard(
              arabicTitle = enrollment.arabicTitle,
              subject = "Quran",
              track = enrollment.track,
              teacherInitial = enrollment.teacherName.firstOrNull()?.uppercase() ?: "S",
              teacherName = enrollment.teacherName,
              country = enrollment.teacherCountry,
              membersText = catalogItem?.let { "${it.currentMembers}/${it.maxMembers}" } ?: "Active",
              level = enrollment.level,
              language = enrollment.language,
              isJoined = true,
              onActionClick = { onLeaveGroup(enrollment.id, enrollment.title) },
            )
          }
        }
      } else {
        items(CatalogData.groups, key = { it.id }) { group ->
          val isJoined = group.id in joinedTargetIds
          val enrollmentId = joinedGroupEnrollments.firstOrNull { it.targetId == group.id }?.id
          GroupCard(
            arabicTitle = group.arabicTitle,
            subject = group.subject,
            track = group.track,
            teacherInitial = group.teacherInitial,
            teacherName = group.teacherName,
            country = group.country,
            membersText = "${group.currentMembers}/${group.maxMembers}",
            level = group.level,
            language = group.language,
            isJoined = isJoined,
            onActionClick = {
              if (isJoined && enrollmentId != null) {
                onLeaveGroup(enrollmentId, group.englishTitle)
              } else {
                onJoinGroup(group)
              }
            },
          )
        }
      }
    }
  }
}

@Composable
private fun GroupCard(
  arabicTitle: String,
  subject: String,
  track: String,
  teacherInitial: String,
  teacherName: String,
  country: String,
  membersText: String,
  level: String,
  language: String,
  isJoined: Boolean,
  onActionClick: () -> Unit,
) {
  val strings = LocalAlMaqraStrings.current
  val ext = LocalAlMaqraColors.current
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, ext.cardBorder),
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f),
        ) {
          Text(
            text = arabicTitle,
            fontFamily = AmiriFontFamily,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
          )
          Text(
            text = "  •  ",
            color = ext.goldAccent,
            style = MaterialTheme.typography.titleMedium,
          )
          Icon(
            imageVector = Icons.Filled.MenuBook,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = subject,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = track,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )

      Spacer(modifier = Modifier.height(14.dp))
      Row(verticalAlignment = Alignment.CenterVertically) {
        SheikhAvatarBadge(initial = teacherInitial, size = 40.dp)
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = teacherName,
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
          color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
          text = "  •  ",
          color = ext.goldAccent,
        )
        Text(
          text = country,
          style = MaterialTheme.typography.bodySmall,
          color = ext.mutedLabel,
        )
      }

      Spacer(modifier = Modifier.height(14.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(14.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Filled.Groups,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = membersText,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface,
            )
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Filled.SignalCellularAlt,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = level,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface,
            )
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Filled.Language,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = language,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface,
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
      if (isJoined) {
        OutlinedButton(
          onClick = onActionClick,
          shape = RoundedCornerShape(10.dp),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(strings.leaveGroup, color = MaterialTheme.colorScheme.primary)
        }
      } else {
        Button(
          onClick = onActionClick,
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = ForestGreen,
            contentColor = ParchmentCream,
          ),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(strings.joinGroup)
        }
      }
    }
  }
}
