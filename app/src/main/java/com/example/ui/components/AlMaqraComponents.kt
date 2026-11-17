package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.BOOTSTRAPPED_ADMIN_EMAIL
import com.example.data.WHATSAPP_SUPPORT_NUMBER
import com.example.data.WHATSAPP_SUPPORT_URI
import com.example.data.model.LoginEvent
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.GoldHighlight
import com.example.ui.theme.LocalAlMaqraColors
import com.example.ui.theme.ParchmentCream

fun emailAdminLoginDigest(context: Context, events: List<LoginEvent>) {
  val summaryLines = if (events.isEmpty()) {
    "No user sign-ins recorded yet."
  } else {
    events.take(30).joinToString("\n") { ev ->
      "• ${ev.userName} (${ev.userEmail.ifBlank { "no-email" }}) — ${ev.deviceInfo} [${ev.status}]"
    }
  }
  val uri = Uri.parse(
    "mailto:$BOOTSTRAPPED_ADMIN_EMAIL?subject=${Uri.encode("AL MAQRA User Login Report")}&body=${Uri.encode("Recent AL MAQRA Sign-In Activity:\n\n$summaryLines")}",
  )
  val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
  }
  try {
    context.startActivity(intent)
  } catch (_: Exception) {
    Toast.makeText(
      context,
      "No email client installed. Send digest to $BOOTSTRAPPED_ADMIN_EMAIL",
      Toast.LENGTH_LONG,
    ).show()
  }
}

fun openWhatsAppSupport(context: Context) {
  val uri = Uri.parse(
    "$WHATSAPP_SUPPORT_URI?text=${Uri.encode("Assalamu Alaikum AL MAQRA team, I would like assistance.")}",
  )
  val intent = Intent(Intent.ACTION_VIEW, uri).apply {
    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
  }
  try {
    context.startActivity(intent)
  } catch (_: Exception) {
    Toast.makeText(
      context,
      "WhatsApp / browser not found. Contact +91 $WHATSAPP_SUPPORT_NUMBER",
      Toast.LENGTH_LONG,
    ).show()
  }
}

