package dev.wafla.musiclive.ui

import android.Manifest
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.wafla.musiclive.R
import dev.wafla.musiclive.data.AppPrefs
import dev.wafla.musiclive.data.MusicRepository
import dev.wafla.musiclive.data.NowPlaying
import dev.wafla.musiclive.data.formatDuration
import dev.wafla.musiclive.notification.LiveUpdateStatus
import dev.wafla.musiclive.service.LiveUpdateService
import dev.wafla.musiclive.service.MusicListenerService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicLiveApp(resumeTick: Int) {
    val context = LocalContext.current
    val notificationManager = remember {
        context.getSystemService(NotificationManager::class.java)
    }
    var refreshKey by remember { mutableIntStateOf(0) }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var actionsEnabled by remember {
        mutableStateOf(AppPrefs.areNotificationActionsEnabled(context))
    }

    val notificationGranted = remember(resumeTick, refreshKey) {
        context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }
    val listenerEnabled = remember(resumeTick, refreshKey) {
        isNotificationListenerEnabled(context)
    }
    val canPostPromoted = remember(resumeTick, refreshKey) {
        notificationManager.canPostPromotedNotifications()
    }

    LaunchedEffect(resumeTick) {
        actionsEnabled = AppPrefs.areNotificationActionsEnabled(context)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        refreshKey += 1
        if (granted) {
            startLiveUpdate(context)
        }
    }

    val now = MusicRepository.nowPlaying
    val serviceRunning = LiveUpdateStatus.serviceRunning
    val hasPromotable = LiveUpdateStatus.hasPromotableCharacteristics
    val canPost = LiveUpdateStatus.canPostPromotedNotifications
    val actuallyPromoted = LiveUpdateStatus.actuallyPromoted
    val lastError = LiveUpdateStatus.lastError

    MusicLiveTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = if (selectedTab == 0) "Music Live" else "Журнал",
                            fontWeight = FontWeight.SemiBold,
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_home),
                                contentDescription = null,
                            )
                        },
                        label = { Text("Главная") },
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_history),
                                contentDescription = null,
                            )
                        },
                        label = { Text("Журнал") },
                    )
                }
            },
        ) { innerPadding ->
            if (selectedTab == 1) {
                JournalScreen(innerPadding)
            } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    NowPlayingCard(now = now)
                }
                item {
                    PermissionsCard(
                        context = context,
                        listenerEnabled = listenerEnabled,
                        notificationGranted = notificationGranted,
                        canPostPromoted = canPostPromoted,
                        onRequestNotificationPermission = {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        },
                    )
                }
                item {
                    PrototypeCard(
                        context = context,
                        now = now,
                        listenerEnabled = listenerEnabled,
                        notificationGranted = notificationGranted,
                        serviceRunning = serviceRunning,
                        actionsEnabled = actionsEnabled,
                        onActionsChanged = { enabled ->
                            actionsEnabled = enabled
                            AppPrefs.setNotificationActionsEnabled(context, enabled)
                            if (LiveUpdateStatus.serviceRunning) {
                                LiveUpdateService.refresh(context)
                            }
                        },
                        onStart = {
                            if (!notificationGranted) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                startLiveUpdate(context)
                            }
                        },
                        onStop = { LiveUpdateService.stop(context) },
                        onRefresh = {
                            if (LiveUpdateStatus.serviceRunning) {
                                LiveUpdateService.refresh(context)
                            } else {
                                startLiveUpdate(context)
                            }
                            refreshKey += 1
                        },
                    )
                }
                item {
                    PromotionCard(
                        context = context,
                        serviceRunning = serviceRunning,
                        hasPromotable = hasPromotable,
                        canPostPromoted = canPost,
                        actuallyPromoted = actuallyPromoted,
                        lastError = lastError,
                        onRefresh = {
                            refreshKey += 1
                            if (LiveUpdateStatus.serviceRunning) {
                                LiveUpdateService.refresh(context)
                            }
                        },
                    )
                }
            }
            }
        }
    }
}

