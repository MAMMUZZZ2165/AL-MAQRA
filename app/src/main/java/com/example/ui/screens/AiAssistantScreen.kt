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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AiChatMessage
import com.example.ui.AiUiState
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.GoldHighlight
import com.example.ui.theme.LocalAlMaqraColors
import com.example.ui.theme.ParchmentCream
import com.example.util.LocalAlMaqraStrings

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiAssistantScreen(
  messages: List<AiChatMessage>,
  uiState: AiUiState,
  onSendPrompt: (String) -> Unit,
  onClearChat: () -> Unit,
  onDismissError: () -> Unit,
) {
  val strings = LocalAlMaqraStrings.current
  val ext = LocalAlMaqraColors.current
  var input by remember { mutableStateOf("") }
  val listState = rememberLazyListState()
  val isBusy = uiState is AiUiState.Loading || uiState is AiUiState.Streaming

  LaunchedEffect(messages.size, messages.lastOrNull()?.text?.length) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.lastIndex)
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .navigationBarsPadding()
      .imePadding(),
  ) {
    // Header Strip
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = MaterialTheme.colorScheme.surface,
      border = BorderStroke(1.dp, ext.cardBorder),
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Surface(
            shape = CircleShape,
            color = ForestGreen,
            modifier = Modifier.size(38.dp),
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = strings.aiAssistant,
                tint = GoldHighlight,
                modifier = Modifier.size(20.dp),
              )
            }
          }
          Spacer(Modifier.width(12.dp))
          Column {
            Text(
              text = strings.aiAssistant,
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = strings.aiAssistantSubtitle,
              style = MaterialTheme.typography.bodySmall,
              color = ext.mutedLabel,
            )
          }
        }
        if (messages.isNotEmpty()) {
          IconButton(
            onClick = onClearChat,
            modifier = Modifier.testTag("ai_clear_chat"),
          ) {
            Icon(
              imageVector = Icons.Filled.DeleteSweep,
              contentDescription = "Clear conversation",
              tint = ext.mutedLabel,
            )
          }
        }
      }
    }

    // Error Banner (surfaces real Firebase AI / App Check / network errors)
    if (uiState is AiUiState.Error) {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f)),
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(
            imageVector = Icons.Filled.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
          )
          Spacer(Modifier.width(10.dp))
          Text(
            text = uiState.message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f),
          )
          TextButton(onClick = onDismissError) {
            Text(strings.close)
          }
        }
      }
    }

    // Chat Thread or Starter Suggestions
    LazyColumn(
      state = listState,
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      if (messages.isEmpty()) {
        item {
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = ext.cardBannerBg,
            border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.14f)),
          ) {
            Column(modifier = Modifier.padding(18.dp)) {
              Text(
                text = "Assalamu Alaikum! Ask your Quran study assistant",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = ForestGreen,
              )
              Spacer(Modifier.height(6.dp))
              Text(
                text = "Get help with Tajweed rules, Makharij al-Huruf, daily Hifz schedules, and Surah summaries in English, Arabic, or Urdu.",
                style = MaterialTheme.typography.bodySmall,
                color = ForestGreen.copy(alpha = 0.8f),
              )
              Spacer(Modifier.height(14.dp))
              val starters = listOf(
                "Explain Noon Sakinah & Tanween rules simply",
                "Build a 30-day Juz Amma Hifz & revision plan",
                "Difference between Idgham and Ikhfa with examples",
                "Key themes of Surah Al-Mulk (Juz 29)",
              )
              FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                starters.forEachIndexed { idx, prompt ->
                  AssistChip(
                    onClick = { if (!isBusy) onSendPrompt(prompt) },
                    label = { Text(prompt, style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.testTag("ai_starter_$idx"),
                  )
                }
              }
            }
          }
        }
      } else {
        items(messages, key = { it.id }) { msg ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (msg.isFromUser) Arrangement.End else Arrangement.Start,
          ) {
            Surface(
              modifier = Modifier.widthIn(max = 310.dp),
              shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (msg.isFromUser) 16.dp else 4.dp,
                bottomEnd = if (msg.isFromUser) 4.dp else 16.dp,
              ),
              color = if (msg.isFromUser) ForestGreen else MaterialTheme.colorScheme.surface,
              border = if (msg.isFromUser) null else BorderStroke(1.dp, ext.cardBorder),
            ) {
              Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                if (!msg.isFromUser && msg.text.isBlank() && msg.isStreaming) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                      modifier = Modifier.size(16.dp),
                      strokeWidth = 2.dp,
                      color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                      text = "Thinking...",
                      style = MaterialTheme.typography.bodySmall,
                      color = ext.mutedLabel,
                    )
                  }
                } else {
                  Text(
                    text = msg.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (msg.isFromUser) ParchmentCream else MaterialTheme.colorScheme.onSurface,
                  )
                }
              }
            }
          }
        }
      }
    }

    // Input Bar
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = MaterialTheme.colorScheme.surface,
      border = BorderStroke(1.dp, ext.cardBorder),
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        OutlinedTextField(
          value = input,
          onValueChange = { input = it },
          placeholder = { Text("Ask about Tajweed, Hifz, or any Surah...") },
          modifier = Modifier
            .weight(1f)
            .testTag("ai_prompt_input"),
          maxLines = 3,
          enabled = !isBusy,
        )
        Spacer(Modifier.width(8.dp))
        IconButton(
          onClick = {
            if (input.isNotBlank() && !isBusy) {
              val prompt = input
              input = ""
              onSendPrompt(prompt)
            }
          },
          enabled = input.isNotBlank() && !isBusy,
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (input.isNotBlank() && !isBusy) ForestGreen else ext.mutedLabel.copy(alpha = 0.25f))
            .testTag("ai_send_button"),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = "Send prompt",
            tint = ParchmentCream,
          )
        }
      }
    }
  }
}
