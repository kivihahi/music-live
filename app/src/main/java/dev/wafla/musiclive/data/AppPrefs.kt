package dev.wafla.musiclive.data

import android.content.Context
import androidx.core.content.edit

object AppPrefs {
    private const val NAME = "music_live_settings"
    private const val KEY_NOTIFICATION_ACTIONS = "notification_actions"
    private const val KEY_WELCOME_SHOWN = "welcome_shown"

    fun areNotificationActionsEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_NOTIFICATION_ACTIONS, true)

    fun setNotificationActionsEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit { putBoolean(KEY_NOTIFICATION_ACTIONS, enabled) }
    }

    fun isWelcomeShown(context: Context): Boolean =
        prefs(context).getBoolean(KEY_WELCOME_SHOWN, false)

    fun setWelcomeShown(context: Context) {
        prefs(context).edit { putBoolean(KEY_WELCOME_SHOWN, true) }
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
}