@Composable
private fun NowPlayingCard(now: NowPlaying?) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Сейчас играет",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                AlbumArt(bitmap = now?.artwork, size = 76.dp)
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = now?.title ?: "Нет активной медиасессии",
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = now?.artist ?: "Включите музыку в любом плеере",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    now?.album?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            if (now != null) {
                Spacer(modifier = Modifier.height(14.dp))
                LinearProgressIndicator(
                    progress = { now.progress },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = formatDuration(now.positionMs),
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = formatDuration(now.durationMs),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionsCard(
    context: Context,
    listenerEnabled: Boolean,
    notificationGranted: Boolean,
    canPostPromoted: Boolean,
    onRequestNotificationPermission: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "1. Доступ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Онбординг: доступ к уведомлениям, POST_NOTIFICATIONS и экран Live Updates.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            PermissionRow(
                title = "Слушатель уведомлений",
                detail = "Нужен для MediaSessionManager.getActiveSessions()",
                granted = listenerEnabled,
                action = "Открыть",
                onAction = {
                    openIntent(
                        context,
                        Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS),
                    )
                },
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            PermissionRow(
                title = "POST_NOTIFICATIONS",
                detail = "Разрешение на показ уведомлений",
                granted = notificationGranted,
                action = if (notificationGranted) "Настройки" else "Разрешить",
                onAction = {
                    if (notificationGranted) {
                        openIntent(
                            context,
                            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName),
                        )
                    } else {
                        onRequestNotificationPermission()
                    }
                },
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            PermissionRow(
                title = "Live Updates",
                detail = "canPostPromotedNotifications() = $canPostPromoted",
                granted = canPostPromoted,
                action = "Настройки",
                onAction = { openPromotionSettings(context) },
            )
        }
    }
}

@Composable
private fun PrototypeCard(
    context: Context,
    now: NowPlaying?,
    listenerEnabled: Boolean,
    notificationGranted: Boolean,
    serviceRunning: Boolean,
    actionsEnabled: Boolean,
    onActionsChanged: (Boolean) -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onRefresh: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "2. Live Update",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Одно ongoing-уведомление с двумя метриками: прошло и осталось. Чип в статус-баре только с иконкой, без времени. Выключите кнопки, чтобы проверить чистый прототип без Notification.Action.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                IconButton(
                    onClick = { MusicRepository.skipToPrevious() },
                    enabled = now?.canSkipPrevious == true,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_skip_previous),
                        contentDescription = "Предыдущий трек",
                    )
                }
                FilledIconButton(
                    onClick = { MusicRepository.togglePlayPause() },
                    enabled = now != null,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Icon(
                        painter = painterResource(
                            if (now?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play,
                        ),
                        contentDescription = if (now?.isPlaying == true) "Пауза" else "Играть",
                    )
                }
                IconButton(
                    onClick = { MusicRepository.skipToNext() },
                    enabled = now?.canSkipNext == true,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_skip_next),
                        contentDescription = "Следующий трек",
                    )
                }
            }
            if (now != null && now.durationMs > 0L) {
                SeekSlider(now = now)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Кнопки в уведомлении",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = "Notification.Action + PendingIntent",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = actionsEnabled, onCheckedChange = onActionsChanged)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = if (serviceRunning) onStop else onStart,
                ) {
                    Text(if (serviceRunning) "Остановить" else "Запустить")
                }
                OutlinedButton(onClick = onRefresh) {
                    Text("Обновить")
                }
            }
            if (!listenerEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Слушатель уведомлений выключен: уведомление будет с заглушкой, пока доступ не выдан.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            if (!notificationGranted) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "POST_NOTIFICATIONS не выдан: система может не показать уведомление.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun SeekSlider(now: NowPlaying) {
    var dragValue by remember(now.title, now.packageName) {
        mutableStateOf<Float?>(null)
    }
    Slider(
        value = dragValue ?: now.progress,
        onValueChange = { dragValue = it },
        onValueChangeFinished = {
            dragValue?.let { value ->
                MusicRepository.seekTo((value * now.durationMs).toLong())
            }
            dragValue = null
        },
        enabled = now.canSeek,
    )
}

