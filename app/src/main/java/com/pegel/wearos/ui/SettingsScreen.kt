package com.pegel.wearos.ui

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Checkbox
import androidx.wear.compose.material.CheckboxDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.rememberScalingLazyListState
import com.pegel.wearos.data.DrinkType
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

/**
 * Settings Screen for configuring active drinks.
 * Supports drag & drop reordering and checkbox selection.
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        if (uiState.isLoading) {
            LoadingScreen()
        } else {
            SettingsContent(
                uiState = uiState,
                onToggleDrink = { drinkType -> viewModel.toggleDrink(drinkType) },
                onReorder = { from, to -> viewModel.reorderDrinks(from, to) },
                onSave = { viewModel.saveChanges { onNavigateBack() } },
                onCancel = {
                    viewModel.cancelChanges()
                    onNavigateBack()
                },
                onDismissMessage = { viewModel.clearValidationMessage() }
            )
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun SettingsContent(
    uiState: SettingsUiState,
    onToggleDrink: (DrinkType) -> Unit,
    onReorder: (Int, Int) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onDismissMessage: () -> Unit
) {
    val listState = rememberScalingLazyListState()
    val view = LocalView.current

    // Separate active and inactive drinks
    val activeDrinks = uiState.allDrinks
        .filter { it.isActive }
        .sortedBy { it.order }
    val inactiveDrinks = uiState.allDrinks
        .filter { !it.isActive }
        .sortedBy { it.drinkType.displayName }

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(
            top = 32.dp,
            bottom = 48.dp,
            start = 10.dp,
            end = 10.dp
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Header
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = "Active Drinks",
                    style = MaterialTheme.typography.title3,
                    color = MaterialTheme.colors.primary
                )
                Text(
                    text = "${uiState.activeCount}/${SettingsViewModel.MAX_ACTIVE_DRINKS}",
                    style = MaterialTheme.typography.caption1,
                    color = MaterialTheme.colors.onSurfaceVariant
                )
            }
        }

        // Validation message
        if (uiState.validationMessage != null) {
            item {
                ValidationMessage(
                    message = uiState.validationMessage,
                    onDismiss = onDismissMessage
                )
            }
        }

        // Active drinks section (reorderable)
        if (activeDrinks.isNotEmpty()) {
            item {
                Text(
                    text = "Active (drag to reorder)",
                    style = MaterialTheme.typography.caption2,
                    color = MaterialTheme.colors.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                )
            }

            items(activeDrinks.size) { index ->
                val drinkItem = activeDrinks[index]
                var isDragging by remember { mutableStateOf(false) }

                DraggableDrinkItem(
                    drinkItem = drinkItem,
                    isDragging = isDragging,
                    onToggle = { onToggleDrink(drinkItem.drinkType) },
                    onDragStart = {
                        isDragging = true
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    },
                    onDragEnd = { isDragging = false },
                    onReorder = { fromIndex, toIndex ->
                        onReorder(fromIndex, toIndex)
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    },
                    index = index,
                    itemCount = activeDrinks.size
                )
            }
        }

        // Inactive drinks section
        if (inactiveDrinks.isNotEmpty()) {
            item {
                Text(
                    text = "Available",
                    style = MaterialTheme.typography.caption2,
                    color = MaterialTheme.colors.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                )
            }

            items(inactiveDrinks.size) { index ->
                val drinkItem = inactiveDrinks[index]
                DrinkItemRow(
                    drinkItem = drinkItem,
                    onToggle = { onToggleDrink(drinkItem.drinkType) },
                    showDragHandle = false
                )
            }
        }

        // Action buttons
        item {
            Spacer(modifier = Modifier.height(8.dp))
            ActionButtons(
                onSave = onSave,
                onCancel = onCancel,
                hasChanges = uiState.hasChanges
            )
        }
    }
}

@Composable
fun DraggableDrinkItem(
    drinkItem: DrinkItem,
    isDragging: Boolean,
    onToggle: () -> Unit,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onReorder: (Int, Int) -> Unit,
    index: Int,
    itemCount: Int
) {
    var dragOffset by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationY = dragOffset
                alpha = if (isDragging) 0.7f else 1f
                scaleX = if (isDragging) 1.05f else 1f
                scaleY = if (isDragging) 1.05f else 1f
            }
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { onDragStart() },
                    onDragEnd = {
                        onDragEnd()
                        dragOffset = 0f
                    },
                    onDragCancel = {
                        onDragEnd()
                        dragOffset = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffset += dragAmount.y

                        // Calculate new index based on drag offset
                        val itemHeight = 60.dp.toPx()
                        val indexOffset = (dragOffset / itemHeight).toInt()
                        val newIndex = (index + indexOffset).coerceIn(0, itemCount - 1)

                        if (newIndex != index) {
                            onReorder(index, newIndex)
                            dragOffset = 0f
                        }
                    }
                )
            }
    ) {
        DrinkItemRow(
            drinkItem = drinkItem,
            onToggle = onToggle,
            showDragHandle = true,
            isDragging = isDragging
        )
    }
}

@Composable
fun DrinkItemRow(
    drinkItem: DrinkItem,
    onToggle: () -> Unit,
    showDragHandle: Boolean,
    isDragging: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(
                color = if (isDragging)
                    MaterialTheme.colors.surface.copy(alpha = 0.8f)
                else
                    MaterialTheme.colors.surface,
                shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Drag handle (for active drinks only)
        if (showDragHandle) {
            Text(
                text = "≡",
                style = MaterialTheme.typography.title2,
                color = MaterialTheme.colors.onSurfaceVariant,
                modifier = Modifier.width(20.dp),
                textAlign = TextAlign.Center
            )
        } else {
            Spacer(modifier = Modifier.width(20.dp))
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Checkbox
        Checkbox(
            checked = drinkItem.isActive,
            onCheckedChange = { onToggle() },
            modifier = Modifier.size(20.dp),
            colors = CheckboxDefaults.colors(
                checkedBoxColor = MaterialTheme.colors.primary,
                uncheckedBoxColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f)
            )
        )

        Spacer(modifier = Modifier.width(6.dp))

        // Icon
        Text(
            text = drinkItem.drinkType.defaultEmoji,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(6.dp))

        // Name
        Text(
            text = drinkItem.drinkType.displayName,
            style = MaterialTheme.typography.body2,
            fontSize = 12.sp,
            color = if (drinkItem.isActive)
                MaterialTheme.colors.onSurface
            else
                MaterialTheme.colors.onSurfaceVariant.copy(alpha = 0.6f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ValidationMessage(
    message: String,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colors.error.copy(alpha = 0.2f),
                shape = MaterialTheme.shapes.small
            )
            .padding(12.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.caption1,
            color = MaterialTheme.colors.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ActionButtons(
    onSave: () -> Unit,
    onCancel: () -> Unit,
    hasChanges: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Save button
        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            colors = if (hasChanges)
                ButtonDefaults.primaryButtonColors()
            else
                ButtonDefaults.secondaryButtonColors(),
            enabled = hasChanges
        ) {
            Text(
                text = "Save",
                style = MaterialTheme.typography.button,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Cancel button
        Button(
            onClick = onCancel,
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            colors = ButtonDefaults.secondaryButtonColors()
        ) {
            Text(
                text = "Cancel",
                style = MaterialTheme.typography.button,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
