package com.example.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.CatalogData
import com.example.data.model.DailyQuote
import com.example.data.model.UserProfile
import com.example.ui.components.QrCodeGraphic
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.DarkCardPine
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.GoldHighlight
import com.example.ui.theme.LocalAlMaqraColors
import com.example.ui.theme.ParchmentCream
import com.example.util.LocalAlMaqraStrings

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
  profile: UserProfile?,
  dailyQuotes: List<DailyQuote> = emptyList(),
  onSubmitFeedback: (Int, String) -> Unit,
  onNavigateToCourses: () -> Unit,
  onOpenQuran: () -> Unit,
  onOpenAiAssistant: () -> Unit,
  onOpenWhatsApp: () -> Unit,
) {
  val strings = LocalAlMaqraStrings.current
  val ext = LocalAlMaqraColors.current
  var showRatingDialog by remember { mutableStateOf(false) }

  val greetingName = profile?.displayName?.takeIf { it.isNotBlank() } ?: "Student"
  val combinedQuotes = remember(dailyQuotes) {
    val merged = dailyQuotes + CatalogData.defaultQuotes
    merged.distinctBy { it.quoteText.trim().lowercase() }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    // 1. Personalized Welcome Header
    item {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "${strings.welcomeBack}, $greetingName!",
          style = MaterialTheme.typography.headlineMedium,
          color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = strings.journeySubtitle,
          style = MaterialTheme.typography.bodyMedium,
          color = ext.mutedLabel,
        )
      }
    }

    // 2. Quick Access Row: Holy Qur'an | AI Assistant | WhatsApp 9497577616
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        QuickShortcutCard(
          title = strings.quranReader,
          subtitle = "114 Surahs • Audio",
          icon = Icons.AutoMirrored.Filled.MenuBook,
          highlighted = true,
          onClick = onOpenQuran,
          modifier = Modifier
            .weight(1f)
            .testTag("home_quick_quran"),
        )
        QuickShortcutCard(
          title = strings.aiAssistant,
          subtitle = "Tajweed Mentor",
          icon = Icons.Filled.AutoAwesome,
          highlighted = false,
          onClick = onOpenAiAssistant,
          modifier = Modifier
            .weight(1f)
            .testTag("home_quick_ai"),
        )
        QuickShortcutCard(
          title = "WhatsApp",
          subtitle = "9497577616",
          icon = Icons.Filled.Chat,
          highlighted = false,
          onClick = onOpenWhatsApp,
          modifier = Modifier
            .weight(1f)
            .testTag("home_quick_whatsapp"),
        )
      }
    }

    // 3. WHY CHOOSE US (From Brand Card)
    item {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ext.cardBorder),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Surface(
            shape = RoundedCornerShape(50),
            color = ext.cardBannerBg,
            border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.14f)),
          ) {
            Text(
              text = "WHY CHOOSE US",
              style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
              ),
              color = ForestGreen,
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            )
          }
          Spacer(modifier = Modifier.height(14.dp))
          Text(
            text = "Why AL MAQRA is the Right Choice for Your Family",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
          )
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "We provide structured Islamic education with compassionate teachers, personal tracking, and complete convenience from home.",
            style = MaterialTheme.typography.bodyMedium,
            color = ext.mutedLabel,
            textAlign = TextAlign.Center,
          )
          Spacer(modifier = Modifier.height(16.dp))
          Button(
            onClick = onNavigateToCourses,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = ParchmentCream),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("home_explore_courses_btn"),
          ) {
            Icon(Icons.Filled.School, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Explore Courses & Fee Structure", style = MaterialTheme.typography.labelLarge)
          }
        }
      }
    }

    // 4. OUR MISSION Section
    item {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = ForestGreen,
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Surface(
            shape = RoundedCornerShape(50),
            color = ParchmentCream.copy(alpha = 0.16f),
          ) {
            Text(
              text = "OUR MISSION",
              style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
              ),
              color = GoldHighlight,
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
            )
          }
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "To help people around the world recite the Holy Qur'an with proper Tajweed.",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = ParchmentCream,
            textAlign = TextAlign.Center,
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Learn Qur'an reading, Tajweed rules, recitation & pronunciation wherever you are.",
            style = MaterialTheme.typography.bodyMedium,
            color = ParchmentCream.copy(alpha = 0.88f),
            textAlign = TextAlign.Center,
          )
        }
      }
    }

    // 5. About Studying Qur'an & Daily Tajweed Quotes
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = "About Studying Qur'an & Tajweed Quotes",
          style = MaterialTheme.typography.titleLarge,
          color = MaterialTheme.colorScheme.onBackground,
        )

        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, ext.cardBorder),
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Filled.MenuBook, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp))
              Spacer(Modifier.width(8.dp))
              Text(
                text = "Why Tajweed & Guided Study Matter",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
              )
            }
            Spacer(Modifier.height(8.dp))
            Text(
              text = "Studying the Qur'an with Tajweed preserves the exact articulation of every Arabic letter (Makharij) and honors the rhythm revealed to the Prophet (peace be upon him). With live feedback, structured Hifz cycles, and daily reflection, every learner progresses with clarity and confidence.",
              style = MaterialTheme.typography.bodyMedium,
              color = ext.mutedLabel,
            )
          }
        }

        val quotePagerState = rememberPagerState(pageCount = { combinedQuotes.size })
        HorizontalPager(
          state = quotePagerState,
          pageSpacing = 12.dp,
        ) { idx ->
          val quote = combinedQuotes[idx]
          DailyQuoteCard(quote = quote)
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center,
        ) {
          repeat(combinedQuotes.size.coerceAtMost(8)) { i ->
            val selected = quotePagerState.currentPage % combinedQuotes.size.coerceAtMost(8) == i
            Box(
              modifier = Modifier
                .padding(horizontal = 3.dp)
                .size(width = if (selected) 18.dp else 7.dp, height = 7.dp)
                .clip(CircleShape)
                .background(
                  if (selected) MaterialTheme.colorScheme.primary
                  else ext.mutedLabel.copy(alpha = 0.35f),
                ),
            )
          }
        }
      }
    }

    // 6. Interactive Feedback Banner Carousel
    item {
      val pagerState = rememberPagerState(pageCount = { 2 })
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(
          state = pagerState,
          pageSpacing = 12.dp,
        ) { page ->
          if (page == 0) {
            OpinionBannerCard(onRateClick = { showRatingDialog = true })
          } else {
            ExploreCoursesBannerCard(onBrowseClick = onNavigateToCourses)
          }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          repeat(2) { index ->
            val selected = pagerState.currentPage == index
            Box(
              modifier = Modifier
                .size(width = if (selected) 20.dp else 7.dp, height = 7.dp)
                .clip(CircleShape)
                .background(
                  if (selected) MaterialTheme.colorScheme.primary
                  else ext.mutedLabel.copy(alpha = 0.35f),
                ),
            )
          }
        }
      }
    }

    // 7. TRUSTED BY STUDENTS WORLDWIDE + Brand Footer (From Brand Image 2)
    item {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = DarkCardPine,
        border = BorderStroke(1.dp, GoldHighlight.copy(alpha = 0.25f)),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Public, contentDescription = null, tint = GoldHighlight, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(
              text = "TRUSTED BY STUDENTS WORLDWIDE • LEARN. RECITE. REFLECT.",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
              ),
              color = ParchmentCream,
              textAlign = TextAlign.Center,
            )
          }
          Spacer(modifier = Modifier.height(14.dp))
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            CatalogData.worldwideCountries.forEach { countryLabel ->
              Surface(
                shape = RoundedCornerShape(50),
                color = ForestGreen.copy(alpha = 0.72f),
                border = BorderStroke(1.dp, ParchmentCream.copy(alpha = 0.16f)),
              ) {
                Text(
                  text = countryLabel,
                  style = MaterialTheme.typography.labelMedium,
                  color = ParchmentCream,
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(18.dp))
          HorizontalDivider(color = ParchmentCream.copy(alpha = 0.16f))
          Spacer(modifier = Modifier.height(16.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Surface(
              shape = CircleShape,
              color = ParchmentCream,
              modifier = Modifier.size(44.dp),
            ) {
              Image(
                painter = painterResource(id = R.drawable.al_maqra_logo),
                contentDescription = "AL MAQRA Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                  .fillMaxSize()
                  .clip(CircleShape),
              )
            }
            Spacer(Modifier.width(12.dp))
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "AL MAQRA ",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = ParchmentCream,
                )
                Text(
                  text = "المقرأ",
                  fontFamily = AmiriFontFamily,
                  style = MaterialTheme.typography.titleMedium,
                  color = GoldHighlight,
                )
              }
              Text(
                text = "LEARN. RECITE. REFLECT.",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                color = ParchmentCream.copy(alpha = 0.8f),
              )
            }
          }
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "A global Qur'anic learning platform and online Madrasa helping students worldwide recite the Holy Qur'an correctly with Tajweed.",
            style = MaterialTheme.typography.bodySmall,
            color = ParchmentCream.copy(alpha = 0.82f),
          )
        }
      }
    }
  }

  if (showRatingDialog) {
    FeedbackRatingDialog(
      onDismiss = { showRatingDialog = false },
      onSubmit = { rating, comment ->
        onSubmitFeedback(rating, comment)
        showRatingDialog = false
      },
    )
  }
}

