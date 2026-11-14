package dev.wafla.musiclive.data

import android.graphics.Bitmap

data class NowPlaying(
    val title: String,
    val artist: String,
    val album: String?,
    val artwork: Bitmap?,
    val durationMs: Long,
    val positionMs: Long,
    val isPlaying: Boolean,
    val packageName: String,
    val canSeek: Boolean,
    val canSkipNext: Boolean,
    val canSkipPrevious: Boolean,
) {
    val progress: Float
        get() = if (durationMs > 0L) {
            (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
}

fun formatDuration(ms: Long): String {
    if (ms <= 0L) return "0:00"
    val totalSeconds = ms / 1000L
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return if (minutes >= 60L) {
        val hours = minutes / 60L
        val restMinutes = minutes % 60L
        "%d:%02d:%02d".format(hours, restMinutes, seconds)
    } else {
        "%d:%02d".format(minutes, seconds)
    }
}
