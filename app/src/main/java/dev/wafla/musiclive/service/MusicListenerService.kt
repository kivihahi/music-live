package dev.wafla.musiclive.service

import android.app.Notification
import android.content.ComponentName
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import dev.wafla.musiclive.data.MusicRepository

class MusicListenerService : NotificationListenerService() {
    private companion object {
        const val TAG = "MusicLive.Listener"
    }

    private val handler = Handler(Looper.getMainLooper())
    private val componentName by lazy {
        ComponentName(this, MusicListenerService::class.java)
    }
    private val sessionListener =
        MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
            bindControllers(controllers)
        }

    private var mediaSessionManager: MediaSessionManager? = null
    private var registered = false

    override fun onCreate() {
        super.onCreate()
        mediaSessionManager = getSystemService(MediaSessionManager::class.java)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i(TAG, "Notification listener connected")
        registerSessionListener()
        refreshSessions()
    }

    override fun onListenerDisconnected() {
        Log.i(TAG, "Notification listener disconnected")
        unregisterSessionListener()
        MusicRepository.unbind()
        super.onListenerDisconnected()
    }

    override fun onDestroy() {
        unregisterSessionListener()
        MusicRepository.unbind()
        super.onDestroy()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null || sbn.packageName == packageName) return
        if (sbn.notification.category == Notification.CATEGORY_TRANSPORT) {
            refreshSessions()
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        if (sbn == null || sbn.packageName == packageName) return
        if (sbn.notification.category == Notification.CATEGORY_TRANSPORT) {
            refreshSessions()
        }
    }

    private fun registerSessionListener() {
        if (registered) return
        val manager = mediaSessionManager ?: return
        try {
            manager.addOnActiveSessionsChangedListener(sessionListener, componentName, handler)
            registered = true
        } catch (e: SecurityException) {
            Log.e(TAG, "Cannot register active sessions listener", e)
        }
    }

    private fun unregisterSessionListener() {
        if (!registered) return
        try {
            mediaSessionManager?.removeOnActiveSessionsChangedListener(sessionListener)
        } catch (e: Exception) {
            Log.w(TAG, "Cannot unregister active sessions listener", e)
        }
        registered = false
    }

    private fun refreshSessions() {
        val manager = mediaSessionManager ?: return
        try {
            bindControllers(manager.getActiveSessions(componentName))
        } catch (e: SecurityException) {
            Log.e(TAG, "Notification listener access is not granted", e)
            MusicRepository.unbind()
        } catch (e: Exception) {
            Log.e(TAG, "Cannot read active sessions", e)
            MusicRepository.unbind()
        }
    }

    private fun bindControllers(controllers: List<MediaController>?) {
        val controller = controllers?.firstOrNull()
        if (controller == null) {
            Log.i(TAG, "No active media sessions")
            MusicRepository.unbind()
        } else {
            MusicRepository.bind(controller)
        }
    }


}
