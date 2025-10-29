package com.pegel.wearos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pegel.wearos.ui.SettingsScreen
import com.pegel.wearos.ui.SettingsViewModel
import com.pegel.wearos.ui.theme.PegelTheme

/**
 * Activity for managing drink settings.
 * Allows users to select and reorder their active drinks.
 */
class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PegelTheme {
                val viewModel: SettingsViewModel = viewModel()
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { finish() }
                )
            }
        }
    }
}
