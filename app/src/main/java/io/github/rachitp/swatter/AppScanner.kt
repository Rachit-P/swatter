package io.github.rachitp.swatter

import android.app.AppOpsManager
import android.app.usage.StorageStatsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.os.UserHandle
import io.github.rachitp.swatter.core.AppSnapshot

/** Reads what Android lets a normal app see about other installed apps. */
class AppScanner(private val context: Context) {
    private val pm: PackageManager = context.packageManager
    private val appOps = context.getSystemService(AppOpsManager::class.java)!!

    fun hasUsageAccess(): Boolean =
        opMode(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName) == AppOpsManager.MODE_ALLOWED

    fun scan(nowMillis: Long = System.currentTimeMillis(), recentWindowMillis: Long = 15 * 60 * 1000L): List<AppSnapshot> {
        val usage = hasUsageAccess()
        val lastForeground = if (usage) lastForegroundTimes(nowMillis - recentWindowMillis, nowMillis) else emptyMap()
        return installedApps().map { info ->
            val pkg = info.packageName
            AppSnapshot(
                packageName = pkg,
                label = info.loadLabel(pm).toString(),
                installedAtMillis = firstInstallTime(pkg),
                isSystemApp = info.flags and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0,
                canDrawOverlays = canDrawOverlays(info),
                hasLauncherIcon = pm.getLaunchIntentForPackage(pkg) != null,
                lastForegroundMillis = lastForeground[pkg],
                storageBytes = if (usage) storageBytes(info) else null,
            )
        }
    }

    @Suppress("DEPRECATION")
    private fun installedApps(): List<ApplicationInfo> =
        if (Build.VERSION.SDK_INT >= 33) {
            pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
        } else {
            pm.getInstalledApplications(0)
        }

    @Suppress("DEPRECATION")
    private fun firstInstallTime(pkg: String): Long = try {
        if (Build.VERSION.SDK_INT >= 33) {
            pm.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(0)).firstInstallTime
        } else {
            pm.getPackageInfo(pkg, 0).firstInstallTime
        }
    } catch (e: PackageManager.NameNotFoundException) {
        0L
    }

    /** "Display over other apps" is an app-op, so we read the op mode rather than the permission. */
    fun canDrawOverlays(info: ApplicationInfo): Boolean =
        when (opMode(AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW, info.uid, info.packageName)) {
            AppOpsManager.MODE_ALLOWED -> true
            AppOpsManager.MODE_DEFAULT ->
                pm.checkPermission(android.Manifest.permission.SYSTEM_ALERT_WINDOW, info.packageName) ==
                    PackageManager.PERMISSION_GRANTED
            else -> false
        }

    /** Raw overlay op mode for another package, for test diagnostics. */
    fun overlayModeForDebug(pkg: String): Int {
        val uid = pm.getApplicationInfo(pkg, 0).uid
        return opMode(AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW, uid, pkg)
    }

    @Suppress("DEPRECATION")
    private fun opMode(op: String, uid: Int, pkg: String): Int = try {
        if (Build.VERSION.SDK_INT >= 29) {
            appOps.unsafeCheckOpNoThrow(op, uid, pkg)
        } else {
            appOps.checkOpNoThrow(op, uid, pkg)
        }
    } catch (e: SecurityException) {
        AppOpsManager.MODE_ERRORED
    }

    private fun lastForegroundTimes(from: Long, to: Long): Map<String, Long> {
        val usm = context.getSystemService(UsageStatsManager::class.java) ?: return emptyMap()
        val result = HashMap<String, Long>()
        val events = usm.queryEvents(from, to)
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            @Suppress("DEPRECATION")
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                result[event.packageName] = maxOf(result[event.packageName] ?: 0L, event.timeStamp)
            }
        }
        return result
    }

    private fun storageBytes(info: ApplicationInfo): Long? = try {
        val stats = context.getSystemService(StorageStatsManager::class.java)!!
            .queryStatsForPackage(info.storageUuid, info.packageName, UserHandle.getUserHandleForUid(info.uid))
        stats.appBytes + stats.dataBytes + stats.cacheBytes
    } catch (e: Exception) {
        null
    }
}
