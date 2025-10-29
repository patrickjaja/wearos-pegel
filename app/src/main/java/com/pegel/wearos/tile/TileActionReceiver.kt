package com.pegel.wearos.tile

import android.app.Activity
import android.os.Bundle
import androidx.wear.tiles.TileService
import com.pegel.wearos.data.DrinkRepository
import com.pegel.wearos.data.DrinkType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Receiver activity for handling tile button clicks.
 *
 * This activity is launched when a user taps a drink button on the tile.
 * It performs the following actions:
 * 1. Extracts the drink type from the intent extras
 * 2. Logs the drink using DrinkRepository
 * 3. Triggers haptic feedback
 * 4. Requests a tile update
 * 5. Finishes immediately (invisible to user)
 *
 * This pattern is necessary because Wear OS tiles require an activity
 * to handle click actions, but we want the interaction to feel instant
 * without actually opening an app screen.
 */
class TileActionReceiver : Activity() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Extract drink type from intent
        val drinkTypeName = intent.getStringExtra(DrinkTileService.EXTRA_DRINK_TYPE)

        if (drinkTypeName != null) {
            try {
                val drinkType = DrinkType.valueOf(drinkTypeName)
                handleDrinkTap(drinkType)
            } catch (e: IllegalArgumentException) {
                // Invalid drink type, just finish
                finish()
            }
        } else {
            // No drink type provided, just finish
            finish()
        }
    }

    /**
     * Handle a drink button tap by logging the drink, providing haptic feedback,
     * and updating the tile.
     */
    private fun handleDrinkTap(drinkType: DrinkType) {
        val repository = DrinkRepository(applicationContext)

        // Launch coroutine to log drink
        scope.launch {
            try {
                // Log the drink
                repository.logDrink(drinkType)

                // Trigger haptic feedback
                DrinkTileService.triggerHapticFeedback(applicationContext)

                // Request tile update
                TileService.getUpdater(applicationContext)
                    .requestUpdate(DrinkTileService::class.java)

            } finally {
                // Always finish the activity
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Note: We don't cancel the scope here because we need the coroutine
        // to complete even after the activity finishes. The coroutine is short-lived
        // and will complete quickly, so this won't cause a memory leak.
    }
}