@Composable
private fun QuickShortcutCard(
  title: String,
  subtitle: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  highlighted: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val ext = LocalAlMaqraColors.current
  Surface(
    modifier = modifier
      .clip(RoundedCornerShape(14.dp))
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(14.dp),
    color = if (highlighted) ForestGreen else MaterialTheme.colorScheme.surface,
    border = if (highlighted) null else BorderStroke(1.dp, ext.cardBorder),
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
      horizontalAlignment = Alignment.Start,
    ) {
      Icon(
        imageVector = icon,
        contentDescription = title,
        tint = if (highlighted) GoldHighlight else MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(20.dp),
      )
      Spacer(Modifier.height(6.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = if (highlighted) ParchmentCream else MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.labelSmall,
        color = if (highlighted) ParchmentCream.copy(alpha = 0.8f) else ext.mutedLabel,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun DailyQuoteCard(quote: DailyQuote) {
  val ext = LocalAlMaqraColors.current
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .animateContentSize(),
    shape = RoundedCornerShape(16.dp),
    color = ext.cardBannerBg,
    border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.16f)),
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = ForestGreen,
        ) {
          Text(
            text = quote.category.ifBlank { "Tajweed" },
            style = MaterialTheme.typography.labelSmall,
            color = ParchmentCream,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          )
        }
        Text(
          text = quote.dateLabel.ifBlank { "Daily Quote" },
          style = MaterialTheme.typography.labelSmall,
          color = ForestGreen.copy(alpha = 0.78f),
        )
      }

      if (quote.arabicText.isNotBlank()) {
        Spacer(Modifier.height(10.dp))
        Text(
          text = quote.arabicText,
          fontFamily = AmiriFontFamily,
          fontSize = 20.sp,
          lineHeight = 32.sp,
          color = ForestGreen,
          textAlign = TextAlign.End,
          modifier = Modifier.fillMaxWidth(),
        )
      }

      Spacer(Modifier.height(8.dp))
      Row(verticalAlignment = Alignment.Top) {
        Icon(
          imageVector = Icons.Filled.FormatQuote,
          contentDescription = null,
          tint = ForestGreen,
          modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
          text = quote.quoteText,
          style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
          color = ForestGreen,
        )
      }
      Spacer(Modifier.height(8.dp))
      Text(
        text = "— ${quote.source}",
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = ForestGreen.copy(alpha = 0.8f),
      )
    }
  }
}

