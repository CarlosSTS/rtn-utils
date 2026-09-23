package com.rtnutils.utils

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings

object PermissionUtils {

    fun hasAppOpPermission(context: Context, op: String): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
            ?: return false
        @Suppress("DEPRECATION")
        val mode = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(op, Process.myUid(), context.packageName)
            } else {
                appOps.checkOpNoThrow(op, Process.myUid(), context.packageName)
            }
        } catch (e: Exception) {
            return false
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun hasUsageAccessPermission(context: Context): Boolean =
        hasAppOpPermission(context, AppOpsManager.OPSTR_GET_USAGE_STATS)

    fun openSettings(context: Context, action: String): Boolean {
        return try {
            val intent = Intent(action)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun openUsageAccessSettings(context: Context): Boolean =
        openSettings(context, Settings.ACTION_USAGE_ACCESS_SETTINGS)

    fun openAppDetailsSettings(context: Context, packageName: String): Boolean {
        return try {
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null),
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }
}
