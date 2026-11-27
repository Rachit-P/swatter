package io.github.rachitp.swatter

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** The volume-button trigger depends on the system seeing CleanupActivity as an accessibility shortcut target. */
@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 30)
class ShortcutTargetTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun cleanupActivityIsAnAccessibilityShortcutTarget() {
        val intent = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_ACCESSIBILITY_SHORTCUT_TARGET)
            .setPackage(context.packageName)
        @Suppress("DEPRECATION")
        val targets = context.packageManager.queryIntentActivities(intent, 0)
        assertTrue(targets.any { it.activityInfo.name == CleanupActivity::class.java.name })
    }
}
