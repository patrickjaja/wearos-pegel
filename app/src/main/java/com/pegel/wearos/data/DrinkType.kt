package com.pegel.wearos.data

import androidx.annotation.DrawableRes
import com.pegel.wearos.R
import kotlinx.serialization.Serializable

/**
 * Enum representing the different types of alcoholic drinks that can be logged.
 * Each drink type has a drawable icon, emoji fallback, and display name for UI purposes.
 */
@Serializable
enum class DrinkType(
    @DrawableRes val iconRes: Int,
    val defaultEmoji: String,
    val displayName: String
) {
    BEER(
        iconRes = R.drawable.drink_beer_glas,
        defaultEmoji = "🍺",
        displayName = "Beer"
    ),
    WINE(
        iconRes = R.drawable.drink_wine_glas,
        defaultEmoji = "🍷",
        displayName = "Wine"
    ),
    SHOT(
        iconRes = R.drawable.drink_shot_glass,
        defaultEmoji = "🥃",
        displayName = "Shot"
    ),
    COCKTAIL(
        iconRes = R.drawable.drink_cocktail_glas,
        defaultEmoji = "🍸",
        displayName = "Cocktail"
    ),
    LONG_DRINK(
        iconRes = R.drawable.drink_long_drink_glass,
        defaultEmoji = "🍹",
        displayName = "Long Drink"
    ),
    BEER_TOWER(
        iconRes = R.drawable.drink_beer_tower,
        defaultEmoji = "🍺🗼",
        displayName = "Beer Tower"
    ),
    VODKA_PITCHER(
        iconRes = R.drawable.drink_vodka_pitcher_with_straws,
        defaultEmoji = "🍹🥤",
        displayName = "Vodka Pitcher"
    ),
    VODKA_TOWER(
        iconRes = R.drawable.drink_vodka_tower,
        defaultEmoji = "🍸🗼",
        displayName = "Vodka Tower"
    );

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
