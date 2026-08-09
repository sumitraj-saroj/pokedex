package com.dexter.app.ui.quiz

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizAudioPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var exoPlayer: ExoPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var simulationJob: Job? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED || playbackState == Player.STATE_IDLE) {
                _isPlaying.value = false
            }
        }
    }

    fun playCry(audioUrl: String?) {
        simulationJob?.cancel()

        if (audioUrl.isNullOrEmpty()) {
            simulationJob = scope.launch {
                _isPlaying.value = true
                delay(1800)
                _isPlaying.value = false
            }
            return
        }

        try {
            if (exoPlayer == null) {
                exoPlayer = ExoPlayer.Builder(context).build().apply {
                    addListener(playerListener)
                }
            }
            exoPlayer?.let { player ->
                player.stop()
                player.clearMediaItems()
                val mediaItem = MediaItem.fromUri(Uri.parse(audioUrl))
                player.setMediaItem(mediaItem)
                player.prepare()
                player.playWhenReady = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            simulationJob = scope.launch {
                _isPlaying.value = true
                delay(1800)
                _isPlaying.value = false
            }
        }
    }

    fun stop() {
        simulationJob?.cancel()
        try {
            exoPlayer?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _isPlaying.value = false
    }

    fun release() {
        simulationJob?.cancel()
        try {
            exoPlayer?.removeListener(playerListener)
            exoPlayer?.stop()
            exoPlayer?.release()
            exoPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _isPlaying.value = false
    }
}

