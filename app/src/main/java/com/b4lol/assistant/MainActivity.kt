package com.b4lol.assistant

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
).chunked(2)

private val tileHints = listOf(
    TileHint("Lock Device", R.drawable.ic_lock), TileHint("Caffeine", R.drawable.ic_coffee),
    TileHint("Screenshot", R.drawable.ic_screenshot), TileHint("Mobile Data", R.drawable.ic_mobile_data),
    TileHint("Wi-Fi", R.drawable.ic_wifi), TileHint("Private DNS", R.drawable.ic_dns)
).chunked(2)

class MainActivity : ComponentActivity() {
    private var rootGranted by mutableStateOf<Boolean?>(null)
    private var runningScript by mutableStateOf<String?>(null)
    private var missingModule by mutableStateOf<Pair<String, String>?>(null)
    private var popupMessage by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        popupMessage = intent?.getStringExtra("meowna")
        setContent {
            val dark = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
            val scheme = remember(dark) {
                when {
                    Build.VERSION.SDK_INT >= 31 && dark -> dynamicDarkColorScheme(this)
                    Build.VERSION.SDK_INT >= 31 -> dynamicLightColorScheme(this)
                    dark -> darkColorScheme()
                    else -> lightColorScheme()
                }
            }
            MaterialTheme(colorScheme = scheme) { Screen() }
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
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }
            item {
                Column {
                    Text("B4Assistant", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                    Text("Your Quick Settings toolkit", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        when (rootGranted) {
                            null -> LoadingIndicator(modifier = Modifier.size(40.dp))
                            true -> Icon(painterResource(R.drawable.ic_root_ok), null, Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            false -> Icon(painterResource(R.drawable.ic_root_error), null, Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Column {
                            Text("Root access", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                            Text(when (rootGranted) { null -> "Checking…"; true -> "Ready for root actions"; false -> "Grant access to use shortcuts" }, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            item { SectionTitle("Root shortcuts", if (runningScript == null) "Run supported module actions" else "Running action…") }
            items(shortcuts, key = { it.first().title }) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { shortcut ->
                        ShortcutCard(shortcut.title, shortcut.subtitle, painterResource(shortcut.icon), Modifier.weight(1f), runningScript == null) { execute(shortcut) }
                    }
                }
            }
            item { SectionTitle("Quick Settings tiles", "Add these from your system panel") }
            items(tileHints, key = { it.first().title }) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { hint ->
                        ShortcutCard(hint.title, "Available as a tile", painterResource(hint.icon), Modifier.weight(1f)) {
                            toast("Add ${hint.title} in Quick Settings")
                        }
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { open("https://github.com/b4lol/B4Assistant") }, modifier = Modifier.weight(1f)) { Text("Source") }
                    OutlinedButton(onClick = { open("https://github.com/b4lol/B4Assistant/issues") }, modifier = Modifier.weight(1f)) { Text("Feedback") }
                }
            }
            item { Text("Based on MeowAssistant by MeowDump", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
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
    private fun SectionTitle(title: String, subtitle: String) {
        Column {
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    @Composable
    private fun ShortcutCard(title: String, subtitle: String, icon: Painter, modifier: Modifier, enabled: Boolean = true, onClick: () -> Unit) {
        Card(onClick = onClick, enabled = enabled, modifier = modifier.height(136.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, null, Modifier.size(26.dp), tint = MaterialTheme.colorScheme.primary)
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        }
    }
}
