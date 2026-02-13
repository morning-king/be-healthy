package com.behealthy.app.feature.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.behealthy.app.core.database.entity.FitnessPlanEntity
import com.behealthy.app.core.repository.FitnessPlanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlanUiState(
    val plans: List<FitnessPlanEntity> = emptyList(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false
)

@HiltViewModel
class PlanViewModel @Inject constructor(
    private val repository: FitnessPlanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlanUiState())
    val uiState: StateFlow<PlanUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.allPlans.collect { plans ->
                    _uiState.value = _uiState.value.copy(plans = plans)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Optionally set an error state
            }
        }
    }
    
    fun resetSaveState() {
        _uiState.value = _uiState.value.copy(isSaved = false)
    }

    fun addPlan(
        name: String,
        durationType: String,
        durationQuantity: Int,
        startDate: String,
        endDate: String,
        targetText: String,
        workDayDiet: Boolean,
        workDayExercise: Boolean,
        workDayMinutes: Int,
        workDaySteps: Int,
        workDayCalories: Int,
        restDayDiet: Boolean,
        restDayExercise: Boolean,
        restDayMinutes: Int,
        restDayCalories: Int,
        note: String
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                kotlinx.coroutines.delay(1000) // Show loading animation
                
                val newPlan = FitnessPlanEntity(
                    name = name,
                    durationType = durationType,
                    durationQuantity = durationQuantity,
                    startDate = startDate,
                    endDate = endDate,
                    targetText = targetText,
                    workDayDietEnabled = workDayDiet,
                    workDayExerciseEnabled = workDayExercise,
                    workDayExerciseMinutes = workDayMinutes,
                    workDayExerciseSteps = workDaySteps,
                    workDayExerciseCalories = workDayCalories,
                    restDayDietEnabled = restDayDiet,
                    restDayExerciseEnabled = restDayExercise,
                    restDayExerciseMinutes = restDayMinutes,
                    restDayExerciseCalories = restDayCalories,
                    note = note
                )
                repository.createPlan(newPlan)
                _uiState.value = _uiState.value.copy(isLoading = false, isSaved = true)
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}
