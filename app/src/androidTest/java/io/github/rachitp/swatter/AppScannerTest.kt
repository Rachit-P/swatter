package io.github.rachitp.swatter

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.rachitp.swatter.core.Analyzer
import io.github.rachitp.swatter.core.AnalyzerConfig
import io.github.rachitp.swatter.core.Badge
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Runs the real scanner on the emulator. The test APK (a separate, icon-less package) plays the part of
 * a "suspicious" app: we let it draw over other apps and check that Swatter flags it.
 */
@RunWith(AndroidJUnit4::class)
class AppScannerTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val app = instrumentation.targetContext
    private val testPkg = instrumentation.context.packageName

    @Before
    fun setUp() {
        shell("appops set ${app.packageName} GET_USAGE_STATS allow")
        shell("appops set $testPkg SYSTEM_ALERT_WINDOW allow")
    }

    @After
    fun tearDown() {
        shell("appops set ${app.packageName} GET_USAGE_STATS default")
        shell("appops set $testPkg SYSTEM_ALERT_WINDOW default")
    }

    @Test
    fun usageAccessIsDetected() {
        assertTrue(AppScanner(app).hasUsageAccess())
    }

    @Test
    fun seesOtherAppsWithInstallTimeAndStorage() {
        val snapshots = AppScanner(app).scan()
        val other = snapshots.single { it.packageName == testPkg }
        assertTrue("install time should be set", other.installedAtMillis > 0)
        assertFalse(other.isSystemApp)
        assertNotNull("storage needs usage access, which is granted", other.storageBytes)
        assertTrue("system apps should be listed too", snapshots.any { it.isSystemApp })
    }

    @Test
    fun flagsAnotherAppThatCanDrawOverlays() {
        val other = AppScanner(app).scan().single { it.packageName == testPkg }
        assertTrue("overlay app-op should be visible for another package", other.canDrawOverlays)
        assertFalse("test APK has no launcher icon", other.hasLauncherIcon)

        val ranked = Analyzer.rank(listOf(other), System.currentTimeMillis()).single()
        assertTrue(Badge.SHOWS_OVER_OTHER_APPS in ranked.badges)
        assertTrue(Badge.HIDDEN_FROM_HOME_SCREEN in ranked.badges)
    }

    @Test
    fun overlayNotFlaggedWhenNotAllowed() {
        shell("appops set $testPkg SYSTEM_ALERT_WINDOW deny")
        val other = AppScanner(app).scan().single { it.packageName == testPkg }
        assertFalse(other.canDrawOverlays)
    }

    @Test
    fun rankingLeavesOutSystemAppsAndSwatterItself() {
        val now = System.currentTimeMillis()
        val ranked = Analyzer.rank(
            AppScanner(app).scan(now),
            now,
            AnalyzerConfig(excludedPackages = setOf(app.packageName)),
        )
        assertTrue(ranked.none { it.app.isSystemApp || it.app.packageName == app.packageName })
        assertEquals(ranked.sortedByDescending { it.app.installedAtMillis }.map { it.app.installedAtMillis }, ranked.map { it.app.installedAtMillis })
    }
}
