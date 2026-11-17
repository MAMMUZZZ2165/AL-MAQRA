package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.QuranAudioPlayer
import com.example.data.QuranIndexData
import com.example.data.model.QuranAyah
import com.example.data.model.QuranSurahMeta
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.GoldHighlight
import com.example.ui.theme.LocalAlMaqraColors
import com.example.ui.theme.ParchmentCream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

private val memSurahCache = mutableMapOf<Int, List<QuranAyah>>()

@Composable
fun QuranScreen() {
  val context = LocalContext.current
  val ext = LocalAlMaqraColors.current
  val audioPlayer = remember { QuranAudioPlayer() }
  val playbackState by audioPlayer.state.collectAsStateWithLifecycle()

  var selectedSurah by remember { mutableStateOf<QuranSurahMeta?>(null) }
  var searchQuery by remember { mutableStateOf("") }
  var selectedRevFilter by remember { mutableStateOf("All") }

  DisposableEffect(Unit) {
    onDispose {
      audioPlayer.stop()
    }
  }

  if (selectedSurah != null) {
    BackHandler {
      audioPlayer.stop()
      selectedSurah = null
    }
    SurahReaderView(
      context = context,
      surah = selectedSurah!!,
      audioPlayer = audioPlayer,
      onBackToIndex = {
        audioPlayer.stop()
        selectedSurah = null
      },
      onSwitchSurah = { nextMeta ->
        audioPlayer.stop()
        selectedSurah = nextMeta
      },
    )
    return
  }

  val filteredSurahs = remember(searchQuery, selectedRevFilter) {
    val q = searchQuery.trim().lowercase()
    QuranIndexData.allSurahs.filter { s ->
      val matchesRev = selectedRevFilter == "All" || s.revelationType.equals(selectedRevFilter, ignoreCase = true)
      val matchesQuery = q.isEmpty() ||
        s.englishName.lowercase().contains(q) ||
        s.englishTranslation.lowercase().contains(q) ||
        s.arabicName.contains(q) ||
        s.number.toString() == q
      matchesRev && matchesQuery
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
  ) {
    // Header summary & Audio Reciter Bar
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      shape = RoundedCornerShape(16.dp),
      color = ForestGreen,
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "القرآن الكريم",
              fontFamily = AmiriFontFamily,
              style = MaterialTheme.typography.headlineSmall,
              color = ParchmentCream,
            )
            Text(
              text = "All 114 Surahs • Uthmani Script, Translation & Audio",
              style = MaterialTheme.typography.bodySmall,
              color = ParchmentCream.copy(alpha = 0.85f),
            )
          }
          Icon(
            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
            contentDescription = "Audio Enabled",
            tint = GoldHighlight,
            modifier = Modifier.size(28.dp),
          )
        }
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Search Surah by name, meaning, or number (1-114)") },
          leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { searchQuery = "" }) {
                Icon(Icons.Filled.Close, contentDescription = "Clear search")
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .testTag("quran_search_input"),
        )
      }
    }

    // Revelation filter chips
    LazyRow(
      contentPadding = PaddingValues(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      items(listOf("All", "Meccan", "Medinan")) { filter ->
        FilterChip(
          selected = selectedRevFilter == filter,
          onClick = { selectedRevFilter = filter },
          label = { Text(filter) },
        )
      }
    }

    Spacer(Modifier.height(6.dp))

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      items(filteredSurahs, key = { it.number }) { surah ->
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { selectedSurah = surah }
            .testTag("surah_item_${surah.number}"),
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, ext.cardBorder),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Surface(
              shape = CircleShape,
              color = if (playbackState.playingSurahNumber == surah.number) ForestGreen else ext.cardBannerBg,
              modifier = Modifier.size(44.dp),
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text(
                  text = "${surah.number}",
                  style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                  color = if (playbackState.playingSurahNumber == surah.number) ParchmentCream else ForestGreen,
                )
              }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = surah.englishName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = "${surah.englishTranslation} • ${surah.ayahCount} Ayahs • ${surah.revelationType}",
                style = MaterialTheme.typography.bodySmall,
                color = ext.mutedLabel,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
            Spacer(Modifier.width(8.dp))
            Text(
              text = surah.arabicName,
              fontFamily = AmiriFontFamily,
              fontSize = 22.sp,
              color = MaterialTheme.colorScheme.primary,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun SurahReaderView(
  context: Context,
  surah: QuranSurahMeta,
  audioPlayer: QuranAudioPlayer,
  onBackToIndex: () -> Unit,
  onSwitchSurah: (QuranSurahMeta) -> Unit,
) {
  val ext = LocalAlMaqraColors.current
  val scope = rememberCoroutineScope()
  val playbackState by audioPlayer.state.collectAsStateWithLifecycle()
  val listState = rememberLazyListState()

  var ayahs by remember(surah.number) { mutableStateOf<List<QuranAyah>>(emptyList()) }
  var isLoading by remember(surah.number) { mutableStateOf(true) }
  var errorMsg by remember(surah.number) { mutableStateOf<String?>(null) }
  var reloadTrigger by remember { mutableIntStateOf(0) }

  LaunchedEffect(surah.number, reloadTrigger) {
    isLoading = true
    errorMsg = null
    val result = loadSurahWithCache(context, surah.number)
    result
      .onSuccess {
        ayahs = it
        isLoading = false
      }
      .onFailure { err ->
        errorMsg = err.localizedMessage ?: "Unable to load Surah text. Check your internet connection."
        isLoading = false
      }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
  ) {
    // Top Surah Control Bar
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = ext.cardBannerBg,
      border = BorderStroke(1.dp, ext.cardBorder),
    ) {
      Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            IconButton(
              onClick = onBackToIndex,
              modifier = Modifier.size(40.dp).testTag("quran_back_to_list"),
            ) {
              Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "All Surahs", tint = ForestGreen)
            }
            Spacer(Modifier.width(4.dp))
            Column {
              Text(
                text = "${surah.number}. ${surah.englishName} (${surah.arabicName})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = ForestGreen,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Text(
                text = "${surah.englishTranslation} • ${surah.ayahCount} Ayahs",
                style = MaterialTheme.typography.labelSmall,
                color = ForestGreen.copy(alpha = 0.78f),
              )
            }
          }

          if (ayahs.isNotEmpty()) {
            val isThisSurahPlaying = playbackState.playingSurahNumber == surah.number &&
              (playbackState.isPlaying || playbackState.isBuffering)
            Button(
              onClick = {
                if (isThisSurahPlaying) {
                  audioPlayer.togglePlayPause()
                } else {
                  audioPlayer.playSurahFromStart(surah.number, ayahs)
                }
              },
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = ForestGreen,
                contentColor = ParchmentCream,
              ),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
              modifier = Modifier.testTag("quran_listen_surah"),
            ) {
              if (playbackState.isBuffering && playbackState.playingSurahNumber == surah.number) {
                CircularProgressIndicator(
                  modifier = Modifier.size(16.dp),
                  color = ParchmentCream,
                  strokeWidth = 2.dp,
                )
              } else {
                Icon(
                  imageVector = if (playbackState.isPlaying && playbackState.playingSurahNumber == surah.number) {
                    Icons.Filled.Pause
                  } else {
                    Icons.Filled.PlayArrow
                  },
                  contentDescription = "Listen",
                  modifier = Modifier.size(18.dp),
                )
              }
              Spacer(Modifier.width(4.dp))
              Text(
                text = if (playbackState.isPlaying && playbackState.playingSurahNumber == surah.number) "Pause" else "Listen",
                style = MaterialTheme.typography.labelLarge,
              )
            }
          }
        }

        Spacer(Modifier.height(8.dp))
        // Reciter Selector
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          items(QuranIndexData.audioReciters, key = { it.id }) { rec ->
            FilterChip(
              selected = playbackState.reciterId == rec.id,
              onClick = { audioPlayer.setReciter(rec.id) },
              label = { Text(rec.name, style = MaterialTheme.typography.labelMedium) },
            )
          }
        }

        AnimatedVisibility(
          visible = playbackState.playingSurahNumber == surah.number &&
            (playbackState.playingAyahNumberInSurah != null || playbackState.errorMessage != null),
          enter = fadeIn(),
          exit = fadeOut(),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text(
              text = playbackState.errorMessage
                ?: "Reciting Ayah ${playbackState.playingAyahNumberInSurah ?: 1} of ${surah.ayahCount}",
              style = MaterialTheme.typography.labelMedium,
              color = if (playbackState.errorMessage != null) MaterialTheme.colorScheme.error else ForestGreen,
              modifier = Modifier.weight(1f),
            )
            IconButton(
              onClick = { audioPlayer.stop() },
              modifier = Modifier.size(32.dp),
            ) {
              Icon(Icons.Filled.Stop, contentDescription = "Stop audio", tint = ForestGreen, modifier = Modifier.size(18.dp))
            }
          }
        }
      }
    }

    when {
      isLoading -> {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = ForestGreen)
            Spacer(Modifier.height(12.dp))
            Text("Loading Surah ${surah.englishName}...", style = MaterialTheme.typography.bodyMedium)
          }
        }
      }

      errorMsg != null -> {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
          Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(errorMsg!!, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
            Button(
              onClick = { reloadTrigger++ },
              colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = ParchmentCream),
            ) {
              Icon(Icons.Filled.Refresh, contentDescription = null)
              Spacer(Modifier.width(6.dp))
              Text("Retry")
            }
          }
        }
      }

      else -> {
        LazyColumn(
          state = listState,
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          if (surah.number != 1 && surah.number != 9) {
            item {
              Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = ForestGreen,
              ) {
                Text(
                  text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                  fontFamily = AmiriFontFamily,
                  fontSize = 24.sp,
                  color = ParchmentCream,
                  textAlign = TextAlign.Center,
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp, horizontal = 12.dp),
                )
              }
            }
          }

          itemsIndexed(ayahs, key = { _, ayah -> ayah.globalNumber }) { index, ayah ->
            val isThisAyahPlaying = playbackState.playingGlobalAyahNumber == ayah.globalNumber &&
              (playbackState.isPlaying || playbackState.isBuffering)
            val cardColor by animateColorAsState(
              targetValue = if (isThisAyahPlaying) {
                ext.cardBannerBg
              } else {
                MaterialTheme.colorScheme.surface
              },
              label = "ayah_highlight",
            )

            Surface(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(16.dp),
              color = cardColor,
              border = BorderStroke(
                width = if (isThisAyahPlaying) 1.5.dp else 1.dp,
                color = if (isThisAyahPlaying) ForestGreen else ext.cardBorder,
              ),
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Surface(
                    shape = CircleShape,
                    color = if (isThisAyahPlaying) ForestGreen else MaterialTheme.colorScheme.surfaceVariant,
                  ) {
                    Text(
                      text = "${surah.number}:${ayah.numberInSurah}",
                      style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                      color = if (isThisAyahPlaying) ParchmentCream else MaterialTheme.colorScheme.primary,
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                  }

                  OutlinedButton(
                    onClick = {
                      audioPlayer.playSingleOrResumeAyah(surah.number, ayahs, index)
                      scope.launch { listState.animateScrollToItem(index) }
                    },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp).testTag("listen_ayah_${ayah.numberInSurah}"),
                  ) {
                    Icon(
                      imageVector = if (isThisAyahPlaying && playbackState.isPlaying) Icons.Filled.Pause else Icons.AutoMirrored.Filled.VolumeUp,
                      contentDescription = "Listen Ayah ${ayah.numberInSurah}",
                      modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                      text = if (isThisAyahPlaying && playbackState.isPlaying) "Pause" else "Listen",
                      style = MaterialTheme.typography.labelMedium,
                    )
                  }
                }

                Spacer(Modifier.height(12.dp))
                Text(
                  text = ayah.arabicText,
                  fontFamily = AmiriFontFamily,
                  fontSize = 24.sp,
                  lineHeight = 40.sp,
                  textAlign = TextAlign.End,
                  color = MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = ext.cardBorder)
                Spacer(Modifier.height(8.dp))
                Text(
                  text = ayah.englishTranslation,
                  style = MaterialTheme.typography.bodyMedium,
                  color = ext.mutedLabel,
                )
              }
            }
          }

          // Prev / Next Surah Footer Navigation
          item {
            Row(
              modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              val prevSurah = QuranIndexData.allSurahs.getOrNull(surah.number - 2)
              val nextSurah = QuranIndexData.allSurahs.getOrNull(surah.number)
              if (prevSurah != null) {
                OutlinedButton(
                  onClick = { onSwitchSurah(prevSurah) },
                  modifier = Modifier.weight(1f),
                  shape = RoundedCornerShape(12.dp),
                ) {
                  Text("Prev: ${prevSurah.englishName}", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
              }
              if (nextSurah != null) {
                Button(
                  onClick = { onSwitchSurah(nextSurah) },
                  modifier = Modifier.weight(1f),
                  shape = RoundedCornerShape(12.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = ParchmentCream),
                ) {
                  Text("Next: ${nextSurah.englishName}", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
              }
            }
          }
        }
      }
    }
  }
}