@Composable
private fun PromotionCard(
    context: Context,
    serviceRunning: Boolean,
    hasPromotable: Boolean?,
    canPostPromoted: Boolean?,
    actuallyPromoted: Boolean?,
    lastError: String?,
    onRefresh: () -> Unit,
) {
    val explanation = when {
        !serviceRunning ->
            "Сервис не запущен. Запустите его на шаге 2 и посмотрите значения ниже."
        hasPromotable == false ->
            "hasPromotableCharacteristics() = false: уведомление не выполняет требования promoted. Проверьте ongoing, title, стиль, отсутствие custom view и важность канала."
        canPostPromoted == false ->
            "canPostPromotedNotifications() = false: Live Updates выключены для приложения или системы. Откройте экран Live Updates."
        actuallyPromoted == true ->
            "Система присвоила FLAG_PROMOTED_ONGOING. Чип должен быть доступен в строке состояния; на OEM-прошивках поведение может отличаться."
        actuallyPromoted == false ->
            "Система не присвоила FLAG_PROMOTED_ONGOING. Для медиаплееров это ожидаемо: официальные Live Updates в Android 16/17 не обещают чип для музыки. Уведомление останется в шторке."
        else ->
            "Проверка выполняется после публикации уведомления."
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "3. Проверка чипа",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            DiagnosticRow(
                label = "hasPromotableCharacteristics()",
                value = hasPromotable,
            )
            DiagnosticRow(
                label = "canPostPromotedNotifications()",
                value = canPostPromoted,
            )
            DiagnosticRow(
                label = "FLAG_PROMOTED_ONGOING",
                value = actuallyPromoted,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = explanation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            lastError?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onRefresh) {
                    Text("Проверить снова")
                }
                TextButton(onClick = { openPromotionSettings(context) }) {
                    Text("Live Updates")
                }
            }
        }
    }
}

@Composable
private fun DiagnosticRow(label: String, value: Boolean?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = when (value) {
                true -> "да"
                false -> "нет"
                null -> "-"
            },
            style = MaterialTheme.typography.labelMedium,
            color = when (value) {
                true -> MaterialTheme.colorScheme.primary
                false -> MaterialTheme.colorScheme.error
                null -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun PermissionRow(
    title: String,
    detail: String,
    granted: Boolean,
    action: String,
    onAction: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = if (granted) "OK" else "НЕТ",
            style = MaterialTheme.typography.labelMedium,
            color = if (granted) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            },
            fontWeight = FontWeight.SemiBold,
        )
        TextButton(onClick = onAction) {
            Text(action)
        }
    }
}

@Composable
private fun AlbumArt(bitmap: Bitmap?, size: androidx.compose.ui.unit.Dp) {
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Обложка",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(16.dp)),
        )
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_app_mark),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(size / 2),
            )
        }
    }
}

private fun startLiveUpdate(context: Context) {
    runCatching {
        LiveUpdateService.start(context)
    }.onFailure {
        LiveUpdateStatus.lastError = "Не удалось запустить сервис: ${it.message}"
    }
}

private fun openIntent(context: Context, intent: Intent) {
    runCatching {
        context.startActivity(intent)
    }.onFailure {
        LiveUpdateStatus.lastError = "Не удалось открыть настройки: ${it.message}"
    }
}

private fun openPromotionSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_PROMOTION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    val opened = runCatching { context.startActivity(intent) }.isSuccess
    if (!opened) {
        openIntent(
            context,
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName),
        )
    }
}

private fun isNotificationListenerEnabled(context: Context): Boolean {
    val enabled = Settings.Secure.getString(
        context.contentResolver,
        "enabled_notification_listeners",
    ) ?: return false
    val expected = ComponentName(context, MusicListenerService::class.java)
    return enabled
        .split(':')
        .mapNotNull { ComponentName.unflattenFromString(it) }
        .any { it == expected }
}
