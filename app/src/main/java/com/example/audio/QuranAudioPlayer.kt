package com.example.audio

import android.media.AudioAttributes
import android.media.MediaPlayer
import com.example.data.model.QuranAyah
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class QuranPlaybackState(
  val isPlaying: Boolean = false,
  val isBuffering: Boolean = false,
  val playingSurahNumber: Int? = null,
  val playingAyahNumberInSurah: Int? = null,
  val playingGlobalAyahNumber: Int? = null,
  val reciterId: String = "ar.alafasy",
  val autoAdvance: Boolean = true,
  val errorMessage: String? = null,
)

class QuranAudioPlayer {
  private var mediaPlayer: MediaPlayer? = null
  private var currentAyahList: List<QuranAyah> = emptyList()
  private var currentIndex: Int = -1

  private val _state = MutableStateFlow(QuranPlaybackState())
  val state: StateFlow<QuranPlaybackState> = _state.asStateFlow()

  fun setReciter(reciterId: String) {
    val wasPlaying = _state.value.isPlaying || _state.value.isBuffering
    val surahNum = _state.value.playingSurahNumber
    _state.value = _state.value.copy(reciterId = reciterId, errorMessage = null)
    if (wasPlaying && surahNum != null && currentIndex in currentAyahList.indices) {
      playAyahInternal(surahNum, currentAyahList, currentIndex, _state.value.autoAdvance)
    }
  }

  fun playSurahFromStart(surahNumber: Int, ayahs: List<QuranAyah>) {
    if (ayahs.isEmpty()) return
    playAyahInternal(surahNumber, ayahs, 0, autoAdvance = true)
  }

  fun playSingleOrResumeAyah(surahNumber: Int, ayahs: List<QuranAyah>, index: Int) {
    if (index !in ayahs.indices) return
    val target = ayahs[index]
    val cur = _state.value
    if (cur.playingGlobalAyahNumber == target.globalNumber && mediaPlayer != null) {
      try {
        if (cur.isPlaying) {
          mediaPlayer?.pause()
          _state.value = cur.copy(isPlaying = false, isBuffering = false)
          return
        } else if (!cur.isBuffering) {
          mediaPlayer?.start()
          _state.value = cur.copy(isPlaying = true)
          return
        }
      } catch (_: Exception) {
        // Re-prepare below
      }
    }
    playAyahInternal(surahNumber, ayahs, index, autoAdvance = true)
  }

  fun togglePlayPause() {
    val cur = _state.value
    val player = mediaPlayer ?: return
    try {
      if (cur.isPlaying) {
        player.pause()
        _state.value = cur.copy(isPlaying = false)
      } else if (!cur.isBuffering) {
        player.start()
        _state.value = cur.copy(isPlaying = true)
      }
    } catch (_: Exception) {
      stop()
    }
  }

  private fun playAyahInternal(
    surahNumber: Int,
    ayahs: List<QuranAyah>,
    index: Int,
    autoAdvance: Boolean,
  ) {
    releasePlayerOnly()
    currentAyahList = ayahs
    currentIndex = index
    val ayah = ayahs[index]
    val reciter = _state.value.reciterId
    val url = "https://cdn.islamic.network/quran/audio/128/$reciter/${ayah.globalNumber}.mp3"

    _state.value = _state.value.copy(
      isPlaying = false,
      isBuffering = true,
      playingSurahNumber = surahNumber,
      playingAyahNumberInSurah = ayah.numberInSurah,
      playingGlobalAyahNumber = ayah.globalNumber,
      autoAdvance = autoAdvance,
      errorMessage = null,
    )

    try {
      val player = MediaPlayer().apply {
        setAudioAttributes(
          AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build(),
        )
        setDataSource(url)
        setOnPreparedListener { mp ->
          mp.start()
          _state.value = _state.value.copy(
            isPlaying = true,
            isBuffering = false,
            errorMessage = null,
          )
        }
        setOnCompletionListener {
          val nextIdx = currentIndex + 1
          if (_state.value.autoAdvance && nextIdx in currentAyahList.indices) {
            playAyahInternal(surahNumber, currentAyahList, nextIdx, autoAdvance = true)
          } else {
            _state.value = _state.value.copy(
              isPlaying = false,
              isBuffering = false,
              playingAyahNumberInSurah = null,
              playingGlobalAyahNumber = null,
            )
          }
        }
        setOnErrorListener { _, _, _ ->
          _state.value = _state.value.copy(
            isPlaying = false,
            isBuffering = false,
            errorMessage = "Audio stream unavailable. Check connection and tap Listen again.",
          )
          true
        }
        prepareAsync()
      }
      mediaPlayer = player
    } catch (e: Exception) {
      _state.value = _state.value.copy(
        isPlaying = false,
        isBuffering = false,
        errorMessage = e.localizedMessage ?: "Unable to start audio playback.",
      )
    }
  }

  private fun releasePlayerOnly() {
    try {
      mediaPlayer?.reset()
    } catch (_: Exception) {}
    try {
      mediaPlayer?.release()
    } catch (_: Exception) {}
    mediaPlayer = null
  }

  fun stop() {
    releasePlayerOnly()
    currentAyahList = emptyList()
    currentIndex = -1
    _state.value = QuranPlaybackState(reciterId = _state.value.reciterId)
  }
}
