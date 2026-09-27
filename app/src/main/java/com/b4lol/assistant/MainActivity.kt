package com.b4lol.assistant

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.b4lol.assistant.utils.RootUtils
import com.b4lol.assistant.utils.ScriptExecutor

private data class Shortcut(val title: String, val subtitle: String, val icon: Int, val script: String, val url: String)
private data class TileHint(val title: String, val icon: Int)

private val shortcuts = listOf(
    Shortcut("Kill App", "QuietKill", R.drawable.ic_kill, ScriptExecutor.SCRIPT_KILL_APP, ScriptExecutor.URL_QUITEKILL),
    Shortcut("Play Integrity", "PIF action", R.drawable.ic_shield, ScriptExecutor.SCRIPT_PIF, ScriptExecutor.URL_PIF),
    Shortcut("Kill GMS", "DroidGuard", R.drawable.ic_block, ScriptExecutor.SCRIPT_KILL_GMS, ScriptExecutor.URL_PIF),
    Shortcut("Update Keybox", "Integrity-Box", R.drawable.ic_key, ScriptExecutor.SCRIPT_KEYBOX, ScriptExecutor.URL_PIF),
    Shortcut("Refresh Target", "Packages", R.drawable.ic_refresh, ScriptExecutor.SCRIPT_REFRESH_TARGET, ScriptExecutor.URL_PIF),
    Shortcut("Import HMA", "Template", R.drawable.ic_import, ScriptExecutor.SCRIPT_IMPORT_HMA, ScriptExecutor.URL_PIF),
    Shortcut("Hide Lineage", "Properties", R.drawable.ic_hide, ScriptExecutor.SCRIPT_HIDE_LINEAGE, ScriptExecutor.URL_PIF),
    Shortcut("Open WebUI", "Integrity-Box", R.drawable.ic_web, ScriptExecutor.SCRIPT_OPEN_WEBUI, ScriptExecutor.URL_PIF)
)

private val tileHints = listOf(
    TileHint("Lock Device", R.drawable.ic_lock), TileHint("Caffeine", R.drawable.ic_coffee),
    TileHint("Screenshot", R.drawable.ic_screenshot), TileHint("Mobile Data", R.drawable.ic_mobile_data),
    TileHint("Wi-Fi", R.drawable.ic_wifi), TileHint("Private DNS", R.drawable.ic_dns)
).chunked(2)

private val LightColors = lightColorScheme(
    primary = Color(0xFF176B61), onPrimary = Color.White,
    primaryContainer = Color(0xFFD5F2E9), onPrimaryContainer = Color(0xFF123E37),
    secondaryContainer = Color(0xFFE8EFEB), onSecondaryContainer = Color(0xFF283B35),
    background = Color(0xFFF6F8F5), onBackground = Color(0xFF17231F),
    surface = Color.White, onSurface = Color(0xFF17231F),
    surfaceContainerLow = Color(0xFFF0F4F0), surfaceContainerHigh = Color(0xFFE8EEEA),
    onSurfaceVariant = Color(0xFF53625B), outlineVariant = Color(0xFFD8E3DC),
    errorContainer = Color(0xFFFFE1DC), onErrorContainer = Color(0xFF722C24)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF89D9C4), onPrimary = Color(0xFF00382F),
    primaryContainer = Color(0xFF1D5146), onPrimaryContainer = Color(0xFFBDF1E3),
    secondaryContainer = Color(0xFF2A3C35), onSecondaryContainer = Color(0xFFD3E8DC),
    background = Color(0xFF101916), onBackground = Color(0xFFE5EEE7),
    surface = Color(0xFF18231E), onSurface = Color(0xFFE5EEE7),
    surfaceContainerLow = Color(0xFF1B2821), surfaceContainerHigh = Color(0xFF29372F),
    onSurfaceVariant = Color(0xFFAEBCB2), outlineVariant = Color(0xFF394A40),
    errorContainer = Color(0xFF632B29), onErrorContainer = Color(0xFFFFDAD6)
)