@Composable
fun AlMaqraTopBar(
  title: String,
  showBackButton: Boolean = false,
  onMenuOrBackClick: () -> Unit,
  showFilterAction: Boolean = false,
  filterLabel: String = "Filter",
  unreadCount: Int = 0,
  onActionClick: () -> Unit,
) {
  val ext = LocalAlMaqraColors.current
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .background(ext.headerBackground),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .height(60.dp)
        .padding(horizontal = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      IconButton(
        onClick = onMenuOrBackClick,
        modifier = Modifier.testTag(if (showBackButton) "topbar_back" else "topbar_menu"),
      ) {
        Icon(
          imageVector = if (showBackButton) Icons.AutoMirrored.Filled.ArrowBack else Icons.Filled.Menu,
          contentDescription = if (showBackButton) "Back" else "Open navigation drawer",
          tint = ext.headerContent,
        )
      }

      Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = ext.headerContent,
        textAlign = TextAlign.Center,
        modifier = Modifier.weight(1f),
      )

      if (showFilterAction) {
        Surface(
          onClick = onActionClick,
          shape = RoundedCornerShape(10.dp),
          color = Color.White.copy(alpha = 0.16f),
          modifier = Modifier
            .padding(end = 4.dp)
            .testTag("topbar_filter"),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              imageVector = Icons.Filled.FilterAlt,
              contentDescription = filterLabel,
              tint = ext.headerContent,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = filterLabel,
              style = MaterialTheme.typography.labelLarge,
              color = ext.headerContent,
            )
          }
        }
      } else {
        IconButton(
          onClick = onActionClick,
          modifier = Modifier.testTag("topbar_notifications"),
        ) {
          Box(contentAlignment = Alignment.TopEnd) {
            Icon(
              imageVector = Icons.Outlined.Notifications,
              contentDescription = "Notifications",
              tint = ext.headerContent,
            )
            if (unreadCount > 0) {
              Surface(
                shape = CircleShape,
                color = ext.liveDot,
                modifier = Modifier.size(16.dp),
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(
                    text = if (unreadCount > 9) "9+" else "$unreadCount",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
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

/**
 * Standardized EmptyStateCard matching the screenshots:
 * rounded 16dp card, flat geometric vector illustration (mosque dome, open notebook/Mushaf,
 * microphone badge, 3-dot speech bubble, and audio waveform), centered message, optional primary CTA.
 */
@Composable
fun EmptyStateCard(
  message: String,
  ctaLabel: String? = null,
  onCtaClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  val ext = LocalAlMaqraColors.current
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    color = ext.emptyCardBg,
    border = BorderStroke(1.dp, ext.cardBorder),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 28.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      EmptyStateIllustration(modifier = Modifier.size(148.dp))
      Spacer(modifier = Modifier.height(14.dp))
      Text(
        text = message,
        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
      )
      if (ctaLabel != null && onCtaClick != null) {
        Spacer(modifier = Modifier.height(18.dp))
        Button(
          onClick = onCtaClick,
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = ForestGreen,
            contentColor = ParchmentCream,
          ),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("empty_state_cta"),
        ) {
          Text(
            text = ctaLabel,
            style = MaterialTheme.typography.labelLarge,
          )
        }
      }
    }
  }
}

@Composable
private fun EmptyStateIllustration(modifier: Modifier = Modifier) {
  val ext = LocalAlMaqraColors.current
  val domeColor = if (ext.isDark) Color(0xFF568371) else Color(0xFF477862)
  val notebookBg = if (ext.isDark) Color(0xFFAEC7BC) else Color(0xFFBFD4CA)
  val notebookSpine = if (ext.isDark) Color(0xFF7B9E8E) else Color(0xFF8AAFA0)
  val creamHighlight = ParchmentCream
  val goldColor = GoldHighlight
  val darkInk = ForestGreen

  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // 1. Mosque dome silhouette behind the Mushaf/notebook
    val domePath = Path().apply {
      moveTo(w * 0.14f, h * 0.78f)
      lineTo(w * 0.14f, h * 0.36f)
      cubicTo(w * 0.14f, h * 0.18f, w * 0.32f, h * 0.14f, w * 0.36f, h * 0.08f)
      cubicTo(w * 0.40f, h * 0.14f, w * 0.56f, h * 0.18f, w * 0.56f, h * 0.36f)
      lineTo(w * 0.56f, h * 0.78f)
      close()
    }
    drawPath(path = domePath, color = domeColor)

    // 2. Mushaf / Notebook body
    val bookLeft = w * 0.31f
    val bookTop = h * 0.24f
    val bookW = w * 0.52f
    val bookH = h * 0.58f
    drawRoundRect(
      color = notebookBg,
      topLeft = Offset(bookLeft, bookTop),
      size = Size(bookW, bookH),
      cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
    )
    // Notebook right edge spine
    drawRoundRect(
      color = notebookSpine,
      topLeft = Offset(bookLeft + bookW - w * 0.07f, bookTop),
      size = Size(w * 0.07f, bookH),
      cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
    )
    // Spiral notches on right
    for (i in 0 until 5) {
      val ry = bookTop + bookH * (0.18f + i * 0.14f)
      drawLine(
        color = darkInk,
        start = Offset(bookLeft + bookW - w * 0.06f, ry),
        end = Offset(bookLeft + bookW + w * 0.02f, ry),
        strokeWidth = 2.dp.toPx(),
        cap = StrokeCap.Round,
      )
    }
    // Header cream rectangle inside book
    drawRoundRect(
      color = creamHighlight,
      topLeft = Offset(bookLeft + bookW * 0.36f, bookTop + bookH * 0.12f),
      size = Size(bookW * 0.42f, bookH * 0.16f),
      cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
    )
    // Text lines inside book
    for (i in 0 until 3) {
      val ly = bookTop + bookH * (0.58f + i * 0.10f)
      drawLine(
        color = creamHighlight,
        start = Offset(bookLeft + bookW * 0.16f, ly),
        end = Offset(bookLeft + bookW * 0.56f, ly),
        strokeWidth = 2.dp.toPx(),
        cap = StrokeCap.Round,
      )
    }

    // 3. Golden 3-dot speech bubble
    val bubbleLeft = w * 0.19f
    val bubbleTop = h * 0.28f
    val bubbleW = w * 0.30f
    val bubbleH = h * 0.17f
    drawRoundRect(
      color = goldColor,
      topLeft = Offset(bubbleLeft, bubbleTop),
      size = Size(bubbleW, bubbleH),
      cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx()),
    )
    for (i in 0 until 3) {
      drawCircle(
        color = Color(0xFF1D2226),
        radius = 2.8.dp.toPx(),
        center = Offset(bubbleLeft + bubbleW * (0.25f + i * 0.25f), bubbleTop + bubbleH * 0.5f),
      )
    }

    // 4. Circular Microphone Badge bottom-left
    val micCenter = Offset(w * 0.24f, h * 0.76f)
    val micRadius = w * 0.12f
    drawCircle(
      color = goldColor,
      radius = micRadius + 2.dp.toPx(),
      center = micCenter,
      style = Stroke(width = 2.dp.toPx()),
    )
    drawCircle(
      color = Color.White,
      radius = micRadius,
      center = micCenter,
    )
    // Mic capsule
    drawRoundRect(
      color = domeColor,
      topLeft = Offset(micCenter.x - 3.5.dp.toPx(), micCenter.y - 7.5.dp.toPx()),
      size = Size(7.dp.toPx(), 10.dp.toPx()),
      cornerRadius = CornerRadius(3.5.dp.toPx(), 3.5.dp.toPx()),
    )
    drawArc(
      color = domeColor,
      startAngle = 0f,
      sweepAngle = 180f,
      useCenter = false,
      topLeft = Offset(micCenter.x - 6.dp.toPx(), micCenter.y - 2.dp.toPx()),
      size = Size(12.dp.toPx(), 8.dp.toPx()),
      style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round),
    )
    drawLine(
      color = domeColor,
      start = Offset(micCenter.x, micCenter.y + 6.dp.toPx()),
      end = Offset(micCenter.x, micCenter.y + 9.5.dp.toPx()),
      strokeWidth = 1.8.dp.toPx(),
      cap = StrokeCap.Round,
    )

    // 5. Golden Audio Waveform Pill bottom-right
    val waveLeft = w * 0.61f
    val waveTop = h * 0.62f
    val waveW = w * 0.30f
    val waveH = h * 0.17f
    drawRoundRect(
      color = goldColor,
      topLeft = Offset(waveLeft, waveTop),
      size = Size(waveW, waveH),
      cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx()),
    )
    val barHeights = listOf(0.28f, 0.56f, 0.35f, 0.72f, 0.45f, 0.65f, 0.32f, 0.52f)
    barHeights.forEachIndexed { idx, ratio ->
      val bx = waveLeft + waveW * (0.14f + idx * 0.105f)
      val bh = waveH * ratio
      drawLine(
        color = Color(0xFF1D2226),
        start = Offset(bx, waveTop + (waveH - bh) / 2f),
        end = Offset(bx, waveTop + (waveH + bh) / 2f),
        strokeWidth = 1.6.dp.toPx(),
        cap = StrokeCap.Round,
      )
    }
  }
}

