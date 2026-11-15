package dev.wafla.musiclive.service

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import dev.wafla.musiclive.data.AppPrefs
import dev.wafla.musiclive.data.MusicRepository
import dev.wafla.musiclive.notification.LiveUpdateStatus
import dev.wafla.musiclive.notification.NotificationFactory

class LiveUpdateService : Service() {
    companion object {
        private const val TAG = "MusicLive.Service"
        const val ACTION_START = "dev.wafla.musiclive.service.START"
        const val ACTION_STOP = "dev.wafla.musiclive.service.STOP"
        const val ACTION_REFRESH = "dev.wafla.musiclive.service.REFRESH"
        fun start(context: Context) {
            val intent = Intent(context, LiveUpdateService::class.java)
                .setAction(ACTION_START)
            context.startService(intent)
        }

        fun refresh(context: Context) {
            val intent = Intent(context, LiveUpdateService::class.java)
                .setAction(ACTION_REFRESH)
            context.startService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, LiveUpdateService::class.java))
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private val notificationManager by lazy {
        getSystemService(NotificationManager::class.java)
    }

    private var startedForeground = false
    private var promotionCheckToken = 0

    private val ticker = object : Runnable {
        override fun run() {
            updateNotification()
            handler.postDelayed(this, 1_000L)
        }
    }

    override fun onCreate() {
        super.onCreate()
        NotificationFactory.ensureChannel(this)
        LiveUpdateStatus.serviceRunning = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        updateNotification()
        handler.removeCallbacks(ticker)
        handler.postDelayed(ticker, 1_000L)
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(ticker)
        notificationManager.cancel(NotificationFactory.NOTIFICATION_ID)
        LiveUpdateStatus.serviceRunning = false
        startedForeground = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun updateNotification() {
        val nowPlaying = MusicRepository.nowPlaying
        if (nowPlaying == null) {
            hideNotification()
            return
        }

        val notification = NotificationFactory.build(
            context = this,
            nowPlaying = nowPlaying,
            includeActions = AppPrefs.areNotificationActionsEnabled(this),
        )

        try {
            if (startedForeground) {
                notificationManager.notify(NotificationFactory.NOTIFICATION_ID, notification)
            } else {
                startForeground(
                    NotificationFactory.NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
                )
                startedForeground = true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Cannot enter foreground", e)
            LiveUpdateStatus.lastError = "Не удалось показать уведомление: ${e.message}"
            try {
                notificationManager.notify(NotificationFactory.NOTIFICATION_ID, notification)
            } catch (notifyError: Exception) {
                Log.e(TAG, "Cannot post notification", notifyError)
                LiveUpdateStatus.lastError =
                    "Не удалось опубликовать уведомление: ${notifyError.message}"
            }
        }
        inspectPromotion(notification)
    }

    private fun hideNotification() {
        notificationManager.cancel(NotificationFactory.NOTIFICATION_ID)
        if (startedForeground) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            startedForeground = false
        }
        LiveUpdateStatus.hasPromotableCharacteristics = false
        LiveUpdateStatus.actuallyPromoted = false
    }

    private fun inspectPromotion(notification: Notification) {
        val hasPromotable = notification.hasPromotableCharacteristics()
        val canPostPromoted = notificationManager.canPostPromotedNotifications()

        val changed = LiveUpdateStatus.hasPromotableCharacteristics != hasPromotable ||
            LiveUpdateStatus.canPostPromotedNotifications != canPostPromoted

        LiveUpdateStatus.hasPromotableCharacteristics = hasPromotable
        LiveUpdateStatus.canPostPromotedNotifications = canPostPromoted
        LiveUpdateStatus.lastUpdatedAt = System.currentTimeMillis()

        if (changed) {
            Log.i(
                TAG,
                "hasPromotableCharacteristics=$hasPromotable " +
                    "canPostPromotedNotifications=$canPostPromoted",
            )
        }

        val token = ++promotionCheckToken
        handler.postDelayed({
            if (token != promotionCheckToken) return@postDelayed
            val posted = notificationManager.activeNotifications
                .firstOrNull { it.id == NotificationFactory.NOTIFICATION_ID }
                ?.notification
            val promoted = posted != null &&
                posted.flags and Notification.FLAG_PROMOTED_ONGOING != 0
            if (LiveUpdateStatus.actuallyPromoted != promoted) {
                Log.i(TAG, "FLAG_PROMOTED_ONGOING=$promoted")
            }
            LiveUpdateStatus.actuallyPromoted = promoted
        }, 600L)
    }

}
