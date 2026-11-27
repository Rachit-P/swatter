package io.github.rachitp.swatter

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** One-time setup, done by the helper on the owner's phone. */
class SetupActivity : ComponentActivity() {
    private var usageAccess by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SwatterTheme {
                Column(
                    Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                ) {
                    Text(stringResource(R.string.setup_title), fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.setup_intro), fontSize = 17.sp)

                    Spacer(Modifier.height(24.dp))
                    if (usageAccess) {
                        Text(stringResource(R.string.setup_step_usage_done), fontSize = 18.sp, color = Color(0xFF2E7D32))
                    } else {
                        Text(stringResource(R.string.setup_step_usage), fontSize = 18.sp)
                        Spacer(Modifier.height(8.dp))
                        BigButton(stringResource(R.string.open_usage_settings)) { openUsageAccessSettings() }
                    }

                    Spacer(Modifier.height(24.dp))
                    if (Build.VERSION.SDK_INT >= 30) {
                        Text(stringResource(R.string.setup_step_shortcut), fontSize = 18.sp)
                        Spacer(Modifier.height(8.dp))
                        BigButton(stringResource(R.string.open_accessibility_settings)) {
                            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        }
                    } else {
                        Text(stringResource(R.string.setup_step_shortcut_old), fontSize = 18.sp)
                    }

                    Spacer(Modifier.height(24.dp))
                    Text(stringResource(R.string.setup_step_test), fontSize = 18.sp)
                    Spacer(Modifier.height(8.dp))
                    BigButton(stringResource(R.string.open_cleanup)) {
                        startActivity(Intent(this@SetupActivity, CleanupActivity::class.java))
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        usageAccess = AppScanner(this).hasUsageAccess()
    }

    private fun openUsageAccessSettings() {
        val direct = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS, Uri.parse("package:$packageName"))
        try {
            startActivity(direct)
        } catch (e: Exception) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
    }
}

@androidx.compose.runtime.Composable
private fun BigButton(text: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().height(56.dp)) {
        Text(text, fontSize = 18.sp)
    }
}
