package com.pegel.wearos.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pegel.wearos.data.DrinkLog
import com.pegel.wearos.data.DrinkRepository
import com.pegel.wearos.data.DrinkType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the main activity.
 * Manages the state of drink logs and provides actions for the UI.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DrinkRepository(application)

    /**
     * StateFlow of today's drink logs, sorted by timestamp (newest first for display).
     */
    val todayDrinks: StateFlow<List<DrinkLog>> = repository.getTodayDrinks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * StateFlow of drink counts grouped by type.
     */
    val drinkCounts: StateFlow<Map<DrinkType, Int>> = repository.getDrinkCounts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    /**
     * StateFlow of the total number of drinks logged today.
     */
    val totalDrinks: StateFlow<Int> = repository.getTotalDrinksToday()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    /**
     * Reset all drink logs for today.
     * This is a suspending function that should be called from a coroutine.
     */
    fun resetAllDrinks() {
        viewModelScope.launch {
            repository.resetDrinks()
        }
    }

    /**
     * Log a new drink (useful for testing or future features).
     */
    fun logDrink(drinkType: DrinkType) {
        viewModelScope.launch {
            repository.logDrink(drinkType)
        }
    }
}
