package com.pegel.wearos.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pegel.wearos.data.ActiveDrinksRepository
import com.pegel.wearos.data.DrinkType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.wear.tiles.TileService
import com.pegel.wearos.tile.DrinkTileService

/**
 * UI state for the Settings screen.
 */
data class SettingsUiState(
    val allDrinks: List<DrinkItem> = emptyList(),
    val activeCount: Int = 0,
    val hasChanges: Boolean = false,
    val validationMessage: String? = null,
    val isLoading: Boolean = true
)

/**
 * Represents a drink item in the settings list.
 */
data class DrinkItem(
    val drinkType: DrinkType,
    val isActive: Boolean,
    val order: Int // Position in active list, -1 if not active
)

/**
 * ViewModel for the Settings screen.
 * Manages active drinks selection and reordering.
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val activeDrinksRepository = ActiveDrinksRepository(application)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // Store original state for change detection
    private var originalActiveDrinks: List<DrinkType> = emptyList()

    companion object {
        const val MIN_ACTIVE_DRINKS = 1
        const val MAX_ACTIVE_DRINKS = 5
    }

    init {
        loadSettings()
    }

    /**
     * Load current settings from repository.
     */
    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val activeDrinks = activeDrinksRepository.getActiveDrinks().first()
            originalActiveDrinks = activeDrinks

            updateUiState(activeDrinks)

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    /**
     * Update UI state with current active drinks.
     */
    private fun updateUiState(activeDrinks: List<DrinkType>) {
        val allDrinkTypes = DrinkType.getAll()
        val drinkItems = allDrinkTypes.map { drinkType ->
            val order = activeDrinks.indexOf(drinkType)
            DrinkItem(
                drinkType = drinkType,
                isActive = order >= 0,
                order = order
            )
        }

        _uiState.value = _uiState.value.copy(
            allDrinks = drinkItems,
            activeCount = activeDrinks.size,
            hasChanges = activeDrinks != originalActiveDrinks
        )
    }

    /**
     * Get current active drinks in order.
     */
    private fun getCurrentActiveDrinks(): List<DrinkType> {
        return _uiState.value.allDrinks
            .filter { it.isActive }
            .sortedBy { it.order }
            .map { it.drinkType }
    }

    /**
     * Toggle a drink's active state.
     * Enforces min/max constraints.
     */
    fun toggleDrink(drinkType: DrinkType) {
        val currentActive = getCurrentActiveDrinks()
        val isCurrentlyActive = currentActive.contains(drinkType)

        if (isCurrentlyActive) {
            // Trying to deactivate
            if (currentActive.size <= MIN_ACTIVE_DRINKS) {
                _uiState.value = _uiState.value.copy(
                    validationMessage = "At least $MIN_ACTIVE_DRINKS drink must be active"
                )
                return
            }
            // Remove from active
            val newActive = currentActive.filter { it != drinkType }
            updateUiState(newActive)
        } else {
            // Trying to activate
            if (currentActive.size >= MAX_ACTIVE_DRINKS) {
                _uiState.value = _uiState.value.copy(
                    validationMessage = "Maximum $MAX_ACTIVE_DRINKS drinks allowed"
                )
                return
            }
            // Add to active
            val newActive = currentActive + drinkType
            updateUiState(newActive)
        }

        // Clear validation message after successful toggle
        _uiState.value = _uiState.value.copy(validationMessage = null)
    }

    /**
     * Reorder active drinks by moving an item from one position to another.
     */
    fun reorderDrinks(fromIndex: Int, toIndex: Int) {
        val currentActive = getCurrentActiveDrinks().toMutableList()

        if (fromIndex < 0 || fromIndex >= currentActive.size ||
            toIndex < 0 || toIndex >= currentActive.size) {
            return
        }

        val item = currentActive.removeAt(fromIndex)
        currentActive.add(toIndex, item)

        updateUiState(currentActive)
    }

    /**
     * Save changes to repository.
     */
    fun saveChanges(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val activeDrinks = getCurrentActiveDrinks()
                activeDrinksRepository.setActiveDrinks(activeDrinks)

                // Request tile update to reflect new active drinks
                TileService.getUpdater(getApplication())
                    .requestUpdate(DrinkTileService::class.java)

                originalActiveDrinks = activeDrinks
                _uiState.value = _uiState.value.copy(
                    hasChanges = false,
                    validationMessage = null
                )
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    validationMessage = "Failed to save: ${e.message}"
                )
            }
        }
    }

    /**
     * Cancel changes and revert to original state.
     */
    fun cancelChanges() {
        updateUiState(originalActiveDrinks)
        _uiState.value = _uiState.value.copy(
            validationMessage = null
        )
    }

    /**
     * Clear validation message.
     */
    fun clearValidationMessage() {
        _uiState.value = _uiState.value.copy(validationMessage = null)
    }
}
