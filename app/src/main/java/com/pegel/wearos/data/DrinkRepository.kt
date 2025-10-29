package com.pegel.wearos.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer

/**
 * Repository for managing drink logs.
 * Uses DataStore Preferences to persist data with JSON serialization.
 *
 * This repository provides a simple interface for logging drinks, retrieving
 * today's logs, getting drink counts, and resetting the drink log.
 */
class DrinkRepository(private val context: Context) {

    companion object {
        private const val DATASTORE_NAME = "drink_logs"
        private val DRINKS_KEY = stringPreferencesKey("drinks_list")

        // Create a singleton instance of the DataStore
        private val Context.drinkDataStore by preferencesDataStore(name = DATASTORE_NAME)
    }

    private val dataStore = context.drinkDataStore
    private val json = Json

    /**
     * Log a new drink with the current timestamp.
     * This function is suspendable and should be called from a coroutine.
     *
     * @param drinkType The type of drink to log
     */
    suspend fun logDrink(drinkType: DrinkType) {
        val newDrink = DrinkLog.now(drinkType)
        dataStore.edit { preferences ->
            val currentDrinks = getDrinksFromPreferences(preferences)
            val updatedDrinks = currentDrinks + newDrink
            saveDrinksToPreferences(preferences, updatedDrinks)
        }
    }

    /**
     * Get a Flow of today's drink logs, automatically filtered to only include drinks from today.
     * The flow emits updates whenever the data changes.
     *
     * @return Flow<List<DrinkLog>> An ordered list of drinks from today (oldest first)
     */
    fun getTodayDrinks(): Flow<List<DrinkLog>> {
        return dataStore.data.map { preferences ->
            getDrinksFromPreferences(preferences)
                .filter { it.isFromToday() }
                .sortedBy { it.timestamp }
        }
    }

    /**
     * Get a Flow of drink counts per type for today.
     * Automatically updates whenever the drink logs change.
     *
     * @return Flow<Map<DrinkType, Int>> A map of each drink type to its count
     */
    fun getDrinkCounts(): Flow<Map<DrinkType, Int>> {
        return getTodayDrinks().map { drinks ->
            drinks.groupingBy { it.drinkType }
                .eachCount()
                .withDefault { 0 }
        }
    }

    /**
     * Get the total count of drinks logged today.
     *
     * @return Flow<Int> The total number of drinks logged today
     */
    fun getTotalDrinksToday(): Flow<Int> {
        return getTodayDrinks().map { it.size }
    }

    /**
     * Reset all drink logs for the day.
     * This function is suspendable and should be called from a coroutine.
     */
    suspend fun resetDrinks() {
        dataStore.edit { preferences ->
            preferences.remove(DRINKS_KEY)
        }
    }

    /**
     * Clear all drink logs (including historical data).
     * This function is suspendable and should be called from a coroutine.
     */
    suspend fun clearAllDrinks() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    // Private helper functions

    /**
     * Retrieve all stored drinks from preferences.
     * If no drinks are stored, returns an empty list.
     */
    private fun getDrinksFromPreferences(preferences: Preferences): List<DrinkLog> {
        return try {
            val jsonString = preferences[DRINKS_KEY] ?: return emptyList()
            json.decodeFromString(ListSerializer(DrinkLog.serializer()), jsonString)
        } catch (e: Exception) {
            // Log parse error silently and return empty list
            // In production, you might want to log this error
            emptyList()
        }
    }

    /**
     * Save drinks list to preferences as JSON string.
     */
    private fun saveDrinksToPreferences(
        preferences: androidx.datastore.preferences.core.MutablePreferences,
        drinks: List<DrinkLog>
    ) {
        val jsonString = json.encodeToString(
            ListSerializer(DrinkLog.serializer()),
            drinks
        )
        preferences[DRINKS_KEY] = jsonString
    }
}
