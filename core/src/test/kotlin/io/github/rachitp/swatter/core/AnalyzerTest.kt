package io.github.rachitp.swatter.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalyzerTest {
    private val now = 1_000_000_000L
    private val minute = 60_000L

    private fun app(
        pkg: String,
        installedAt: Long = 0,
        system: Boolean = false,
        overlay: Boolean = false,
        icon: Boolean = true,
        lastFg: Long? = null,
        size: Long? = null,
    ) = AppSnapshot(pkg, pkg, installedAt, system, overlay, icon, lastFg, size)

    @Test
    fun sortsNewestInstallFirst() {
        val ranked = Analyzer.rank(
            listOf(app("old", installedAt = 1), app("newest", installedAt = 3), app("mid", installedAt = 2)),
            now,
        )
        assertEquals(listOf("newest", "mid", "old"), ranked.map { it.app.packageName })
    }

    @Test
    fun leavesOutSystemAndExcludedApps() {
        val ranked = Analyzer.rank(
            listOf(app("sys", system = true), app("self"), app("game")),
            now,
            AnalyzerConfig(excludedPackages = setOf("self")),
        )
        assertEquals(listOf("game"), ranked.map { it.app.packageName })
    }

    @Test
    fun cleanAppHasNoBadges() {
        val ranked = Analyzer.rank(listOf(app("calm", lastFg = now - 60 * minute, size = 10)), now).single()
        assertTrue(ranked.badges.isEmpty())
        assertEquals(Risk.LOW, ranked.risk)
    }

    @Test
    fun adwarePatternIsHighRisk() {
        val ranked = Analyzer.rank(
            listOf(app("adware", overlay = true, icon = false, lastFg = now - 2 * minute, size = 900L * 1024 * 1024)),
            now,
        ).single()
        assertEquals(
            setOf(Badge.SHOWS_OVER_OTHER_APPS, Badge.OPEN_RECENTLY, Badge.USES_LOTS_OF_SPACE, Badge.HIDDEN_FROM_HOME_SCREEN),
            ranked.badges,
        )
        assertEquals(Risk.HIGH, ranked.risk)
    }

    @Test
    fun oneBadgeIsMediumRisk() {
        assertEquals(Risk.MEDIUM, Analyzer.rank(listOf(app("x", overlay = true)), now).single().risk)
    }

    @Test
    fun recentWindowBoundary() {
        val window = AnalyzerConfig().recentWindowMillis
        assertTrue(Badge.OPEN_RECENTLY in Analyzer.badgesFor(app("a", lastFg = now - window), now))
        assertTrue(Badge.OPEN_RECENTLY !in Analyzer.badgesFor(app("a", lastFg = now - window - 1), now))
    }

    @Test
    fun unknownUsageDataAddsNoBadges() {
        assertTrue(Analyzer.badgesFor(app("a", lastFg = null, size = null), now).isEmpty())
    }
}
