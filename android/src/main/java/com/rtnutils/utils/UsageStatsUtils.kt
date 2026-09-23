package com.rtnutils.utils

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context

object UsageStatsUtils {

    const val DAY_MS = 24L * 60L * 60L * 1000L

    data class AppUsage(
        var foregroundTimeMs: Long = 0L,
        var lastTimeUsed: Long = 0L,
        var launchCount: Int = 0,
    )

    fun periodToMillis(period: String): Long = when (period.lowercase()) {
        "day" -> DAY_MS
        "month" -> 30L * DAY_MS
        "year" -> 365L * DAY_MS
        else -> 7L * DAY_MS
    }

    fun queryAppUsage(context: Context, start: Long, end: Long): Map<String, AppUsage> {
        val usage = HashMap<String, AppUsage>()
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return usage
        try {
            val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_BEST, start, end)
            if (stats != null) {
                for (s in stats) {
                    val entry = usage.getOrPut(s.packageName) { AppUsage() }
                    entry.foregroundTimeMs += s.totalTimeInForeground
                    if (s.lastTimeUsed > entry.lastTimeUsed) entry.lastTimeUsed = s.lastTimeUsed
                }
            }
        } catch (e: Exception) {
        }
        try {
            val events = usm.queryEvents(start, end)
            val event = UsageEvents.Event()
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                @Suppress("DEPRECATION")
                if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    usage.getOrPut(event.packageName) { AppUsage() }.launchCount++
                }
            }
        } catch (e: Exception) {
        }
        return usage
    }
}
