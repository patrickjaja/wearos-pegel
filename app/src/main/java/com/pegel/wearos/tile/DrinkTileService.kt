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
import androidx.wear.protolayout.LayoutElementBuilders.FontStyle
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.pegel.wearos.data.ActiveDrinksRepository
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
 * This service displays a tile with clickable icon buttons representing active drink types.
 * The layout is dynamic based on the user's active drink preferences (1-5 drinks).
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
    private lateinit var activeDrinksRepository: ActiveDrinksRepository
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    companion object {
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
        activeDrinksRepository = ActiveDrinksRepository(applicationContext)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    /**
     * Called when the system requests the tile layout.
     * This method builds and returns the complete tile UI.
     *
     * Note: We use runBlocking here to fetch data synchronously.
     * This is acceptable for tiles because:
     * 1. DataStore reads are very fast (< 10ms typically)
     * 2. Tile requests are expected to complete quickly
     * 3. The alternative would be showing stale data
     */
    override fun onTileRequest(requestParams: RequestBuilders.TileRequest): ListenableFuture<TileBuilders.Tile> {
        // Fetch current drink count and active drinks synchronously
        val (totalCount, activeDrinks) = runBlocking(Dispatchers.IO) {
            try {
                val count = repository.getTotalDrinksToday().first()
                val drinks = activeDrinksRepository.getActiveDrinks().first()
                Pair(count, drinks)
            } catch (e: Exception) {
                // Default to 0 count and first 5 drinks on error
                Pair(0, DrinkType.entries.take(5))
            }
        }

        val singleTileTimeline = TimelineBuilders.Timeline.Builder()
            .addTimelineEntry(
                TimelineBuilders.TimelineEntry.Builder()
                    .setLayout(
                        LayoutElementBuilders.Layout.Builder()
                            .setRoot(createTileLayout(totalCount, activeDrinks))
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
     * Returns empty resources since we're using emoji text instead of images.
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
     * Dynamically adjusts layout based on number of active drinks:
     * - 1 drink: Single centered button
     * - 2 drinks: 1 row with 2 buttons
     * - 3 drinks: 2 rows (2 buttons + 1 button)
     * - 4 drinks: 2 rows (2 buttons + 2 buttons)
     * - 5 drinks: 2 rows (3 buttons + 2 buttons)
     *
     * @param totalCount The total number of drinks logged today
     * @param activeDrinks The list of active drinks to display
     */
    private fun createTileLayout(
        totalCount: Int,
        activeDrinks: List<DrinkType>
    ): LayoutElementBuilders.LayoutElement {
        val column = LayoutElementBuilders.Column.Builder()
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

        // Create dynamic layout based on number of active drinks
        when (activeDrinks.size) {
            1 -> {
                // Single centered button
                column.addContent(createButtonRow(
                    listOf(activeDrinks[0] to getActionId(activeDrinks[0]))
                ))
            }
            2 -> {
                // 1 row with 2 buttons
                column.addContent(createButtonRow(
                    activeDrinks.map { it to getActionId(it) }
                ))
            }
            3 -> {
                // 2 rows: 2 buttons + 1 button
                column.addContent(createButtonRow(
                    activeDrinks.take(2).map { it to getActionId(it) }
                ))
                column.addContent(createSpacing(SPACING))
                column.addContent(createButtonRow(
                    listOf(activeDrinks[2] to getActionId(activeDrinks[2]))
                ))
            }
            4 -> {
                // 2 rows: 2 buttons + 2 buttons
                column.addContent(createButtonRow(
                    activeDrinks.take(2).map { it to getActionId(it) }
                ))
                column.addContent(createSpacing(SPACING))
                column.addContent(createButtonRow(
                    activeDrinks.drop(2).map { it to getActionId(it) }
                ))
            }
            5 -> {
                // 2 rows: 3 buttons + 2 buttons
                column.addContent(createButtonRow(
                    activeDrinks.take(3).map { it to getActionId(it) }
                ))
                column.addContent(createSpacing(SPACING))
                column.addContent(createButtonRow(
                    activeDrinks.drop(3).map { it to getActionId(it) }
                ))
            }
        }

        return column.build()
    }

    /**
     * Maps a DrinkType to its corresponding action ID.
     * This is used for tile button click handling.
     */
    private fun getActionId(drinkType: DrinkType): String {
        return "action_${drinkType.name.lowercase()}"
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
                    .setText(drinkType.defaultEmoji)
                    .setFontStyle(
                        FontStyle.Builder()
                            .setSize(sp(24f))
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
