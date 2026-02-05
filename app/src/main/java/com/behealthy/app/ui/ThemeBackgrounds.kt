package com.behealthy.app.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.util.lerp
import androidx.compose.foundation.background
import com.behealthy.app.ui.theme.ThemeStyle
import kotlin.math.*
import kotlin.random.Random

@Composable
fun DynamicThemeBackground(
    theme: ThemeStyle,
    alpha: Float
) {
    // Global Alpha Wrapper
    // We draw with opacity = alpha.
    
    when (theme) {
        ThemeStyle.Default -> ParticleBackground(alpha, Color(0xFF4CAF50))
        ThemeStyle.Tech -> TechMatrixBackground(alpha)
        ThemeStyle.Sports -> BouncingBallBackground(alpha, Color(0xFFFF9800)) // Orange
        ThemeStyle.NBA -> BouncingBallBackground(alpha, Color(0xFFC9082A)) // NBA Red
        ThemeStyle.Badminton -> ShuttlecockBackground(alpha)
        ThemeStyle.Cute -> FloatingShapesBackground(alpha, Color(0xFFFF80AB)) // Pink
        ThemeStyle.Doraemon -> DoraemonBackground(alpha)
        ThemeStyle.Minions -> MinionsBackground(alpha)
        ThemeStyle.WallE -> SpaceBackground(alpha)
        ThemeStyle.NewYear -> FireworkBackground(alpha)
        ThemeStyle.Zen -> ZenBackground(alpha)
        ThemeStyle.Dao -> DaoBackground(alpha)
        ThemeStyle.FootballWorldCup -> BouncingBallBackground(alpha, Color.White) // Reuse
    }
}

// --- Shared Particle System ---
data class Particle(
    var x: Float, var y: Float,
    var speedX: Float, var speedY: Float,
    var radius: Float,
    var alpha: Float
)

@Composable
fun ParticleBackground(globalAlpha: Float, color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "particle")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing)),
        label = "time"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val particles = List(20) {
            val r = Random(it + time.toInt()) // Pseudo-random based on time to animate? No, better use state.
            // For simple implementation without FrameState, we just use time to offset positions
            // x = (initial + time * speed) % width
            Particle(
                x = size.width * Random(it).nextFloat(),
                y = size.height * Random(it + 1).nextFloat(),
                speedX = 0f,
                speedY = -1f - Random(it + 2).nextFloat(), // Move up
                radius = 10f + Random(it + 3).nextFloat() * 20f,
                alpha = 0.3f + Random(it + 4).nextFloat() * 0.5f
            )
        }
        
        // We use a simplified stateless animation based on current system time or infinite transition value
        // To make it smooth, we use 'time' (0..1) to interpolate.
        // Actually, to simulate continuous flow, we map time 0..1 to 0..height movement.
        
        particles.forEachIndexed { index, p ->
            val offset = (time * 1000 * (1 + index % 5)) % size.height
            val currentY = (p.y - offset + size.height) % size.height
            
            drawCircle(
                color = color.copy(alpha = p.alpha * globalAlpha),
                radius = p.radius,
                center = Offset(p.x, currentY)
            )
        }
    }
}

@Composable
fun TechMatrixBackground(globalAlpha: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "tech")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label = "phase"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cols = (size.width / 40f).toInt()
        val color = Color(0xFF00FF00) // Matrix Green
        
        for (i in 0 until cols) {
            val speed = 1 + (i % 3)
            val offset = (phase * size.height * speed) % size.height
            val startY = (i * 100f) % size.height
            val currentY = (startY + offset) % size.height
            
            drawLine(
                brush = Brush.verticalGradient(
                    0f to Color.Transparent,
                    0.5f to color.copy(alpha = 0.8f * globalAlpha),
                    1f to Color.Transparent,
                    startY = currentY,
                    endY = currentY + 150f
                ),
                start = Offset(i * 40f + 20f, currentY),
                end = Offset(i * 40f + 20f, currentY + 150f),
                strokeWidth = 2f
            )
        }
    }
}

