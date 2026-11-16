package dev.wafla.musiclive.ui

import android.app.LocaleManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.LocaleList
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.wafla.musiclive.R

@Composable
fun SettingsScreen(
    innerPadding: PaddingValues,
    listenerEnabled: Boolean,
    notificationGranted: Boolean,
    canPostPromoted: Boolean,
    onRequestNotificationPermission: () -> Unit,
) {
    val context = LocalContext.current
    val localeManager = remember { context.getSystemService(LocaleManager::class.java) }
    val applicationLocales = localeManager.applicationLocales
    val selectedLanguage = if (applicationLocales.isEmpty) "" else applicationLocales[0].language
    var showAbout by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            LanguageCard(
                selectedLanguage = selectedLanguage,
                onLanguageSelected = { tag ->
                    localeManager.applicationLocales = if (tag.isEmpty()) {
                        LocaleList.getEmptyLocaleList()
                    } else {
                        LocaleList.forLanguageTags(tag)
                    }
                },
            )
        }
        item {
            PermissionsCard(
                context = context,
                listenerEnabled = listenerEnabled,
                notificationGranted = notificationGranted,
                canPostPromoted = canPostPromoted,
                onRequestNotificationPermission = onRequestNotificationPermission,
            )
        }
        item {
            AboutCard(onClick = { showAbout = true })
        }
    }
    if (showAbout) {
        AboutDialog(onDismiss = { showAbout = false })
    }
}

@Composable
private fun AboutCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.about_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.about_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = "›",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AboutDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.about_title),
                fontWeight = FontWeight.SemiBold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AboutInfoRow(
                    label = stringResource(R.string.about_author_label),
                    value = "Wafla",
                )
                AboutLinkRow(
                    label = stringResource(R.string.about_github_label),
                    value = "kivihahi/music-live",
                    onClick = {
                        openIntent(
                            context,
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/kivihahi/music-live"),
                            ),
                        )
                    },
                )
                AboutInfoRow(
                    label = stringResource(R.string.about_license_label),
                    value = stringResource(R.string.about_license_value),
                )
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:134rt5yt@gmail.com")
                            putExtra(
                                Intent.EXTRA_SUBJECT,
                                context.getString(R.string.about_report_subject),
                            )
                        }
                        openIntent(context, intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.about_report_bug))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.ok))
            }
        },
    )
}

@Composable
private fun AboutInfoRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun AboutLinkRow(label: String, value: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun LanguageCard(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.language_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.language_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(modifier = Modifier.selectableGroup()) {
                LanguageOption(
                    tag = "",
                    labelRes = R.string.language_system,
                    selected = selectedLanguage.isEmpty(),
                    onSelected = onLanguageSelected,
                )
                LanguageOption(
                    tag = "ru",
                    labelRes = R.string.language_russian,
                    selected = selectedLanguage == "ru",
                    onSelected = onLanguageSelected,
                )
                LanguageOption(
                    tag = "en",
                    labelRes = R.string.language_english,
                    selected = selectedLanguage == "en",
                    onSelected = onLanguageSelected,
                )
                LanguageOption(
                    tag = "uk",
                    labelRes = R.string.language_ukrainian,
                    selected = selectedLanguage == "uk",
                    onSelected = onLanguageSelected,
                )
            }
        }
    }
}

@Composable
private fun LanguageOption(
    tag: String,
    labelRes: Int,
    selected: Boolean,
    onSelected: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = { onSelected(tag) },
                role = Role.RadioButton,
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.bodyLarge,
        )
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
                text = stringResource(R.string.permissions_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.permissions_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            PermissionRow(
                title = stringResource(R.string.permission_listener_title),
                detail = stringResource(R.string.permission_listener_detail),
                granted = listenerEnabled,
                action = if (listenerEnabled) {
                    stringResource(R.string.action_settings)
                } else {
                    stringResource(R.string.action_allow)
                },
                onAction = {
                    openIntent(
                        context,
                        Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS),
                    )
                },
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            PermissionRow(
                title = stringResource(R.string.permission_notifications_title),
                detail = stringResource(R.string.permission_notifications_detail),
                granted = notificationGranted,
                action = if (notificationGranted) {
                    stringResource(R.string.action_settings)
                } else {
                    stringResource(R.string.action_allow)
                },
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
                title = stringResource(R.string.permission_live_updates_title),
                detail = stringResource(
                    R.string.permission_live_updates_detail,
                    canPostPromoted.toString(),
                ),
                granted = canPostPromoted,
                action = if (canPostPromoted) {
                    stringResource(R.string.action_settings)
                } else {
                    stringResource(R.string.action_allow)
                },
                onAction = { openPromotionSettings(context) },
            )
        }
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
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = stringResource(
                    if (granted) R.string.status_ok else R.string.status_no,
                ),
                style = MaterialTheme.typography.labelMedium,
                color = if (granted) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            if (granted) {
                OutlinedButton(onClick = onAction) {
                    Text(action)
                }
            } else {
                Button(onClick = onAction) {
                    Text(action)
                }
            }
        }
    }
}