@Composable
fun PulsingLiveDot(
  label: String,
  modifier: Modifier = Modifier,
) {
  val ext = LocalAlMaqraColors.current
  val transition = rememberInfiniteTransition(label = "live_pulse")
  val alpha by transition.animateFloat(
    initialValue = 1f,
    targetValue = 0.25f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 800),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "pulse_alpha",
  )

  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier = Modifier
        .size(9.dp)
        .alpha(alpha)
        .clip(CircleShape)
        .background(ext.liveDot),
    )
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
      color = ext.liveDot,
    )
  }
}

@Composable
fun SheikhAvatarBadge(
  initial: String,
  modifier: Modifier = Modifier,
  size: Dp = 42.dp,
) {
  Surface(
    modifier = modifier.size(size),
    shape = CircleShape,
    color = ForestGreen,
  ) {
    Box(contentAlignment = Alignment.Center) {
      Text(
        text = initial,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = ParchmentCream,
      )
    }
  }
}

@Composable
fun SegmentedPillToggle(
  options: List<String>,
  selectedIndex: Int,
  onSelect: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  val ext = LocalAlMaqraColors.current
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    color = ext.segmentedBg,
    border = BorderStroke(1.dp, ext.cardBorder),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(4.dp),
      horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      options.forEachIndexed { index, title ->
        val selected = index == selectedIndex
        Surface(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onSelect(index) }
            .testTag("segment_${index}"),
          shape = RoundedCornerShape(10.dp),
          color = if (selected) ext.segmentedSelectedBg else Color.Transparent,
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 10.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = title,
              style = MaterialTheme.typography.labelLarge,
              color = if (selected) ext.segmentedSelectedText else ext.mutedLabel,
              maxLines = 1,
            )
          }
        }
      }
    }
  }
}

@Composable
fun QrCodeGraphic(
  modifier: Modifier = Modifier,
  foreground: Color = ForestGreen,
  background: Color = Color.White,
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(10.dp),
    color = background,
    border = BorderStroke(1.dp, foreground.copy(alpha = 0.2f)),
  ) {
    Canvas(
      modifier = Modifier
        .fillMaxSize()
        .padding(8.dp),
    ) {
      val grid = 9
      val cell = size.minDimension / grid
      val matrix = arrayOf(
        intArrayOf(1, 1, 1, 0, 1, 0, 1, 1, 1),
        intArrayOf(1, 0, 1, 0, 0, 1, 1, 0, 1),
        intArrayOf(1, 1, 1, 0, 1, 0, 1, 1, 1),
        intArrayOf(0, 0, 0, 1, 0, 1, 0, 0, 0),
        intArrayOf(1, 0, 1, 1, 1, 0, 1, 1, 0),
        intArrayOf(0, 1, 0, 0, 1, 1, 0, 0, 1),
        intArrayOf(1, 1, 1, 0, 1, 0, 1, 0, 1),
        intArrayOf(1, 0, 1, 0, 0, 1, 0, 1, 0),
        intArrayOf(1, 1, 1, 0, 1, 1, 1, 0, 1),
      )
      for (r in 0 until grid) {
        for (c in 0 until grid) {
          if (matrix[r][c] == 1) {
            drawRect(
              color = foreground,
              topLeft = Offset(c * cell, r * cell),
              size = Size(cell * 0.9f, cell * 0.9f),
            )
          }
        }
      }
    }
  }
}
