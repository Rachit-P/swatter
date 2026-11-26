package io.github.rachitp.swatter.core

/** A warning shown next to an app. Each one is something an owner can understand from a picture or one sentence. */
enum class Badge {
    /** Allowed to draw over other apps: the main way pop-up ads appear. */
    SHOWS_OVER_OTHER_APPS,

    /** Was on screen in the last few minutes, e.g. when the owner saw the ads. */
    OPEN_RECENTLY,

    /** Takes a lot of storage. */
    USES_LOTS_OF_SPACE,

    /** Installed, but has no icon on the home screen. Common adware trick. */
    HIDDEN_FROM_HOME_SCREEN,
}

enum class Risk { LOW, MEDIUM, HIGH }

data class RankedApp(
    val app: AppSnapshot,
    val badges: Set<Badge>,
) {
    val risk: Risk
        get() = when {
            badges.size >= 2 -> Risk.HIGH
            badges.size == 1 -> Risk.MEDIUM
            else -> Risk.LOW
        }
}

data class AnalyzerConfig(
    /** How far back counts as "open recently". */
    val recentWindowMillis: Long = 15 * 60 * 1000L,
    /** Storage above this counts as "a lot of space". */
    val largeStorageBytes: Long = 500L * 1024 * 1024,
    /** Packages never shown, e.g. Swatter itself. */
    val excludedPackages: Set<String> = emptySet(),
)

object Analyzer {
    /**
     * Returns the apps the owner can remove, newest install first, each with its warning badges.
     * System apps and excluded packages are left out because the owner can't (or shouldn't) remove them.
     */
    fun rank(
        apps: List<AppSnapshot>,
        nowMillis: Long,
        config: AnalyzerConfig = AnalyzerConfig(),
    ): List<RankedApp> =
        apps.asSequence()
            .filter { !it.isSystemApp && it.packageName !in config.excludedPackages }
            .map { RankedApp(it, badgesFor(it, nowMillis, config)) }
            .sortedWith(
                compareByDescending<RankedApp> { it.app.installedAtMillis }
                    .thenBy { it.app.label.lowercase() },
            )
            .toList()

    fun badgesFor(app: AppSnapshot, nowMillis: Long, config: AnalyzerConfig = AnalyzerConfig()): Set<Badge> {
        val badges = linkedSetOf<Badge>()
        if (app.canDrawOverlays) badges += Badge.SHOWS_OVER_OTHER_APPS
        val lastSeen = app.lastForegroundMillis
        if (lastSeen != null && nowMillis - lastSeen in 0..config.recentWindowMillis) {
            badges += Badge.OPEN_RECENTLY
        }
        val size = app.storageBytes
        if (size != null && size >= config.largeStorageBytes) badges += Badge.USES_LOTS_OF_SPACE
        if (!app.hasLauncherIcon) badges += Badge.HIDDEN_FROM_HOME_SCREEN
        return badges
    }
}