class MainActivity : ComponentActivity() {
    private var rootGranted by mutableStateOf<Boolean?>(null)
    private var runningScript by mutableStateOf<String?>(null)
    private var missingModule by mutableStateOf<Pair<String, String>?>(null)
    private var popupMessage by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        popupMessage = intent?.getStringExtra("meowna")
        setContent {
            val dark = androidx.compose.foundation.isSystemInDarkTheme()
            MaterialTheme(colorScheme = if (dark) DarkColors else LightColors) { Screen() }
        }
        refreshRootStatus()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        popupMessage = intent.getStringExtra("meowna")
        refreshRootStatus()
    }

    private fun refreshRootStatus() {
        RootUtils.runAsync {
            val granted = RootUtils.hasRootAccess()
            runOnUiThread { if (!isDestroyed) rootGranted = granted }
        }
    }

    private fun execute(shortcut: Shortcut) {
        if (runningScript != null) return
        runningScript = shortcut.script
        ScriptExecutor.executeScript(shortcut.script, shortcut.url, object : ScriptExecutor.ExecutionCallback {
            override fun onSuccess(output: String) {
                runningScript = null
                toast("${shortcut.title}: Success")
            }
            override fun onError(error: String) {
                runningScript = null
                toast("${shortcut.title}: ${error.ifBlank { "Failed" }.take(80)}")
            }
            override fun onModuleMissing(moduleUrl: String) {
                runningScript = null
                missingModule = shortcut.title to moduleUrl
            }
        })
    }

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    private fun open(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (ignored: ActivityNotFoundException) {
            toast("No browser available")
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    private fun Screen() {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { Header() }
                item { Spacer(Modifier.height(8.dp)) }
                item { RootStatus() }
                item { Spacer(Modifier.height(14.dp)) }
                item { SectionTitle("Root shortcuts", if (runningScript == null) "Module actions" else "Running action…") }
                items(shortcuts, key = { it.script }) { shortcut ->
                    ActionCard(shortcut, runningScript == null) { execute(shortcut) }
                }
                item { Spacer(Modifier.height(14.dp)) }
                item { SectionTitle("Quick Settings", "Add these tiles from your system panel") }
                items(tileHints, key = { it.first().title }) { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { hint ->
                            TilePreview(hint, Modifier.weight(1f))
                        }
                    }
                }
                item { Spacer(Modifier.height(12.dp)) }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { open("https://github.com/b4lol/B4Assistant") }, modifier = Modifier.weight(1f)) { Text("Source") }
                        OutlinedButton(onClick = { open("https://github.com/b4lol/B4Assistant/issues") }, modifier = Modifier.weight(1f)) { Text("Feedback") }
                    }
                }
                item { Text("Based on MeowAssistant by MeowDump", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
        missingModule?.let { (name, url) ->
            AlertDialog(
                onDismissRequest = { missingModule = null },
                title = { Text("$name unavailable") },
                text = { Text("The required module is not installed. Open its releases page?") },
                confirmButton = { Button(onClick = { missingModule = null; open(url) }) { Text("Open releases") } },
                dismissButton = { OutlinedButton(onClick = { missingModule = null }) { Text("Cancel") } }
            )
        }
        popupMessage?.let { message ->
            AlertDialog(onDismissRequest = { popupMessage = null }, title = { Text("B4Assistant") }, text = { Text(message) }, confirmButton = { Button(onClick = { popupMessage = null }) { Text("OK") } })
        }
    }

    @Composable
    private fun Header() {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(Modifier.size(52.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
                Text("B4", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
            }
            Column {
                Text("B4Assistant", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text("Your Quick Settings toolkit", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    private fun RootStatus() {
        val granted = rootGranted
        val container = if (granted == false) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
        val foreground = if (granted == false) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
        Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = container), modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(Modifier.size(48.dp).background(foreground.copy(alpha = 0.12f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                    if (granted == null) LoadingIndicator(Modifier.size(30.dp))
                    else Icon(painterResource(if (granted) R.drawable.ic_root_ok else R.drawable.ic_root_error), null, Modifier.size(26.dp), tint = foreground)
                }
                Column {
                    Text("Root access", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = foreground)
                    Text(when (granted) { null -> "Checking access…"; true -> "Ready for root actions"; false -> "Grant access to use shortcuts" }, style = MaterialTheme.typography.bodyMedium, color = foreground)
                }
            }
        }
    }

    @Composable
    private fun SectionTitle(title: String, subtitle: String) {
        Column(Modifier.padding(vertical = 4.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    @Composable
    private fun ActionCard(shortcut: Shortcut, enabled: Boolean, onClick: () -> Unit) {
        Card(
            onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(Modifier.size(48.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                    Icon(painterResource(shortcut.icon), null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Column(Modifier.weight(1f)) {
                    Text(shortcut.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(shortcut.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text("›", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }

    @Composable
    private fun TilePreview(hint: TileHint, modifier: Modifier) {
        Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), modifier = modifier.height(92.dp)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(painterResource(hint.icon), null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
                Text(hint.title, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
