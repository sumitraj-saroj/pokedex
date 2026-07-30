package com.dexter.app.ui.quiz

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizAudioPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var exoPlayer: ExoPlayer? = null

    fun playCry(audioUrl: String?) {
        if (audioUrl.isNullOrEmpty()) return

        try {
            if (exoPlayer == null) {
                exoPlayer = ExoPlayer.Builder(context).build()
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
        }
    }

    fun release() {
        try {
            exoPlayer?.stop()
            exoPlayer?.release()
            exoPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
