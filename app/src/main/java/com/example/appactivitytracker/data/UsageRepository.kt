package com.example.appactivitytracker.data

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Process
import java.util.Calendar

class UsageRepository(private val context: Context) {

    private val usageStatsManager: UsageStatsManager by lazy {
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    }

    fun hasUsagePermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun getTodayUsage(): Pair<Long, List<AppUsageData>> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        val pm = context.packageManager
        val appList = mutableListOf<AppUsageData>()

        stats?.filter { it.totalTimeInForeground > 0 }?.forEach { stat ->
            try {
                val appInfo = pm.getApplicationInfo(stat.packageName, 0)
                val appName = pm.getApplicationLabel(appInfo).toString()
                val icon = pm.getApplicationIcon(stat.packageName)
                val dailyUsage = getWeeklyUsageForApp(stat.packageName)
                val hourlyUsage = getHourlyUsageForApp(stat.packageName)
                appList.add(
                    AppUsageData(
                        packageName = stat.packageName,
                        appName = appName,
                        usageTimeMillis = stat.totalTimeInForeground,
                        icon = icon,
                        dailyUsage = dailyUsage,
                        hourlyUsage = hourlyUsage
                    )
                )
            } catch (e: PackageManager.NameNotFoundException) {
                // skip system apps without info
            }
        }

        appList.sortByDescending { it.usageTimeMillis }
        val total = appList.sumOf { it.usageTimeMillis }
        return Pair(total, appList)
    }

    fun getWeeklyTotalUsage(): List<Long> {
        val result = MutableList(7) { 0L }
        val calendar = Calendar.getInstance()

        // Go back to Sunday of this week
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0=Sun, 6=Sat
        calendar.add(Calendar.DAY_OF_YEAR, -dayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        for (i in 0..6) {
            val dayStart = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val dayEnd = calendar.timeInMillis

            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                dayStart,
                minOf(dayEnd, System.currentTimeMillis())
            )
            result[i] = stats?.sumOf { it.totalTimeInForeground } ?: 0L
        }
        return result
    }

    private fun getWeeklyUsageForApp(packageName: String): List<Long> {
        val result = MutableList(7) { 0L }
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
        calendar.add(Calendar.DAY_OF_YEAR, -dayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        for (i in 0..6) {
            val dayStart = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val dayEnd = calendar.timeInMillis

            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                dayStart,
                minOf(dayEnd, System.currentTimeMillis())
            )
            result[i] = stats?.filter { it.packageName == packageName }
                ?.sumOf { it.totalTimeInForeground } ?: 0L
        }
        return result
    }

    private fun getHourlyUsageForApp(packageName: String): List<Long> {
        val result = MutableList(24) { 0L }
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        for (hour in 0..23) {
            val hourStart = calendar.timeInMillis
            calendar.add(Calendar.HOUR_OF_DAY, 1)
            val hourEnd = calendar.timeInMillis

            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_BEST,
                hourStart,
                minOf(hourEnd, System.currentTimeMillis())
            )
            result[hour] = stats?.filter { it.packageName == packageName }
                ?.sumOf { it.totalTimeInForeground } ?: 0L
        }
        return result
    }
}