package com.behealthy.app.feature.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.behealthy.app.ui.RunningLoading
import com.behealthy.app.ui.theme.ThemeStyle
import kotlinx.coroutines.delay

import androidx.compose.ui.graphics.Color

@Composable
fun SplashScreen(
    onTimeout: () -> Unit,
    themeStyle: ThemeStyle = ThemeStyle.Default,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // 3秒后自动跳转
    LaunchedEffect(Unit) {
        delay(3000)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            // Q版运动动画 (放大作为核心视觉元素)
            RunningLoading(
                size = 160.dp,
                modifier = Modifier.padding(bottom = 16.dp),
                themeStyle = themeStyle
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 个性化欢迎语
                Text(
                    text = uiState.welcomeMessage,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                
                // Slogan / 第二行文字
                if (!uiState.fitnessSummary.isNullOrEmpty()) {
                    Text(
                        text = uiState.fitnessSummary!!,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
