package io.github.rachitp.swatter.core

/**
 * Everything Swatter knows about one installed app at scan time.
 *
 * Fields that need the "Usage access" permission are null when it hasn't been granted,
 * so the analysis can still run with what is available.
 */
data class AppSnapshot(
    val packageName: String,
    val label: String,
    val installedAtMillis: Long,
    val isSystemApp: Boolean,
    /** True if the app is allowed to "display over other apps" (the permission pop-up ads rely on). */
    val canDrawOverlays: Boolean,
    /** True if the app has an icon in the home screen / app drawer. Adware often hides it. */
    val hasLauncherIcon: Boolean,
    /** Last time the app came to the foreground, from usage history. Null without Usage access. */
    val lastForegroundMillis: Long? = null,
    /** App + data + cache size in bytes. Null without Usage access. */
    val storageBytes: Long? = null,
)
