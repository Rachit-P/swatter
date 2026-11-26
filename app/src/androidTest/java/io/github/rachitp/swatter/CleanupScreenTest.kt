package io.github.rachitp.swatter

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.rachitp.swatter.core.Analyzer
import io.github.rachitp.swatter.core.AppSnapshot
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CleanupScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private val res = InstrumentationRegistry.getInstrumentation().targetContext.resources
    private val now = System.currentTimeMillis()

    private val apps = Analyzer.rank(
        listOf(
            AppSnapshot("com.calm.notes", "Notes", now - 100_000_000, false, false, true, null, 1_000),
            AppSnapshot("com.junk.game", "Bubble Game", now - 1_000, false, true, false, now - 60_000, null),
        ),
        now,
    )

    @Test
    fun showsAppsWithBadgesAndRemoveCallsBack() {
        var removed: String? = null
        compose.setContent {
            CleanupScreen(apps, usageAccessGranted = true, iconFor = { null }, onRemove = { removed = it }, onClose = {})
        }
        compose.onNodeWithText("Bubble Game").assertIsDisplayed()
        compose.onNodeWithText("Notes").assertIsDisplayed()
        compose.onNodeWithText("⚠ " + res.getString(R.string.badge_overlay)).assertIsDisplayed()
        compose.onNodeWithText("⚠ " + res.getString(R.string.badge_hidden)).assertIsDisplayed()

        compose.onNodeWithTag("remove-com.junk.game").performClick()
        assertEquals("com.junk.game", removed)
    }

    @Test
    fun showsSetupWarningWithoutUsageAccess() {
        compose.setContent {
            CleanupScreen(apps, usageAccessGranted = false, iconFor = { null }, onRemove = {}, onClose = {})
        }
        compose.onNodeWithTag("usage-warning").assertIsDisplayed()
    }

    @Test
    fun emptyListShowsCleanMessage() {
        compose.setContent {
            CleanupScreen(emptyList(), usageAccessGranted = true, iconFor = { null }, onRemove = {}, onClose = {})
        }
        compose.onNodeWithText(res.getString(R.string.cleanup_empty)).assertIsDisplayed()
    }
}
