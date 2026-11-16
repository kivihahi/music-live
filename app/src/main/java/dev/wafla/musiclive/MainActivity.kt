package dev.wafla.musiclive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableIntStateOf
import dev.wafla.musiclive.data.MusicRepository
import dev.wafla.musiclive.notification.NotificationFactory
import dev.wafla.musiclive.ui.MusicLiveApp

class MainActivity : ComponentActivity() {
    private val resumeTick = mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationFactory.ensureChannel(this)
        MusicRepository.setFallbackTitle(getString(R.string.media_session_active))
        setContent {
            MusicLiveApp(resumeTick = resumeTick.intValue)
        }
    }

    override fun onResume() {
        super.onResume()
        resumeTick.intValue += 1
    }
}
