package dev.wafla.musiclive.data

import android.content.Context
import androidx.core.content.edit

object AppPrefs {
    private const val NAME = "music_live_settings"
    private const val KEY_NOTIFICATION_ACTIONS = "notification_actions"

    fun areNotificationActionsEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_NOTIFICATION_ACTIONS, true)

    fun setNotificationActionsEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit { putBoolean(KEY_NOTIFICATION_ACTIONS, enabled) }
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
}
