package com.pegel.wearos.data

import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneId

/**
 * Data class representing a single logged drink.
 *
 * @property timestamp The time when the drink was logged (milliseconds since epoch)
 * @property drinkType The type of drink that was logged
 */
@Serializable
data class DrinkLog(
    val timestamp: Long,
    val drinkType: DrinkType
) {
    companion object {
        /**
         * Create a new DrinkLog for the current moment.
         */
        fun now(drinkType: DrinkType): DrinkLog {
            return DrinkLog(
                timestamp = System.currentTimeMillis(),
                drinkType = drinkType
            )
        }
    }

    /**
     * Check if this drink log is from today (based on device's local timezone).
     */
    fun isFromToday(): Boolean {
        val instant = Instant.ofEpochMilli(timestamp)
        val logDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
        val today = LocalDate.now(ZoneId.systemDefault())
        return logDate == today
    }

    /**
     * Get the time of day this drink was logged as a formatted string (HH:mm).
     */
    fun getTimeOfDay(): String {
        val instant = Instant.ofEpochMilli(timestamp)
        val localTime = instant.atZone(ZoneId.systemDefault()).toLocalTime()
        return String.format("%02d:%02d", localTime.hour, localTime.minute)
    }
}
