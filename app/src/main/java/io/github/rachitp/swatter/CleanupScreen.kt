package io.github.rachitp.swatter

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import io.github.rachitp.swatter.core.Badge
import io.github.rachitp.swatter.core.RankedApp
import io.github.rachitp.swatter.core.Risk
import java.text.DateFormat
import java.util.Date

private val Red = Color(0xFFD32F2F)
private val Amber = Color(0xFFF57C00)
private val RedBackground = Color(0xFFFFEBEE)

@Composable
fun CleanupScreen(
    apps: List<RankedApp>,
    usageAccessGranted: Boolean,
    iconFor: (String) -> Drawable?,
    onRemove: (String) -> Unit,
    onClose: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
    ) {
        Text(
            stringResource(R.string.cleanup_title),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 32.sp,
        )
        Spacer(Modifier.height(4.dp))
        Text(stringResource(R.string.cleanup_subtitle), fontSize = 16.sp)
        if (!usageAccessGranted) {
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.cleanup_no_usage_access),
                color = Amber,
                fontSize = 16.sp,
                modifier = Modifier.testTag("usage-warning"),
            )
        }
        Spacer(Modifier.height(12.dp))
        if (apps.isEmpty()) {
            Text(stringResource(R.string.cleanup_empty), fontSize = 20.sp, modifier = Modifier.weight(1f))
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).testTag("app-list"),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(apps, key = { it.app.packageName }) { ranked ->
                    AppRow(ranked, iconFor(ranked.app.packageName), onRemove)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onClose, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text(stringResource(R.string.close), fontSize = 20.sp)
        }
    }
}

@Composable
private fun AppRow(ranked: RankedApp, icon: Drawable?, onRemove: (String) -> Unit) {
    val high = ranked.risk == Risk.HIGH
    val shape = RoundedCornerShape(16.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .background(if (high) RedBackground else Color(0xFFF5F5F5), shape)
            .then(if (high) Modifier.border(3.dp, Red, shape) else Modifier)
            .padding(12.dp)
            .testTag("app-${ranked.app.packageName}"),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val bitmap = remember(icon) { icon?.toBitmap(128, 128)?.asImageBitmap() }
            if (bitmap != null) {
                Image(bitmap, contentDescription = null, modifier = Modifier.size(56.dp))
                Spacer(Modifier.width(12.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(ranked.app.label, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(
                    stringResource(
                        R.string.installed_on,
                        DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(ranked.app.installedAtMillis)),
                    ),
                    fontSize = 14.sp,
                )
            }
        }
        for (badge in ranked.badges) {
            Text(
                "⚠ " + stringResource(badge.label()),
                color = if (high) Red else Amber,
                fontSize = 17.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { onRemove(ranked.app.packageName) },
            colors = ButtonDefaults.buttonColors(containerColor = Red),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("remove-${ranked.app.packageName}"),
        ) {
            Text(stringResource(R.string.remove), fontSize = 22.sp, color = Color.White)
        }
    }
}

fun Badge.label(): Int = when (this) {
    Badge.SHOWS_OVER_OTHER_APPS -> R.string.badge_overlay
    Badge.OPEN_RECENTLY -> R.string.badge_recent
    Badge.USES_LOTS_OF_SPACE -> R.string.badge_space
    Badge.HIDDEN_FROM_HOME_SCREEN -> R.string.badge_hidden
}

@Composable
fun SwatterTheme(content: @Composable () -> Unit) = MaterialTheme(content = content)
