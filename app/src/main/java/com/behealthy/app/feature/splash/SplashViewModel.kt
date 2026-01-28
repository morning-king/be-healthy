package com.behealthy.app.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.behealthy.app.data.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.behealthy.app.core.repository.WeatherCondition
import com.behealthy.app.core.repository.WeatherRepository
import java.time.LocalDate

data class SplashUiState(
    val welcomeMessage: String = "",
    val showFitnessSummary: Boolean = false,
    val fitnessSummary: String? = null,
    val showMoodSummary: Boolean = false,
    val moodSummary: String? = null,
    val weatherCondition: WeatherCondition? = null
)

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val weatherRepository: WeatherRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()
    
    init {
        loadSplashData()
    }
    
    private fun loadSplashData() {
        viewModelScope.launch {
            // 获取用户昵称
            val nickname = userProfileRepository.userProfile.first().nickname
            val welcomeMessage = if (nickname.isNullOrEmpty()) {
                "你好，新朋友，欢迎你，又是元气满满的一天 ✨"
            } else {
                "你好，$nickname，欢迎你，又是元气满满的一天 ✨"
            }
            
            // 获取天气
            val weather = weatherRepository.getWeatherForDate(LocalDate.now()).firstOrNull()
            
            _uiState.value = SplashUiState(
                welcomeMessage = welcomeMessage,
                showFitnessSummary = false,
                fitnessSummary = "健康生活，快乐人生 🏃‍♂️💪",
                showMoodSummary = false,
                moodSummary = null,
                weatherCondition = weather?.condition
            )
        }
    }
}