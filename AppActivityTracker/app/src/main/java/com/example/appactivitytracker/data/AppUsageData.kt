package com.example.appactivitytracker.data

import android.graphics.drawable.Drawable

data class AppUsageData(
    val packageName: String,
    val appName: String,
    val usageTimeMillis: Long,
    val icon: Drawable?,
    val dailyUsage: List<Long> = emptyList(),    // Usage per day (Sun-Sat) in millis
    val hourlyUsage: List<Long> = emptyList()    // Usage per hour (0-23) in millis
) {
    fun getFormattedTime(): String {
        val totalMinutes = usageTimeMillis / (1000 * 60)
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours > 0 && minutes > 0 -> "$hours hr, $minutes min"
            hours > 0 -> "$hours hr"
            minutes > 0 -> "$minutes min"
            else -> "Less than 1 minute"
        }
    }
}
