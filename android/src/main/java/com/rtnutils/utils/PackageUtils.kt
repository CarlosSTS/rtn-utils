package com.rtnutils.utils

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build

object PackageUtils {

    fun getLauncherPackages(pm: PackageManager): Set<String> {
        val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val packageNames = LinkedHashSet<String>()
        for (ri in pm.queryIntentActivities(launcherIntent, 0)) {
            ri.activityInfo?.packageName?.let { packageNames.add(it) }
        }
        return packageNames
    }

    fun isSystemApp(appInfo: ApplicationInfo): Boolean =
        (appInfo.flags and
            (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0

    fun getVersionCode(pkgInfo: PackageInfo): Long {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pkgInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            pkgInfo.versionCode.toLong()
        }
    }

    fun getMinSdkVersion(appInfo: ApplicationInfo): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            appInfo.minSdkVersion
        } else {
            0
        }
    }

    fun getCategory(appInfo: ApplicationInfo): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            categoryToString(appInfo.category)
        } else {
            "undefined"
        }
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
}
