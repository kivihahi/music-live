package dev.wafla.musiclive.data

import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object MusicRepository {
    private const val TAG = "MusicLive.Repository"
    private const val TICK_MS = 1_000L

    var nowPlaying by mutableStateOf<NowPlaying?>(null)
        private set

    private val handler = Handler(Looper.getMainLooper())
    private var controller: MediaController? = null
    private var callback: MediaController.Callback? = null
    private var metadata: MediaMetadata? = null
    private var playbackState: PlaybackState? = null
    private var tickerRunning = false

    private val ticker = object : Runnable {
        override fun run() {
            publish()
            if (tickerRunning) {
                handler.postDelayed(this, TICK_MS)
            }
        }
    }

    fun bind(newController: MediaController) {
        if (controller?.sessionToken == newController.sessionToken) {
            controller = newController
            metadata = newController.metadata
            playbackState = newController.playbackState
            publish()
            return
        }

        unbind(clearState = false)
        controller = newController
        metadata = newController.metadata
        playbackState = newController.playbackState

        val newCallback = object : MediaController.Callback() {
            override fun onMetadataChanged(metadata: MediaMetadata?) {
                this@MusicRepository.metadata = metadata
                publish()
            }

            override fun onPlaybackStateChanged(state: PlaybackState?) {
                this@MusicRepository.playbackState = state
                publish()
            }

            override fun onSessionDestroyed() {
                Log.i(TAG, "Media session destroyed: ${newController.packageName}")
                unbind()
            }
        }
        callback = newCallback
        newController.registerCallback(newCallback, handler)

        if (!tickerRunning) {
            tickerRunning = true
            handler.post(ticker)
        }

        publish()
        Log.i(TAG, "Bound to ${newController.packageName}")
    }

    fun unbind(clearState: Boolean = true) {
        callback?.let { controller?.unregisterCallback(it) }
        callback = null
        controller = null
        metadata = null
        playbackState = null
        tickerRunning = false
        handler.removeCallbacks(ticker)
        if (clearState) {
            nowPlaying = null
        }
        Log.i(TAG, "Unbound media controller")
    }

    fun togglePlayPause() {
        val state = playbackState
        val playing = state?.state == PlaybackState.STATE_PLAYING
        if (playing) {
            controller?.transportControls?.pause()
        } else {
            controller?.transportControls?.play()
        }
        nowPlaying = nowPlaying?.copy(isPlaying = !playing)
    }

    fun skipToNext() {
        controller?.transportControls?.skipToNext()
    }

    fun skipToPrevious() {
        controller?.transportControls?.skipToPrevious()
    }

    fun seekTo(positionMs: Long) {
        val target = if (nowPlaying?.durationMs?.let { it > 0 } == true) {
            positionMs.coerceIn(0L, nowPlaying!!.durationMs)
        } else {
            positionMs.coerceAtLeast(0L)
        }
        controller?.transportControls?.seekTo(target)
        nowPlaying = nowPlaying?.copy(positionMs = target)
    }

    fun perform(action: String) {
        when (action) {
            MediaAction.TOGGLE -> togglePlayPause()
            MediaAction.NEXT -> skipToNext()
            MediaAction.PREVIOUS -> skipToPrevious()
        }
    }

    private fun publish() {
        val current = controller
        if (current == null) {
            nowPlaying = null
            return
        }

        val md = metadata ?: current.metadata
        val state = playbackState ?: current.playbackState
        val duration = md?.getLong(MediaMetadata.METADATA_KEY_DURATION)?.coerceAtLeast(0L) ?: 0L
        val title = md?.getString(MediaMetadata.METADATA_KEY_TITLE)?.takeIf { it.isNotBlank() }
            ?: "Медиасессия активна"
        val artist = md?.getString(MediaMetadata.METADATA_KEY_ARTIST)?.takeIf { it.isNotBlank() }
            ?: md?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)?.takeIf { it.isNotBlank() }
            ?: current.packageName
        val album = md?.getString(MediaMetadata.METADATA_KEY_ALBUM)?.takeIf { it.isNotBlank() }
        val artwork = md?.getBitmap(MediaMetadata.METADATA_KEY_ART)
            ?: md?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
        val actions = state?.actions ?: 0L

        val next = NowPlaying(
            title = title,
            artist = artist,
            album = album,
            artwork = artwork,
            durationMs = duration,
            positionMs = positionFor(state, duration),
            isPlaying = state?.state == PlaybackState.STATE_PLAYING,
            packageName = current.packageName,
            canSeek = actions and PlaybackState.ACTION_SEEK_TO != 0L,
            canSkipNext = actions and PlaybackState.ACTION_SKIP_TO_NEXT != 0L,
            canSkipPrevious = actions and PlaybackState.ACTION_SKIP_TO_PREVIOUS != 0L,
        )
        nowPlaying = next
    }

    private fun positionFor(state: PlaybackState?, durationMs: Long): Long {
        val base = state?.position?.coerceAtLeast(0L) ?: 0L
        val speed = state?.playbackSpeed ?: 0f
        val position = if (state?.state == PlaybackState.STATE_PLAYING && speed > 0f) {
            val elapsed = (SystemClock.elapsedRealtime() - state.lastPositionUpdateTime).coerceAtLeast(0L)
            base + (elapsed * speed).toLong()
        } else {
            base
        }
        return if (durationMs > 0L) position.coerceAtMost(durationMs) else position
    }
}
