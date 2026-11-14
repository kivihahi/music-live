package dev.wafla.musiclive.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.wafla.musiclive.data.MusicRepository

class MediaActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        MusicRepository.perform(action)
    }
}
