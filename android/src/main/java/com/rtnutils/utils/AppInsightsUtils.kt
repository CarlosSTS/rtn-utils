package com.rtnutils.utils

import android.app.ActivityManager
import android.app.AppOpsManager
import android.app.usage.StorageStatsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableMap
import java.io.File

/**
 * Collects installed-app metadata plus (when "Usage access" is granted) per-app
 * foreground time and storage footprint.
 *
 * Android exposes no Play-compliant API for the real-time RAM usage of other
 * apps on non-rooted devices, so "consumption" here means storage size and
 * foreground time — the closest per-app metrics that are actually available.
 */
object AppInsightsUtils {

    private const val DAY_MS = 24L * 60L * 60L * 1000L

    data class Options(
        val includeSystemApps: Boolean = false,
        val includeIcons: Boolean = false,
        val sortBy: String = "totalSize",
        val usagePeriod: String = "week",
        val limit: Int = 0,
    )

    /**
     * MIUI/HyperOS Settings report an app's size as the files in its install
     * directory (APKs, extracted native libs, .dm), leaving out the compiled
     * code in oat/ that StorageStats.appBytes includes.
     */
    private fun isXiaomi(): Boolean =
        Build.MANUFACTURER.equals("Xiaomi", ignoreCase = true)

    private fun installDirBytes(appInfo: ApplicationInfo): Long {
        val sourceDir = appInfo.sourceDir ?: return 0L
        val dir = File(sourceDir).parentFile ?: return 0L
        val listed = dirBytes(dir)
        if (listed > 0) return listed
        // Directory not listable: fall back to the APK files alone.
        val paths = listOf(sourceDir) + (appInfo.splitSourceDirs?.toList() ?: emptyList())
        return paths.sumOf { File(it).length() }
    }

    private fun dirBytes(dir: File): Long {
        val children = try {
            dir.listFiles()
        } catch (e: Exception) {
            null
        } ?: return 0L
        return children.sumOf { child ->
            when {
                child.isDirectory && child.name != "oat" -> dirBytes(child)
                child.isFile -> child.length()
                else -> 0L
            }
        }
    }

