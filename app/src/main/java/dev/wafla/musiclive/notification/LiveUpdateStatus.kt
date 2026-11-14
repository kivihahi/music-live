package dev.wafla.musiclive.notification

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object LiveUpdateStatus {
    var serviceRunning by mutableStateOf(false)
    var hasPromotableCharacteristics by mutableStateOf<Boolean?>(null)
    var canPostPromotedNotifications by mutableStateOf<Boolean?>(null)
    var actuallyPromoted by mutableStateOf<Boolean?>(null)
    var lastError by mutableStateOf<String?>(null)
    var lastUpdatedAt by mutableLongStateOf(0L)
}
