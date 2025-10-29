package com.pegel.wearos.data

import kotlinx.serialization.Serializable

/**
 * Enum representing the different types of alcoholic drinks that can be logged.
 * Each drink type has an emoji and display name for UI purposes.
 */
@Serializable
enum class DrinkType(
    val emoji: String,
    val displayName: String
) {
    BEER(emoji = "🍺", displayName = "Beer"),
    WINE(emoji = "🍷", displayName = "Wine"),
    SHOT(emoji = "🥃", displayName = "Shot"),
    COCKTAIL(emoji = "🍸", displayName = "Cocktail"),
    LONG_DRINK(emoji = "🍹", displayName = "Long Drink");

    companion object {
        /**
         * Get a drink type by its display name.
         * Useful for parsing user input or preferences.
         */
        fun fromDisplayName(name: String): DrinkType? {
            return entries.find { it.displayName.equals(name, ignoreCase = true) }
        }

        /**
         * Get all drink types as a list.
         */
        fun getAll(): List<DrinkType> = entries.toList()
    }
}