    fun hasUsageAccessPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
            ?: return false
        @Suppress("DEPRECATION")
        val mode = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName,
                )
            } else {
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName,
                )
            }
        } catch (e: Exception) {
            return false
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun openUsageAccessSettings(context: Context): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getDeviceMemoryInfo(context: Context): WritableMap {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = ActivityManager.MemoryInfo()
        am.getMemoryInfo(info)
        val used = (info.totalMem - info.availMem).coerceAtLeast(0L)
        return Arguments.createMap().apply {
            putDouble("totalBytes", info.totalMem.toDouble())
            putDouble("availableBytes", info.availMem.toDouble())
            putDouble("usedBytes", used.toDouble())
            putBoolean("lowMemory", info.lowMemory)
            putDouble("thresholdBytes", info.threshold.toDouble())
        }
    }

    fun getInstalledApps(context: Context, options: Options): WritableMap {
        val pm = context.packageManager
        val granted = hasUsageAccessPermission(context)

        val periodMs = when (options.usagePeriod.lowercase()) {
            "day" -> DAY_MS
            "month" -> 30L * DAY_MS
            "year" -> 365L * DAY_MS
            else -> 7L * DAY_MS
        }
        val end = System.currentTimeMillis()
        val start = end - periodMs

        // packageName -> [totalForegroundMs, lastTimeUsed]
        val usage = HashMap<String, LongArray>()
        val launches = HashMap<String, Int>()
        if (granted) {
            collectUsage(context, start, end, usage, launches)
        }

        val storageStatsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.getSystemService(Context.STORAGE_STATS_SERVICE) as? StorageStatsManager
        } else {
            null
        }

        val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolved = pm.queryIntentActivities(launcherIntent, 0)
        val packageNames = LinkedHashSet<String>()
        for (ri in resolved) {
            ri.activityInfo?.packageName?.let { packageNames.add(it) }
        }

        val rows = ArrayList<AppRow>(packageNames.size)
        for (pkg in packageNames) {
            val row = buildRow(
                context = context,
                packageName = pkg,
                options = options,
                granted = granted,
                usage = usage[pkg],
                launchCount = launches[pkg] ?: 0,
                storageStatsManager = storageStatsManager,
            ) ?: continue
            if (row.isSystemApp && !options.includeSystemApps) continue
            rows.add(row)
        }

        sortRows(rows, options.sortBy)

        val limited = if (options.limit > 0 && options.limit < rows.size) {
            rows.subList(0, options.limit)
        } else {
            rows
        }

        val appsArray: WritableArray = Arguments.createArray()
        for (row in limited) {
            appsArray.pushMap(row.toMap(options.includeIcons))
        }

        return Arguments.createMap().apply {
            putBoolean("usageAccessGranted", granted)
            putInt("totalCount", rows.size)
            putArray("apps", appsArray)
        }
    }

    private fun collectUsage(
        context: Context,
        start: Long,
        end: Long,
        usage: HashMap<String, LongArray>,
        launches: HashMap<String, Int>,
    ) {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return
        try {
            val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_BEST, start, end)
            if (stats != null) {
                for (s in stats) {
                    val entry = usage.getOrPut(s.packageName) { longArrayOf(0L, 0L) }
                    entry[0] += s.totalTimeInForeground
                    if (s.lastTimeUsed > entry[1]) entry[1] = s.lastTimeUsed
                }
            }
        } catch (e: Exception) {
            // ignore — usage stays empty
        }
        try {
            val events = usm.queryEvents(start, end)
            val event = UsageEvents.Event()
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                @Suppress("DEPRECATION")
                if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    launches[event.packageName] = (launches[event.packageName] ?: 0) + 1
                }
            }
        } catch (e: Exception) {
            // ignore — launch counts stay at 0
        }
    }

    private fun buildRow(
        context: Context,
        packageName: String,
        options: Options,
        granted: Boolean,
        usage: LongArray?,
        launchCount: Int,
        storageStatsManager: StorageStatsManager?,
    ): AppRow? {
        val pm = context.packageManager
        return try {
            @Suppress("DEPRECATION")
            val appInfo = pm.getApplicationInfo(packageName, 0)
            @Suppress("DEPRECATION")
            val pkgInfo = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)

            val isSystemApp = (appInfo.flags and
                (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0

            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pkgInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                pkgInfo.versionCode.toLong()
            }

            val category = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                categoryToString(appInfo.category)
            } else {
                "undefined"
            }

            var appSize = -1L
            var dataSize = -1L
            var cacheSize = -1L
            if (granted && storageStatsManager != null &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            ) {
                try {
                    val stats = storageStatsManager.queryStatsForPackage(
                        appInfo.storageUuid,
                        packageName,
                        Process.myUserHandle(),
                    )
                    appSize = if (isXiaomi()) {
                        installDirBytes(appInfo).takeIf { it > 0 } ?: stats.appBytes
                    } else {
                        stats.appBytes
                    }
                    dataSize = stats.dataBytes
                    cacheSize = stats.cacheBytes
                } catch (e: Exception) {
                    // leave sizes at -1
                }
            }

            val icon = if (options.includeIcons) {
                try {
                    IconUtils.getAppIconBase64(pm.getApplicationIcon(appInfo))
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }

            AppRow(
                packageName = packageName,
                appName = pm.getApplicationLabel(appInfo).toString(),
                versionName = pkgInfo.versionName ?: "",
                versionCode = versionCode,
                icon = icon,
                isSystemApp = isSystemApp,
                enabled = appInfo.enabled,
                firstInstallTime = pkgInfo.firstInstallTime,
                lastUpdateTime = pkgInfo.lastUpdateTime,
                targetSdkVersion = appInfo.targetSdkVersion,
                minSdkVersion = minSdk,
                category = category,
                permissionsCount = pkgInfo.requestedPermissions?.size ?: 0,
                usageTimeMs = usage?.get(0) ?: 0L,
                lastUsedTime = usage?.get(1) ?: 0L,
                launchCount = launchCount,
                appSizeBytes = appSize,
                dataSizeBytes = dataSize,
                cacheSizeBytes = cacheSize,
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun sortRows(rows: MutableList<AppRow>, sortBy: String) {
        val comparator = when (sortBy.lowercase()) {
            "usagetime" -> compareByDescending<AppRow> { it.usageTimeMs }
            "lastused" -> compareByDescending<AppRow> { it.lastUsedTime }
            "name" -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.appName }
            else -> compareByDescending<AppRow> { maxOf(it.totalSizeBytes(), 0L) }
        }
        rows.sortWith(comparator.thenBy(String.CASE_INSENSITIVE_ORDER) { it.appName })
    }

    private fun categoryToString(category: Int): String = when (category) {
        ApplicationInfo.CATEGORY_GAME -> "game"
        ApplicationInfo.CATEGORY_AUDIO -> "audio"
        ApplicationInfo.CATEGORY_VIDEO -> "video"
        ApplicationInfo.CATEGORY_IMAGE -> "image"
        ApplicationInfo.CATEGORY_SOCIAL -> "social"
        ApplicationInfo.CATEGORY_NEWS -> "news"
        ApplicationInfo.CATEGORY_MAPS -> "maps"
        ApplicationInfo.CATEGORY_PRODUCTIVITY -> "productivity"
        else -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                category == ApplicationInfo.CATEGORY_ACCESSIBILITY
            ) {
                "accessibility"
            } else {
                "undefined"
            }
        }
    }

    private data class AppRow(
        val packageName: String,
        val appName: String,
        val versionName: String,
        val versionCode: Long,
        val icon: String?,
        val isSystemApp: Boolean,
        val enabled: Boolean,
        val firstInstallTime: Long,
        val lastUpdateTime: Long,
        val targetSdkVersion: Int,
        val minSdkVersion: Int,
        val category: String,
        val permissionsCount: Int,
        val usageTimeMs: Long,
        val lastUsedTime: Long,
        val launchCount: Int,
        val appSizeBytes: Long,
        val dataSizeBytes: Long,
        val cacheSizeBytes: Long,
    ) {
        // StorageStats.dataBytes already includes cacheBytes, so cache is not added again.
        fun totalSizeBytes(): Long {
            if (appSizeBytes < 0 && dataSizeBytes < 0) return -1L
            return maxOf(appSizeBytes, 0L) + maxOf(dataSizeBytes, 0L)
        }

        fun toMap(includeIcons: Boolean): WritableMap = Arguments.createMap().apply {
            putString("packageName", packageName)
            putString("appName", appName)
            putString("versionName", versionName)
            putDouble("versionCode", versionCode.toDouble())
            if (includeIcons && icon != null) putString("icon", icon)
            putBoolean("isSystemApp", isSystemApp)
            putBoolean("enabled", enabled)
            putDouble("firstInstallTime", firstInstallTime.toDouble())
            putDouble("lastUpdateTime", lastUpdateTime.toDouble())
            putInt("targetSdkVersion", targetSdkVersion)
            putInt("minSdkVersion", minSdkVersion)
            putString("category", category)
            putInt("permissionsCount", permissionsCount)
            putDouble("usageTimeMs", usageTimeMs.toDouble())
            putDouble("lastUsedTime", lastUsedTime.toDouble())
            putInt("launchCount", launchCount)
            putDouble("appSizeBytes", appSizeBytes.toDouble())
            putDouble("dataSizeBytes", dataSizeBytes.toDouble())
            putDouble("cacheSizeBytes", cacheSizeBytes.toDouble())
            putDouble("totalSizeBytes", totalSizeBytes().toDouble())
        }
    }
}
