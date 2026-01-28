package com.behealthy.app.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.behealthy.app.R
import com.behealthy.app.feature.mood.MoodTrackingScreen
import com.behealthy.app.feature.plan.PlanCreationScreen
import com.behealthy.app.feature.plan.PlanListScreen
import com.behealthy.app.feature.profile.ProfileScreen
import com.behealthy.app.feature.splash.SplashScreen
import com.behealthy.app.feature.stats.StatisticsScreen
import com.behealthy.app.feature.task.CalendarScreen
import com.behealthy.app.ui.theme.BeHealthyTheme
import com.behealthy.app.ui.theme.ThemeStyle
import kotlinx.coroutines.launch

@Composable
fun MainApp(
    viewModel: MainViewModel = hiltViewModel()
) {
    val rootNavController = rememberNavController()
    val themeStyleName by viewModel.themeStyle.collectAsState()
    val themeStyle = try {
        ThemeStyle.valueOf(themeStyleName)
    } catch (e: Exception) {
        ThemeStyle.Default
    }

    BeHealthyTheme(themeStyle = themeStyle) {
        NavHost(
            navController = rootNavController,
            startDestination = "home",
            modifier = Modifier.fillMaxSize()
        ) {
            composable("splash") {
                SplashScreen(
                    onTimeout = {
                        rootNavController.navigate("home") {
                            popUpTo("splash") { inclusive = true }
                        }
                    },
                    themeStyle = themeStyle
                )
            }
            
            composable("home") {
                MainScreen(
                    themeStyle = themeStyle,
                    onNavigateToCreatePlan = { rootNavController.navigate("create_plan") },
                    onNavigateToPlanList = { rootNavController.navigate("plan_list") }
                )
            }

            composable("create_plan") {
                PlanCreationScreen(
                    onNavigateBack = { rootNavController.popBackStack() }
                )
            }

            composable("plan_list") {
                PlanListScreen(
                    onNavigateBack = { rootNavController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun ThemedIcon(
    themeStyle: ThemeStyle,
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean
) {
    when (themeStyle) {
        ThemeStyle.NBA -> {
            Box(contentAlignment = Alignment.Center) {
                // Draw Basketball background
                Canvas(modifier = Modifier.size(24.dp)) {
                    val radius = size.minDimension / 2
                    drawCircle(Color(0xFFE65100), radius = radius)
                    
                    // Lines
                    val strokeWidth = 1.5.dp.toPx()
                    drawLine(Color.Black, Offset(size.width/2, 0f), Offset(size.width/2, size.height), strokeWidth)
                    drawLine(Color.Black, Offset(0f, size.height/2), Offset(size.width, size.height/2), strokeWidth)
                    drawCircle(Color.Black, radius = radius, style = Stroke(strokeWidth))
                }
                Icon(icon, contentDescription, tint = Color.White, modifier = Modifier.scale(0.6f))
            }
        }
        ThemeStyle.NewYear -> {
             Box(contentAlignment = Alignment.Center) {
                // Draw Red Diamond (Fu background)
                Canvas(modifier = Modifier.size(24.dp)) {
                   rotate(45f) {
                       drawRect(Color(0xFFD32F2F), topLeft = Offset(size.width * 0.15f, size.height * 0.15f), size = androidx.compose.ui.geometry.Size(size.width * 0.7f, size.height * 0.7f))
                       drawRect(Color(0xFFFFD700), topLeft = Offset(size.width * 0.15f, size.height * 0.15f), size = androidx.compose.ui.geometry.Size(size.width * 0.7f, size.height * 0.7f), style = Stroke(1.dp.toPx()))
                   }
                }
                Icon(icon, contentDescription, tint = Color(0xFFFFD700), modifier = Modifier.scale(0.6f))
            }
        }
        ThemeStyle.Badminton -> {
             Box(contentAlignment = Alignment.Center) {
                // Green background (Court)
                Canvas(modifier = Modifier.size(24.dp)) {
                   drawCircle(Color(0xFF009688))
                   drawCircle(Color.White, style = Stroke(1.5.dp.toPx()))
                }
                Icon(icon, contentDescription, tint = Color.White, modifier = Modifier.scale(0.7f))
            }
        }
        ThemeStyle.FootballWorldCup -> {
             Box(contentAlignment = Alignment.Center) {
                // Soccer ball background
                Canvas(modifier = Modifier.size(24.dp)) {
                   drawCircle(Color.White)
                   drawCircle(Color.Black, style = Stroke(1.dp.toPx()))
                   
                   // Draw a simple pentagon in center
                   val radius = size.minDimension / 4
                   val center = center
                   val path = androidx.compose.ui.graphics.Path()
                   for (i in 0 until 5) {
                       val angle = Math.toRadians((i * 72 - 90).toDouble())
                       val x = center.x + radius * cos(angle).toFloat()
                       val y = center.y + radius * sin(angle).toFloat()
                       if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                   }
                   path.close()
                   drawPath(path, Color.Black)
                }
                // Icon on top, contrasting color? Black might blend with pentagon. Let's use Gold or Red?
                // Or maybe White with shadow?
                // Or maybe just tint the icon Gold?
                Icon(icon, contentDescription, tint = Color(0xFFD50000), modifier = Modifier.scale(0.6f))
            }
        }
        else -> {
            Icon(icon, contentDescription)
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    themeStyle: ThemeStyle,
    onNavigateToCreatePlan: () -> Unit,
    onNavigateToPlanList: () -> Unit
) {
    val mainRoutes = listOf("calendar", "mood", "stats", "profile")
    val pagerState = rememberPagerState(pageCount = { mainRoutes.size })
    val scope = rememberCoroutineScope()

    fun navigateToTab(index: Int) {
        scope.launch {
            pagerState.animateScrollToPage(index)
        }
    }

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            val currentRoute = mainRoutes[pagerState.currentPage]
            
            NavigationBar {
                // Fitness (Home)
                NavigationBarItem(
                    icon = { 
                        ThemedIcon(
                            themeStyle = themeStyle,
                            icon = if (currentRoute == "calendar") Icons.Filled.DirectionsRun else Icons.Outlined.DirectionsRun, 
                            contentDescription = "健身",
                            isSelected = currentRoute == "calendar"
                        ) 
                    },
                    label = { Text("健身") },
                    selected = currentRoute == "calendar",
                    onClick = { navigateToTab(0) }
                )
                
                // Mood
                NavigationBarItem(
                    icon = { 
                        ThemedIcon(
                            themeStyle = themeStyle,
                            icon = if (currentRoute == "mood") Icons.Filled.Face else Icons.Outlined.Face, 
                            contentDescription = "心情",
                            isSelected = currentRoute == "mood"
                        ) 
                    },
                    label = { Text("心情") },
                    selected = currentRoute == "mood",
                    onClick = { navigateToTab(1) }
                )
                
                // Stats
                NavigationBarItem(
                    icon = { 
                        ThemedIcon(
                            themeStyle = themeStyle,
                            icon = if (currentRoute == "stats") Icons.Filled.DateRange else Icons.Outlined.DateRange, 
                            contentDescription = "统计",
                            isSelected = currentRoute == "stats"
                        ) 
                    },
                    label = { Text("统计") },
                    selected = currentRoute == "stats",
                    onClick = { navigateToTab(2) }
                )
                
                // Profile
                NavigationBarItem(
                    icon = { 
                        ThemedIcon(
                            themeStyle = themeStyle,
                            icon = if (currentRoute == "profile") Icons.Filled.Person else Icons.Outlined.Person, 
                            contentDescription = "我的",
                            isSelected = currentRoute == "profile"
                        ) 
                    },
                    label = { Text("我的") },
                    selected = currentRoute == "profile",
                    onClick = { navigateToTab(3) }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = true
            ) { page ->
                when (mainRoutes[page]) {
                    "calendar" -> CalendarScreen(
                        onAddPlanClick = onNavigateToCreatePlan
                    )
                    "mood" -> MoodTrackingScreen()
                    "stats" -> StatisticsScreen(
                        onPlanOverviewClick = onNavigateToPlanList
                    )
                    "profile" -> ProfileScreen()
                }
            }
        }
    }
}


