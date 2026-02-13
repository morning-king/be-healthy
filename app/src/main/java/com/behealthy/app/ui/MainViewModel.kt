package com.behealthy.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.behealthy.app.data.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    userProfileRepository: UserProfileRepository
) : ViewModel() {
    
    val themeStyle = userProfileRepository.userProfile
        .map { it.themeStyle }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "Default"
        )
    
    val backgroundAlpha = userProfileRepository.userProfile
        .map { it.backgroundAlpha }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0.3f
        )
        
    val pageTransition = userProfileRepository.userProfile
        .map { it.pageTransition }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "Default"
        )
        
    val zenRotationEnabled = userProfileRepository.userProfile
        .map { it.zenRotationEnabled }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = true
        )
        
    val zenRotationSpeed = userProfileRepository.userProfile
        .map { it.zenRotationSpeed }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 5f
        )
        
    val zenRotationDirection = userProfileRepository.userProfile
        .map { it.zenRotationDirection }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "Clockwise"
        )
        
    val techIntensity = userProfileRepository.userProfile
        .map { it.techIntensity }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "Standard"
        )
        
    val fontColorMode = userProfileRepository.userProfile
        .map { it.fontColorMode }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "Auto"
        )
}