private suspend fun loadSurahWithCache(
  context: Context,
  surahNumber: Int,
): Result<List<QuranAyah>> = withContext(Dispatchers.IO) {
  memSurahCache[surahNumber]?.let { return@withContext Result.success(it) }

  val cacheDir = File(context.cacheDir, "quran_cache").apply { mkdirs() }
  val cacheFile = File(cacheDir, "surah_$surahNumber.json")

  val rawJson: String = if (cacheFile.exists() && cacheFile.length() > 100L) {
    cacheFile.readText()
  } else {
    try {
      val url = URL("https://api.alquran.cloud/v1/surah/$surahNumber/editions/quran-uthmani,en.sahih")
      val conn = (url.openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 15_000
        readTimeout = 20_000
      }
      val code = conn.responseCode
      if (code !in 200..299) {
        return@withContext Result.failure(IllegalStateException("Server returned HTTP $code"))
      }
      val text = conn.inputStream.bufferedReader().use { it.readText() }
      cacheFile.writeText(text)
      text
    } catch (e: Exception) {
      return@withContext Result.failure(e)
    }
  }

  return@withContext try {
    val root = JSONObject(rawJson)
    val dataArr = root.getJSONArray("data")
    val arabicAyahs = dataArr.getJSONObject(0).getJSONArray("ayahs")
    val englishAyahs = dataArr.getJSONObject(1).getJSONArray("ayahs")
    val list = buildList {
      for (i in 0 until arabicAyahs.length()) {
        val arObj = arabicAyahs.getJSONObject(i)
        val enObj = englishAyahs.getJSONObject(i)
        add(
          QuranAyah(
            globalNumber = arObj.getInt("number"),
            numberInSurah = arObj.getInt("numberInSurah"),
            arabicText = arObj.getString("text"),
            englishTranslation = enObj.getString("text"),
          ),
        )
      }
    }
    memSurahCache[surahNumber] = list
    Result.success(list)
  } catch (e: Exception) {
    if (cacheFile.exists()) cacheFile.delete()
    Result.failure(e)
  }
}
