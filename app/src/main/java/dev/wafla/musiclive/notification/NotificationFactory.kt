package dev.wafla.musiclive.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import dev.wafla.musiclive.MainActivity
import dev.wafla.musiclive.R
import dev.wafla.musiclive.data.MediaAction
import dev.wafla.musiclive.data.NowPlaying
import dev.wafla.musiclive.data.formatDuration
import dev.wafla.musiclive.service.MediaActionReceiver

object NotificationFactory {
    const val CHANNEL_ID = "music_live_update"
    const val NOTIFICATION_ID = 1001

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = context.getString(R.string.channel_description)
            setSound(null, null)
            enableVibration(false)
            setShowBadge(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        manager.createNotificationChannel(channel)
    }

    fun build(
        context: Context,
        nowPlaying: NowPlaying?,
        includeActions: Boolean = true,
    ): Notification {
        ensureChannel(context)

        val title = nowPlaying?.title ?: context.getString(R.string.notification_no_player)
        val artist = nowPlaying?.artist ?: context.getString(R.string.notification_start_music)
        val position = nowPlaying?.positionMs ?: 0L
        val duration = nowPlaying?.durationMs ?: 0L
        val elapsed = if (duration > 0L) position.coerceIn(0L, duration) else position
        val timeText = if (nowPlaying == null) {
            artist
        } else if (duration > 0L) {
            "${formatDuration(elapsed)} / ${formatDuration(duration)}"
        } else {
            formatDuration(elapsed)
        }

        val builder = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_mark)
            .setContentTitle(title)
            .setContentText(timeText)
            .setContentIntent(contentIntent(context))
            .setCategory(Notification.CATEGORY_TRANSPORT)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)

        if (Build.VERSION.SDK_INT_FULL >= Build.VERSION_CODES_FULL.BAKLAVA_1) {
            builder.setRequestPromotedOngoing(true)
        }

        val maxProgress = duration.coerceIn(1L, Int.MAX_VALUE.toLong()).toInt()
        val currentProgress = elapsed.coerceIn(0L, maxProgress.toLong()).toInt()
        val progressStyle = Notification.ProgressStyle()
            .setProgressIndeterminate(duration <= 0L)
            .setStyledByProgress(true)
        if (duration > 0L) {
            progressStyle
                .addProgressSegment(Notification.ProgressStyle.Segment(maxProgress))
                .setProgress(currentProgress)
        }
        builder.setStyle(progressStyle)

        if (includeActions) {
            builder.addAction(
                action(
                    context,
                    R.drawable.ic_skip_previous,
                    context.getString(R.string.action_back),
                    MediaAction.PREVIOUS,
                ),
            )
            if (nowPlaying?.isPlaying == true) {
                builder.addAction(
                    action(
                        context,
                        R.drawable.ic_pause,
                        context.getString(R.string.action_pause),
                        MediaAction.TOGGLE,
                    ),
                )
            } else {
                builder.addAction(
                    action(
                        context,
                        R.drawable.ic_play,
                        context.getString(R.string.action_play),
                        MediaAction.TOGGLE,
                    ),
                )
            }
            builder.addAction(
                action(
                    context,
                    R.drawable.ic_skip_next,
                    context.getString(R.string.action_forward),
                    MediaAction.NEXT,
                ),
            )
        }

        return builder.build()
    }

    private fun contentIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun action(
        context: Context,
        icon: Int,
        title: String,
        action: String,
    ): Notification.Action {
        val intent = Intent(context, MediaActionReceiver::class.java).setAction(action)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return Notification.Action.Builder(
            Icon.createWithResource(context, icon),
            title,
            pendingIntent,
        ).build()
    }
}
