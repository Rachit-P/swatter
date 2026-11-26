package io.github.rachitp.swatter

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.rachitp.swatter.core.AnalyzerConfig
import io.github.rachitp.swatter.core.Analyzer
import io.github.rachitp.swatter.core.RankedApp
import java.util.concurrent.Executors

/** The screen the owner reaches by holding both volume buttons. */
class CleanupActivity : ComponentActivity() {
    private var apps by mutableStateOf<List<RankedApp>>(emptyList())
    private var usageAccess by mutableStateOf(true)
    private val worker = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SwatterTheme {
                CleanupScreen(
                    apps = apps,
                    usageAccessGranted = usageAccess,
                    iconFor = { pkg -> runCatching { packageManager.getApplicationIcon(pkg) }.getOrNull() },
                    onRemove = ::requestUninstall,
                    onClose = ::finish,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-scan every time we come back, e.g. after the uninstall dialog closes.
        worker.execute {
            val scanner = AppScanner(this)
            val now = System.currentTimeMillis()
            val ranked = Analyzer.rank(scanner.scan(now), now, AnalyzerConfig(excludedPackages = setOf(packageName)))
            val granted = scanner.hasUsageAccess()
            runOnUiThread {
                apps = ranked
                usageAccess = granted
            }
        }
    }

    override fun onDestroy() {
        worker.shutdown()
        super.onDestroy()
    }

    @Suppress("DEPRECATION")
    private fun requestUninstall(pkg: String) {
        // Android always asks the owner to confirm; a normal app can't uninstall silently.
        startActivity(Intent(Intent.ACTION_DELETE, Uri.parse("package:$pkg")))
    }
}
