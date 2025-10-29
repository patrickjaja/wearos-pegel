package com.pegel.wearos.tile

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.DimensionBuilders.sp
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.pegel.wearos.data.DrinkRepository
import com.pegel.wearos.data.DrinkType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Wear OS Tile Service for the Pegel drinking tracker app.
 *
 * This service displays a tile with 5 clickable emoji buttons representing different drink types.
 * When a user taps a button:
 * 1. The drink is logged to the DrinkRepository
 * 2. Haptic feedback is triggered
 * 3. The tile is updated to show the new count
 *
 * The tile shows the total drink count for today and provides quick access to log drinks
 * without opening the full app.
 */
class DrinkTileService : TileService() {

    private lateinit var repository: DrinkRepository
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    companion object {
        // Action IDs for each drink type button
        private const val ACTION_CLICK_BEER = "action_beer"
        private const val ACTION_CLICK_WINE = "action_wine"
        private const val ACTION_CLICK_SHOT = "action_shot"
        private const val ACTION_CLICK_COCKTAIL = "action_cocktail"
        private const val ACTION_CLICK_LONG_DRINK = "action_long_drink"

        // Vibration duration for haptic feedback
        private const val VIBRATION_DURATION_MS = 50L

        // UI dimensions
        private const val BUTTON_SIZE = 48
        private const val TEXT_SIZE = 32
        private const val COUNT_TEXT_SIZE = 16
        private const val SPACING = 4

        // Tile resources
        private const val RESOURCES_VERSION = "1"
        const val EXTRA_DRINK_TYPE = "drink_type"

        /**
         * Trigger haptic feedback for user interaction.
         * Called from TileActionReceiver after logging a drink.
         */
        fun triggerHapticFeedback(context: Context) {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.let {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    it.vibrate(
                        VibrationEffect.createOneShot(
                            VIBRATION_DURATION_MS,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    )
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(VIBRATION_DURATION_MS)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        repository = DrinkRepository(applicationContext)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    /**
     * Called when the system requests the tile layout.
     * This method builds and returns the complete tile UI.
     *
     * Note: We use runBlocking here to fetch the count synchronously.
     * This is acceptable for tiles because:
     * 1. DataStore reads are very fast (< 10ms typically)
     * 2. Tile requests are expected to complete quickly
     * 3. The alternative would be showing stale data
     */
    override fun onTileRequest(requestParams: RequestBuilders.TileRequest): ListenableFuture<TileBuilders.Tile> {
        // Fetch current drink count synchronously
        val totalCount = runBlocking(Dispatchers.IO) {
            try {
                repository.getTotalDrinksToday().first()
            } catch (e: Exception) {
                0 // Default to 0 on error
            }
        }

        val singleTileTimeline = TimelineBuilders.Timeline.Builder()
            .addTimelineEntry(
                TimelineBuilders.TimelineEntry.Builder()
                    .setLayout(
                        LayoutElementBuilders.Layout.Builder()
                            .setRoot(createTileLayout(totalCount))
                            .build()
                    )
                    .build()
            )
            .build()

        val tile = TileBuilders.Tile.Builder()
            .setResourcesVersion(RESOURCES_VERSION)
            .setFreshnessIntervalMillis(0) // Always fresh when user interaction occurs
            .setTileTimeline(singleTileTimeline)
            .build()

        return Futures.immediateFuture(tile)
    }

    /**
     * Called when the system requests tile resources.
     * Currently returns an empty resources bundle as we're using emoji text.
     */
    override fun onTileResourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest
    ): ListenableFuture<ResourceBuilders.Resources> {
        val resources = ResourceBuilders.Resources.Builder()
            .setVersion(RESOURCES_VERSION)
            .build()
        return Futures.immediateFuture(resources)
    }


    /**
     * Creates the main tile layout with drink buttons and count display.
     *
     * @param totalCount The total number of drinks logged today
     */
    private fun createTileLayout(totalCount: Int): LayoutElementBuilders.LayoutElement {
        return LayoutElementBuilders.Column.Builder()
            .setWidth(dp(192f))
            .setHeight(dp(192f))
            .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
            .setModifiers(
                ModifiersBuilders.Modifiers.Builder()
                    .setPadding(
                        ModifiersBuilders.Padding.Builder()
                            .setAll(dp(8f))
                            .build()
                    )
                    .build()
            )
            // Total count display at top
            .addContent(createCountDisplay(totalCount))
            .addContent(createSpacing(SPACING))
            // Row 1: Beer, Wine, Shot
            .addContent(createButtonRow(
                listOf(
                    DrinkType.BEER to ACTION_CLICK_BEER,
                    DrinkType.WINE to ACTION_CLICK_WINE,
                    DrinkType.SHOT to ACTION_CLICK_SHOT
                )
            ))
            .addContent(createSpacing(SPACING))
            // Row 2: Cocktail, Long Drink
            .addContent(createButtonRow(
                listOf(
                    DrinkType.COCKTAIL to ACTION_CLICK_COCKTAIL,
                    DrinkType.LONG_DRINK to ACTION_CLICK_LONG_DRINK
                )
            ))
            .build()
    }

    /**
     * Creates a row of drink buttons.
     */
    private fun createButtonRow(
        drinks: List<Pair<DrinkType, String>>
    ): LayoutElementBuilders.LayoutElement {
        val row = LayoutElementBuilders.Row.Builder()
            .setWidth(DimensionBuilders.wrap())
            .setHeight(DimensionBuilders.wrap())
            .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_CENTER)

        drinks.forEachIndexed { index, (drinkType, actionId) ->
            if (index > 0) {
                row.addContent(createSpacing(SPACING))
            }
            row.addContent(createDrinkButton(drinkType, actionId))
        }

        return row.build()
    }

    /**
     * Creates a clickable drink button with emoji and tap action.
     */
    private fun createDrinkButton(
        drinkType: DrinkType,
        actionId: String
    ): LayoutElementBuilders.LayoutElement {
        return LayoutElementBuilders.Box.Builder()
            .setWidth(dp(BUTTON_SIZE.toFloat()))
            .setHeight(dp(BUTTON_SIZE.toFloat()))
            .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
            .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_CENTER)
            .setModifiers(
                ModifiersBuilders.Modifiers.Builder()
                    .setClickable(
                        ModifiersBuilders.Clickable.Builder()
                            .setId(actionId)
                            .setOnClick(
                                ActionBuilders.LaunchAction.Builder()
                                    .setAndroidActivity(
                                        ActionBuilders.AndroidActivity.Builder()
                                            .setClassName(TileActionReceiver::class.java.name)
                                            .setPackageName(packageName)
                                            .addKeyToExtraMapping(
                                                EXTRA_DRINK_TYPE,
                                                ActionBuilders.AndroidStringExtra.Builder()
                                                    .setValue(drinkType.name)
                                                    .build()
                                            )
                                            .build()
                                    )
                                    .build()
                            )
                            .build()
                    )
                    .setBackground(
                        ModifiersBuilders.Background.Builder()
                            .setColor(argb(0x40FFFFFF)) // Semi-transparent white
                            .setCorner(
                                ModifiersBuilders.Corner.Builder()
                                    .setRadius(dp(24f))
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .addContent(
                LayoutElementBuilders.Text.Builder()
                    .setText(drinkType.emoji)
                    .setFontStyle(
                        LayoutElementBuilders.FontStyle.Builder()
                            .setSize(sp(TEXT_SIZE.toFloat()))
                            .build()
                    )
                    .build()
            )
            .build()
    }

    /**
     * Creates the total count display at the top of the tile.
     */
    private fun createCountDisplay(count: Int): LayoutElementBuilders.LayoutElement {
        return LayoutElementBuilders.Text.Builder()
            .setText("Today: $count")
            .setFontStyle(
                LayoutElementBuilders.FontStyle.Builder()
                    .setSize(sp(COUNT_TEXT_SIZE.toFloat()))
                    .setColor(argb(0xFFFFFFFF.toInt()))
                    .build()
            )
            .build()
    }

    /**
     * Creates a spacer element for layout spacing.
     */
    private fun createSpacing(size: Int): LayoutElementBuilders.LayoutElement {
        return LayoutElementBuilders.Spacer.Builder()
            .setWidth(dp(size.toFloat()))
            .setHeight(dp(size.toFloat()))
            .build()
    }
}
