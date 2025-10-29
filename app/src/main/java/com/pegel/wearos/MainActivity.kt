package com.pegel.wearos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.ScalingLazyListState
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.material.dialog.Dialog
import androidx.wear.compose.material.rememberScalingLazyListState
import com.pegel.wearos.data.DrinkLog
import com.pegel.wearos.data.DrinkType
import com.pegel.wearos.ui.MainViewModel
import com.pegel.wearos.ui.theme.PegelTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PegelApp()
        }
    }
}

@Composable
fun PegelApp(
    viewModel: MainViewModel = viewModel()
) {
    PegelTheme {
        val todayDrinks by viewModel.todayDrinks.collectAsStateWithLifecycle()
        val drinkCounts by viewModel.drinkCounts.collectAsStateWithLifecycle()
        val totalDrinks by viewModel.totalDrinks.collectAsStateWithLifecycle()

        // State for reset confirmation dialog
        var showResetDialog by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
        ) {
            if (totalDrinks == 0) {
                // Empty state
                EmptyStateScreen()
            } else {
                // Main content
                DrinkLogScreen(
                    drinkLogs = todayDrinks.sortedByDescending { it.timestamp }, // Newest first
                    drinkCounts = drinkCounts,
                    totalDrinks = totalDrinks,
                    onResetClick = { showResetDialog = true }
                )
            }

            // Reset confirmation dialog
            ResetConfirmationDialog(
                showDialog = showResetDialog,
                onConfirm = {
                    viewModel.resetAllDrinks()
                    showResetDialog = false
                },
                onDismiss = { showResetDialog = false }
            )
        }
    }
}

@Composable
fun EmptyStateScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🍺",
                style = MaterialTheme.typography.display1,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No drinks yet",
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Use the tile to log",
                style = MaterialTheme.typography.caption3,
                color = MaterialTheme.colors.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun DrinkLogScreen(
    drinkLogs: List<DrinkLog>,
    drinkCounts: Map<DrinkType, Int>,
    totalDrinks: Int,
    onResetClick: () -> Unit,
    listState: ScalingLazyListState = rememberScalingLazyListState()
) {
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
        // Summary card at the top
        item {
            DrinkSummaryCard(
                drinkCounts = drinkCounts,
                totalDrinks = totalDrinks
            )
        }

        // Section header
        item {
            Text(
                text = "Today's Log",
                style = MaterialTheme.typography.caption1,
                color = MaterialTheme.colors.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        // Drink log items
        items(drinkLogs.size) { index ->
            DrinkLogItem(drinkLog = drinkLogs[index])
        }

        // Reset button
        item {
            Spacer(modifier = Modifier.height(8.dp))
            ResetButton(onClick = onResetClick)
        }
    }
}

@Composable
fun DrinkSummaryCard(
    drinkCounts: Map<DrinkType, Int>,
    totalDrinks: Int
) {
    Card(
        onClick = { /* No action needed */ },
        enabled = false,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Total count
            Text(
                text = "Total: $totalDrinks",
                style = MaterialTheme.typography.title3,
                color = MaterialTheme.colors.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Drink counts by type
            if (drinkCounts.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    drinkCounts.entries.forEachIndexed { index, (drinkType, count) ->
                        Text(
                            text = "${drinkType.emoji} $count",
                            style = MaterialTheme.typography.body1
                        )
                        if (index < drinkCounts.size - 1) {
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DrinkLogItem(drinkLog: DrinkLog) {
    Card(
        onClick = { /* No action needed */ },
        enabled = false,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time
            Text(
                text = drinkLog.getTimeOfDay(),
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onSurfaceVariant
            )

            // Emoji
            Text(
                text = drinkLog.drinkType.emoji,
                style = MaterialTheme.typography.display3,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun ResetButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        colors = ButtonDefaults.secondaryButtonColors()
    ) {
        Text(
            text = "Reset All",
            style = MaterialTheme.typography.button,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ResetConfirmationDialog(
    showDialog: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        showDialog = showDialog,
        onDismissRequest = onDismiss
    ) {
        Alert(
            title = {
                Text(
                    text = "Reset All?",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.onBackground
                )
            },
            content = {
                Text(
                    text = "This will clear all drinks logged today.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.onSurfaceVariant,
                    style = MaterialTheme.typography.body2
                )
            },
            positiveButton = {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.primaryButtonColors()
                ) {
                    Text("Yes")
                }
            },
            negativeButton = {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.secondaryButtonColors()
                ) {
                    Text("No")
                }
            }
        )
    }
}
