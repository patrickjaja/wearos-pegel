package com.pegel.wearos.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

/**
 * Repository for managing user's active drinks preferences.
 * Uses DataStore Preferences to persist the ordered list of active drink types.
 *
 * This repository allows users to customize which drinks appear in their UI,
 * supporting 1-5 active drinks at a time.
 */
class ActiveDrinksRepository(private val context: Context) {

    companion object {
        private const val DATASTORE_NAME = "active_drinks_preferences"
        private val ACTIVE_DRINKS_KEY = stringPreferencesKey("active_drinks_order")

        // Default active drinks (first 5 from enum)
        private val DEFAULT_ACTIVE_DRINKS = listOf(
            DrinkType.BEER,
            DrinkType.WINE,
            DrinkType.SHOT,
            DrinkType.COCKTAIL,
            DrinkType.LONG_DRINK
        )

        // Create a singleton instance of the DataStore
        private val Context.activeDrinksDataStore by preferencesDataStore(name = DATASTORE_NAME)
    }

    private val dataStore = context.activeDrinksDataStore
    private val json = Json

    /**
     * Get the ordered list of active drink types.
     * Returns a Flow that emits updates whenever the preferences change.
     * Defaults to the first 5 drink types if no preferences are set.
     *
     * @return Flow<List<DrinkType>> An ordered list of 1-5 active drink types
     */
    fun getActiveDrinks(): Flow<List<DrinkType>> {
        return dataStore.data.map { preferences ->
            try {
                val jsonString = preferences[ACTIVE_DRINKS_KEY]
                if (jsonString != null) {
                    val drinkNames = json.decodeFromString(
                        ListSerializer(String.serializer()),
                        jsonString
                    )
                    // Convert string names back to DrinkType enum values
                    drinkNames.mapNotNull { name ->
                        try {
                            DrinkType.valueOf(name)
                        } catch (e: IllegalArgumentException) {
                            // Handle case where stored drink type no longer exists
                            null
                        }
                    }.takeIf { it.isNotEmpty() } ?: DEFAULT_ACTIVE_DRINKS
                } else {
                    DEFAULT_ACTIVE_DRINKS
                }
            } catch (e: Exception) {
                // Log parse error silently and return default list
                // In production, you might want to log this error
                DEFAULT_ACTIVE_DRINKS
            }
        }
    }

    /**
     * Set the ordered list of active drink types.
     * This function is suspendable and should be called from a coroutine.
     *
     * @param drinks List of 1-5 drink types to set as active
     * @throws IllegalArgumentException if the list is empty or has more than 5 items
     */
    suspend fun setActiveDrinks(drinks: List<DrinkType>) {
        require(drinks.isNotEmpty()) { "Active drinks list cannot be empty" }
        require(drinks.size <= 5) { "Active drinks list cannot have more than 5 items" }
        require(drinks.distinct().size == drinks.size) { "Active drinks list cannot contain duplicates" }

        dataStore.edit { preferences ->
            // Convert enum values to their string names for storage
            val drinkNames = drinks.map { it.name }
            val jsonString = json.encodeToString(
                ListSerializer(String.serializer()),
                drinkNames
            )
            preferences[ACTIVE_DRINKS_KEY] = jsonString
        }
    }

    /**
     * Check if a specific drink type is currently active.
     * This function is suspendable and should be called from a coroutine.
     *
     * @param drinkType The drink type to check
     * @return Boolean true if the drink type is in the active list
     */
    suspend fun isActive(drinkType: DrinkType): Boolean {
        return getActiveDrinks().first().contains(drinkType)
    }

    /**
     * Reset active drinks to default (first 5 drink types).
     * This function is suspendable and should be called from a coroutine.
     */
    suspend fun resetToDefaults() {
        setActiveDrinks(DEFAULT_ACTIVE_DRINKS)
    }

    /**
     * Add a drink type to the active list if not already present and space is available.
     * This function is suspendable and should be called from a coroutine.
     *
     * @param drinkType The drink type to add
     * @return Boolean true if the drink was added, false if already present or list is full
     */
    suspend fun addActiveDrink(drinkType: DrinkType): Boolean {
        val currentDrinks = getActiveDrinks().first()
        if (currentDrinks.contains(drinkType) || currentDrinks.size >= 5) {
            return false
        }
        setActiveDrinks(currentDrinks + drinkType)
        return true
    }

    /**
     * Remove a drink type from the active list.
     * This function is suspendable and should be called from a coroutine.
     *
     * @param drinkType The drink type to remove
     * @return Boolean true if the drink was removed, false if not present or would make list empty
     */
    suspend fun removeActiveDrink(drinkType: DrinkType): Boolean {
        val currentDrinks = getActiveDrinks().first()
        if (!currentDrinks.contains(drinkType) || currentDrinks.size <= 1) {
            return false
        }
        setActiveDrinks(currentDrinks.filter { it != drinkType })
        return true
    }

    /**
     * Reorder active drinks by moving a drink to a new position.
     * This function is suspendable and should be called from a coroutine.
     *
     * @param drinkType The drink type to move
     * @param newPosition The new position (0-based index)
     * @return Boolean true if reordered successfully, false if drink not found or invalid position
     */
    suspend fun reorderActiveDrink(drinkType: DrinkType, newPosition: Int): Boolean {
        val currentDrinks = getActiveDrinks().first().toMutableList()
        val oldIndex = currentDrinks.indexOf(drinkType)

        if (oldIndex == -1 || newPosition < 0 || newPosition >= currentDrinks.size) {
            return false
        }

        currentDrinks.removeAt(oldIndex)
        currentDrinks.add(newPosition, drinkType)
        setActiveDrinks(currentDrinks)
        return true
    }
}
