package com.rtnutils.utils

import android.app.ActivityManager
import android.content.Context
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap

object DeviceUtils {

    fun getMemoryInfo(context: Context): WritableMap {
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
}
