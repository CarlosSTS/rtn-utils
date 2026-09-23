package com.rtnutils.utils

import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableMap

object AppInsightsUtils {

    data class Options(
        val includeSystemApps: Boolean = false,
        val includeIcons: Boolean = false,
        val sortBy: String = "totalSize",
        val usagePeriod: String = "week",
        val limit: Int = 0,
    )

    fun getInstalledApps(context: Context, options: Options): WritableMap {
        val pm = context.packageManager
        val granted = PermissionUtils.hasUsageAccessPermission(context)

        val end = System.currentTimeMillis()
        val start = end - UsageStatsUtils.periodToMillis(options.usagePeriod)

        val usage = if (granted) {
            UsageStatsUtils.queryAppUsage(context, start, end)
        } else {
            emptyMap()
        }

        val storageStatsManager = if (granted) StorageUtils.getStorageStatsManager(context) else null

        val packageNames = PackageUtils.getLauncherPackages(pm)
        val rows = ArrayList<AppRow>(packageNames.size)
        for (pkg in packageNames) {
            val row = buildRow(
                pm = pm,
                packageName = pkg,
                options = options,
                usage = usage[pkg],
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

    private fun buildRow(
        pm: PackageManager,
        packageName: String,
        options: Options,
        usage: UsageStatsUtils.AppUsage?,
        storageStatsManager: StorageStatsManager?,
    ): AppRow? {
        return try {
            @Suppress("DEPRECATION")
            val appInfo = pm.getApplicationInfo(packageName, 0)
            @Suppress("DEPRECATION")
            val pkgInfo = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)

            val storage = StorageUtils.queryAppStorage(storageStatsManager, appInfo)

            val icon = if (options.includeIcons) {
                IconUtils.getAppIconBase64OrNull(pm, appInfo)
            } else {
                null
            }

            AppRow(
                packageName = packageName,
                appName = pm.getApplicationLabel(appInfo).toString(),
                versionName = pkgInfo.versionName ?: "",
                versionCode = PackageUtils.getVersionCode(pkgInfo),
                icon = icon,
                isSystemApp = PackageUtils.isSystemApp(appInfo),
                enabled = appInfo.enabled,
                firstInstallTime = pkgInfo.firstInstallTime,
                lastUpdateTime = pkgInfo.lastUpdateTime,
                targetSdkVersion = appInfo.targetSdkVersion,
                minSdkVersion = PackageUtils.getMinSdkVersion(appInfo),
                category = PackageUtils.getCategory(appInfo),
                permissionsCount = pkgInfo.requestedPermissions?.size ?: 0,
                usageTimeMs = usage?.foregroundTimeMs ?: 0L,
                lastUsedTime = usage?.lastTimeUsed ?: 0L,
                launchCount = usage?.launchCount ?: 0,
                appSizeBytes = storage?.appBytes ?: -1L,
                dataSizeBytes = storage?.dataBytes ?: -1L,
                cacheSizeBytes = storage?.cacheBytes ?: -1L,
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
        fun totalSizeBytes(): Long {
            if (appSizeBytes < 0 && dataSizeBytes < 0 && cacheSizeBytes < 0) return -1L
            return maxOf(appSizeBytes, 0L) + maxOf(dataSizeBytes, 0L) + maxOf(cacheSizeBytes, 0L)
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
