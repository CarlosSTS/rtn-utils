package com.rtnutils.utils

import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import android.os.Process

object StorageUtils {

    data class AppStorage(
        val appBytes: Long,
        val dataBytes: Long,
        val cacheBytes: Long,
    )

    fun getStorageStatsManager(context: Context): StorageStatsManager? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.getSystemService(Context.STORAGE_STATS_SERVICE) as? StorageStatsManager
        } else {
            null
        }
    }

    fun queryAppStorage(manager: StorageStatsManager?, appInfo: ApplicationInfo): AppStorage? {
        if (manager == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return null
        return try {
            val stats = manager.queryStatsForPackage(
                appInfo.storageUuid,
                appInfo.packageName,
                Process.myUserHandle(),
            )
            AppStorage(
                appBytes = stats.appBytes,
                dataBytes = (stats.dataBytes - stats.cacheBytes).coerceAtLeast(0L),
                cacheBytes = stats.cacheBytes,
            )
        } catch (e: Exception) {
            null
        }
    }
}