@Composable
fun ZenBackground(globalAlpha: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "zen")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(60000, easing = LinearEasing)),
        label = "rotation"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Draw Enso Circle (Ink style)
        val center = center
        val radius = size.minDimension * 0.3f
        
        rotate(rotation, center) {
            drawArc(
                brush = Brush.sweepGradient(
                    0f to Color.Black.copy(alpha = 0f),
                    0.7f to Color.Black.copy(alpha = 0.5f * globalAlpha),
                    1f to Color.Black.copy(alpha = 0.8f * globalAlpha)
                ),
                startAngle = 0f,
                sweepAngle = 300f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                style = Stroke(width = 40f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }
    }
}

@Composable
fun DaoBackground(globalAlpha: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "dao")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing)),
        label = "rotation"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = center
        val radius = size.minDimension * 0.2f
        
        rotate(rotation, center) {
            // Yang (White)
            drawArc(
                color = Color.White.copy(alpha = 0.8f * globalAlpha),
                startAngle = -90f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
            )
            // Yin (Black)
            drawArc(
                color = Color.Black.copy(alpha = 0.8f * globalAlpha),
                startAngle = 90f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
            )
            // Circles
            drawCircle(
                color = Color.White.copy(alpha = 0.8f * globalAlpha),
                radius = radius / 2,
                center = Offset(center.x, center.y + radius / 2)
            )
            drawCircle(
                color = Color.Black.copy(alpha = 0.8f * globalAlpha),
                radius = radius / 2,
                center = Offset(center.x, center.y - radius / 2)
            )
            // Dots
            drawCircle(
                color = Color.Black.copy(alpha = 0.8f * globalAlpha),
                radius = radius / 6,
                center = Offset(center.x, center.y + radius / 2)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.8f * globalAlpha),
                radius = radius / 6,
                center = Offset(center.x, center.y - radius / 2)
            )
        }
    }
}

@Composable
fun BouncingBallBackground(globalAlpha: Float, ballColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "ball")
    val yOffset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1000
                0f at 0
                1f at 500 with FastOutSlowInEasing // Down
                0f at 1000 // Up
            }
        ),
        label = "bounce"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val ballRadius = 30f
        val groundY = size.height - 100f
        val bounceHeight = 300f
        
        val currentY = groundY - (yOffset * bounceHeight)
        
        drawCircle(
            color = ballColor.copy(alpha = 0.6f * globalAlpha),
            radius = ballRadius,
            center = Offset(size.width / 2, currentY)
        )
        
        // Shadow
        val shadowRadius = ballRadius * (1f - yOffset * 0.5f)
        drawOval(
            color = Color.Black.copy(alpha = 0.2f * globalAlpha * (1f - yOffset)),
            topLeft = Offset(size.width / 2 - shadowRadius, groundY + 10f),
            size = androidx.compose.ui.geometry.Size(shadowRadius * 2, 10f)
        )
    }
}

@Composable
fun SpaceBackground(globalAlpha: Float) {
    // Wall-E style stars
    ParticleBackground(globalAlpha, Color.White)
}

@Composable
fun FireworkBackground(globalAlpha: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "firework")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label = "progress"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 3
        val maxRadius = 300f
        
        val currentRadius = progress * maxRadius
        val currentAlpha = (1f - progress) * globalAlpha
        
        for (i in 0 until 12) {
            val angle = (i * 30f) * (PI / 180f)
            val x = centerX + cos(angle).toFloat() * currentRadius
            val y = centerY + sin(angle).toFloat() * currentRadius
            
            drawCircle(
                color = Color(0xFFFFD700).copy(alpha = currentAlpha), // Gold
                radius = 5f,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
fun ShuttlecockBackground(globalAlpha: Float) {
    // Reuse bouncing ball but white
    BouncingBallBackground(globalAlpha, Color.White)
}

@Composable
fun FloatingShapesBackground(globalAlpha: Float, color: Color) {
    // Reuse particles
    ParticleBackground(globalAlpha, color)
}

@Composable
fun DoraemonBackground(globalAlpha: Float) {
    // Blue background with bell
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0096E1).copy(alpha = 0.1f * globalAlpha)))
    // Bell
    BouncingBallBackground(globalAlpha, Color(0xFFF1C40F)) // Yellow Bell
}

@Composable
fun MinionsBackground(globalAlpha: Float) {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFFD600).copy(alpha = 0.1f * globalAlpha)))
    // Goggles
    ZenBackground(globalAlpha) // Reusing circle logic for now
}