@Composable
private fun OpinionBannerCard(onRateClick: () -> Unit) {
  val strings = LocalAlMaqraStrings.current
  val ext = LocalAlMaqraColors.current
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
    color = ext.cardBannerBg,
    border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.14f)),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = strings.opinionBannerTitle,
          style = MaterialTheme.typography.titleLarge,
          color = ForestGreen,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = strings.opinionBannerSubtitle,
          style = MaterialTheme.typography.bodySmall,
          color = ForestGreen.copy(alpha = 0.78f),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
          onClick = onRateClick,
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = ForestGreen,
            contentColor = ParchmentCream,
          ),
          modifier = Modifier.testTag("rate_now_button"),
        ) {
          Text(
            text = strings.rateNow,
            style = MaterialTheme.typography.labelLarge,
          )
        }
      }
      Spacer(modifier = Modifier.width(14.dp))
      QrCodeGraphic(modifier = Modifier.size(92.dp))
    }
  }
}

@Composable
private fun ExploreCoursesBannerCard(onBrowseClick: () -> Unit) {
  val strings = LocalAlMaqraStrings.current
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
    color = ForestGreen,
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "Free Trial & Guided Placement",
          style = MaterialTheme.typography.titleLarge,
          color = ParchmentCream,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Book a free teacher assessment before choosing Group (₹1,000/mo) or Individual (₹1,500/mo) classes.",
          style = MaterialTheme.typography.bodySmall,
          color = ParchmentCream.copy(alpha = 0.85f),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
          onClick = onBrowseClick,
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = ParchmentCream,
            contentColor = ForestGreen,
          ),
        ) {
          Text(strings.browseCourses, style = MaterialTheme.typography.labelLarge)
        }
      }
    }
  }
}

@Composable
private fun FeedbackRatingDialog(
  onDismiss: () -> Unit,
  onSubmit: (Int, String) -> Unit,
) {
  val strings = LocalAlMaqraStrings.current
  var selectedRating by remember { mutableIntStateOf(5) }
  var comment by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(strings.opinionBannerTitle) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center,
        ) {
          (1..5).forEach { star ->
            Icon(
              imageVector = if (star <= selectedRating) Icons.Filled.Star else Icons.Outlined.StarOutline,
              contentDescription = "Rate $star stars",
              tint = GoldHighlight,
              modifier = Modifier
                .size(36.dp)
                .clickable { selectedRating = star }
                .padding(2.dp),
            )
          }
        }
        OutlinedTextField(
          value = comment,
          onValueChange = { comment = it },
          label = { Text("Share your feedback (optional)") },
          modifier = Modifier.fillMaxWidth(),
          maxLines = 3,
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onSubmit(selectedRating, comment) },
        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = ParchmentCream),
      ) {
        Text(strings.save)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(strings.cancel)
      }
    },
  )
}
