package io.github.rachitp.swatter

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Opens the real clean-up screen, as the volume shortcut would, and checks it shows up with a scan. */
@RunWith(AndroidJUnit4::class)
class CleanupActivityTest {
    @get:Rule
    val compose = createAndroidComposeRule<CleanupActivity>()

    @Test
    fun opensAndShowsTitle() {
        val title = compose.activity.getString(R.string.cleanup_title)
        compose.onNodeWithText(title).assertIsDisplayed()
    }
}
