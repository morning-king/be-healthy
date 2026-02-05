package com.behealthy.app.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
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
        ThemeStyle.NBA -> NBABackground(alpha)
        ThemeStyle.Badminton -> ShuttlecockBackground(alpha)
        ThemeStyle.Cute -> FloatingShapesBackground(alpha, Color(0xFFFF80AB)) // Pink
        ThemeStyle.Doraemon -> DoraemonBackground(alpha)
        ThemeStyle.Minions -> MinionsBackground(alpha)
        ThemeStyle.WallE -> SpaceBackground(alpha)
        ThemeStyle.NewYear -> NewYearBackground(alpha)
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
    var alpha: Float,
    var phase: Float = 0f // For shimmering/rotation
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
            val r = Random(it)
            Particle(
                x = size.width * r.nextFloat(),
                y = size.height * r.nextFloat(),
                speedX = 0f,
                speedY = -0.5f - r.nextFloat(), // Move up slowly
                radius = (size.minDimension * 0.02f) + r.nextFloat() * (size.minDimension * 0.03f),
                alpha = 0.2f + r.nextFloat() * 0.4f,
                phase = r.nextFloat() * 2 * PI.toFloat()
            )
        }
        
        particles.forEachIndexed { index, p ->
            val offset = (time * size.height * 0.5f * (1 + index % 3))
            val currentY = (p.y - offset + size.height * 2) % size.height
            
            // Simple breathing effect
            val breathing = sin(time * 20 + p.phase) * 0.2f + 1f
            
            drawCircle(
                color = color.copy(alpha = p.alpha * globalAlpha),
                radius = p.radius * breathing,
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
        val colWidth = size.width / 20f
        val cols = (size.width / colWidth).toInt() + 1
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
                    endY = currentY + size.height * 0.3f
                ),
                start = Offset(i * colWidth, currentY),
                end = Offset(i * colWidth, currentY + size.height * 0.3f),
                strokeWidth = colWidth * 0.1f
            )
            
            // Draw random characters (rectangles as abstraction)
            if (i % 2 == 0) {
                drawRect(
                    color = color.copy(alpha = 0.4f * globalAlpha),
                    topLeft = Offset(i * colWidth - colWidth * 0.2f, currentY + size.height * 0.3f),
                    size = Size(colWidth * 0.4f, colWidth * 0.4f)
                )
            }
        }
    }
}

@Composable
fun ZenBackground(globalAlpha: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "zen")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(30000, easing = LinearEasing)),
        label = "rotation"
    )
    val breathe by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = center
        val baseRadius = size.minDimension * 0.3f
        
        // Background faint ripples
        for (i in 1..3) {
            drawCircle(
                color = Color.Black.copy(alpha = 0.03f * globalAlpha),
                radius = baseRadius * i * 0.5f * breathe,
                center = center
            )
        }
        
        // Enso Circle (Ink style approximation using multiple arcs)
        rotate(rotation, center) {
            // Main stroke
            drawArc(
                brush = Brush.sweepGradient(
                    0f to Color.Transparent,
                    0.2f to Color.Black.copy(alpha = 0.6f * globalAlpha),
                    0.8f to Color.Black.copy(alpha = 0.8f * globalAlpha),
                    1f to Color.Transparent
                ),
                startAngle = 0f,
                sweepAngle = 320f,
                useCenter = false,
                topLeft = Offset(center.x - baseRadius, center.y - baseRadius),
                size = Size(baseRadius * 2, baseRadius * 2),
                style = Stroke(width = baseRadius * 0.15f, cap = StrokeCap.Round)
            )
            
            // Secondary inner stroke (faster rotation simulated by offset)
            rotate(rotation * 1.5f, center) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0f to Color.Transparent,
                        0.5f to Color.Black.copy(alpha = 0.4f * globalAlpha),
                        1f to Color.Transparent
                    ),
                    startAngle = 120f,
                    sweepAngle = 200f,
                    useCenter = false,
                    topLeft = Offset(center.x - baseRadius * 0.8f, center.y - baseRadius * 0.8f),
                    size = Size(baseRadius * 1.6f, baseRadius * 1.6f),
                    style = Stroke(width = baseRadius * 0.05f, cap = StrokeCap.Round)
                )
            }
        }
        
        // Stone/Center balance
        drawCircle(
            color = Color.Black.copy(alpha = 0.8f * globalAlpha),
            radius = baseRadius * 0.05f,
            center = center
        )
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
        val radius = size.minDimension * 0.25f
        
        // Bagua (Trigrams) Background - Simplified as dashed rings
        rotate(-rotation * 0.5f, center) {
            drawCircle(
                brush = Brush.sweepGradient(
                    listOf(
                        Color.Black.copy(alpha = 0.1f * globalAlpha),
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.1f * globalAlpha),
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.1f * globalAlpha),
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.1f * globalAlpha),
                        Color.Transparent
                    )
                ),
                radius = radius * 1.5f,
                center = center,
                style = Stroke(width = radius * 0.4f)
            )
        }
        
        // Yin Yang Symbol
        rotate(rotation, center) {
            // Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.5f * globalAlpha), Color.Transparent),
                    center = center,
                    radius = radius * 1.2f
                ),
                radius = radius * 1.2f,
                center = center
            )
            
            // Main Circle
            drawCircle(
                color = Color.White.copy(alpha = 0.9f * globalAlpha),
                radius = radius,
                center = center
            )
            
            // Black Half (Yin)
            drawArc(
                color = Color.Black.copy(alpha = 0.9f * globalAlpha),
                startAngle = 90f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )
            
            // Top White Curve
            drawCircle(
                color = Color.White.copy(alpha = 0.9f * globalAlpha),
                radius = radius / 2,
                center = Offset(center.x, center.y - radius / 2)
            )
            
            // Bottom Black Curve
            drawCircle(
                color = Color.Black.copy(alpha = 0.9f * globalAlpha),
                radius = radius / 2,
                center = Offset(center.x, center.y + radius / 2)
            )
            
            // Top Black Dot
            drawCircle(
                color = Color.Black.copy(alpha = 0.9f * globalAlpha),
                radius = radius / 6,
                center = Offset(center.x, center.y - radius / 2)
            )
            
            // Bottom White Dot
            drawCircle(
                color = Color.White.copy(alpha = 0.9f * globalAlpha),
                radius = radius / 6,
                center = Offset(center.x, center.y + radius / 2)
            )
        }
    }
}

@Composable
fun NBABackground(globalAlpha: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "nba")
    // Ball movement along a path
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label = "ballProgress"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        // 1. Draw Court Floor (Wood pattern lines)
        val courtColor = Color(0xFFE0C398).copy(alpha = 0.2f * globalAlpha)
        val lineColor = Color.White.copy(alpha = 0.3f * globalAlpha)
        
        // Draw wood planks
        val plankWidth = size.width / 10
        for (i in 0..10) {
            drawLine(
                color = courtColor,
                start = Offset(i * plankWidth, 0f),
                end = Offset(i * plankWidth, size.height),
                strokeWidth = 2f
            )
        }
        
        // Draw Court Lines (Half court abstraction)
        drawCircle(
            color = lineColor,
            radius = size.width * 0.2f,
            center = center,
            style = Stroke(width = 4f)
        )
        drawLine(
            color = lineColor,
            start = Offset(0f, center.y),
            end = Offset(size.width, center.y),
            strokeWidth = 4f
        )
        drawRect(
            color = lineColor,
            topLeft = Offset(size.width * 0.3f, size.height * 0.7f),
            size = Size(size.width * 0.4f, size.height * 0.3f),
            style = Stroke(width = 4f)
        )
        
        // 2. Bouncing Ball with Arc Trajectory
        val ballRadius = size.minDimension * 0.08f
        val startX = size.width * 0.1f
        val endX = size.width * 0.9f
        val groundY = size.height * 0.8f
        val peakY = size.height * 0.4f
        
        // Map progress to X
        val currentX = lerp(startX, endX, progress)
        // Parabolic arc for Y: y = 4 * height * (x - 0.5)^2 + peak
        // Normalized x (0..1)
        val normX = progress
        // Simple bounce parabola: 1 - 4*(x-0.5)^2 maps 0->0, 0.5->1, 1->0
        val heightFactor = 1f - 4f * (normX - 0.5f).pow(2)
        val currentY = lerp(groundY, peakY, heightFactor)
        
        // Ball Shadow
        val shadowRadius = ballRadius * (1f - heightFactor * 0.3f)
        drawOval(
            color = Color.Black.copy(alpha = 0.1f * globalAlpha * (1f - heightFactor)),
            topLeft = Offset(currentX - shadowRadius, groundY + ballRadius * 0.5f),
            size = Size(shadowRadius * 2, ballRadius * 0.2f)
        )
        
        // Draw Ball (Orange with lines)
        drawCircle(
            color = Color(0xFFFF6B00).copy(alpha = 0.9f * globalAlpha),
            radius = ballRadius,
            center = Offset(currentX, currentY)
        )
        // Ball Lines (Cross)
        rotate(progress * 720f, Offset(currentX, currentY)) {
            drawLine(
                color = Color.Black.copy(alpha = 0.5f * globalAlpha),
                start = Offset(currentX - ballRadius, currentY),
                end = Offset(currentX + ballRadius, currentY),
                strokeWidth = ballRadius * 0.1f
            )
            drawLine(
                color = Color.Black.copy(alpha = 0.5f * globalAlpha),
                start = Offset(currentX, currentY - ballRadius),
                end = Offset(currentX, currentY + ballRadius),
                strokeWidth = ballRadius * 0.1f
            )
        }
    }
}

@Composable
fun BouncingBallBackground(globalAlpha: Float, ballColor: Color) {
    // Simple vertical bounce for other sports
    val infiniteTransition = rememberInfiniteTransition(label = "ball")
    val yOffset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1000
                0f at 0
                1f at 500 with FastOutSlowInEasing
                0f at 1000
            }
        ),
        label = "bounce"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val ballRadius = size.minDimension * 0.06f
        val groundY = size.height * 0.9f
        val bounceHeight = size.height * 0.3f
        
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
            size = Size(shadowRadius * 2, 10f)
        )
    }
}

@Composable
fun SpaceBackground(globalAlpha: Float) {
    // Wall-E style stars
    ParticleBackground(globalAlpha, Color.White)
}

@Composable
fun NewYearBackground(globalAlpha: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "newyear")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)),
        label = "time"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        // 1. Lanterns (Hanging from top)
        val lanternColor = Color(0xFFD32F2F).copy(alpha = 0.8f * globalAlpha)
        val goldColor = Color(0xFFFFD700).copy(alpha = 0.8f * globalAlpha)
        val lanternSize = size.minDimension * 0.15f
        
        listOf(0.2f, 0.5f, 0.8f).forEachIndexed { index, xFactor ->
            val sway = sin(time * 2 * PI + index).toFloat() * 10f // Gentle sway
            val x = size.width * xFactor + sway
            val y = size.height * 0.1f + (if(index == 1) 50f else 0f) // Middle one lower
            
            // String
            drawLine(
                color = goldColor,
                start = Offset(x, 0f),
                end = Offset(x, y),
                strokeWidth = 2f
            )
            // Lantern Body
            drawOval(
                color = lanternColor,
                topLeft = Offset(x - lanternSize/2, y),
                size = Size(lanternSize, lanternSize * 0.8f)
            )
            // Top/Bottom gold rims
            drawRect(
                color = goldColor,
                topLeft = Offset(x - lanternSize/4, y - 5f),
                size = Size(lanternSize/2, 5f)
            )
            drawRect(
                color = goldColor,
                topLeft = Offset(x - lanternSize/4, y + lanternSize * 0.8f),
                size = Size(lanternSize/2, 5f)
            )
            // Tassel
            drawLine(
                color = lanternColor,
                start = Offset(x, y + lanternSize * 0.8f),
                end = Offset(x, y + lanternSize * 1.5f),
                strokeWidth = 4f
            )
        }
        
        // 2. Fireworks
        // Simulated as expanding circles that fade
        val fireworkCount = 3
        for (i in 0 until fireworkCount) {
            val progress = (time * 3 + i.toFloat() / fireworkCount) % 1f
            val centerX = size.width * (0.2f + 0.6f * (i * 0.3f % 1f))
            val centerY = size.height * (0.4f + 0.3f * ((i * 0.7f) % 1f))
            val maxRadius = size.minDimension * 0.4f
            
            if (progress < 0.8f) { // Explosion phase
                val currentRadius = progress * maxRadius
                val alphaFade = 1f - (progress / 0.8f)
                
                val explosionColor = when(i % 3) {
                    0 -> Color.Red
                    1 -> Color.Yellow
                    else -> Color(0xFFE91E63) // Pink
                }
                
                // Draw particles in a circle
                val particleCount = 12
                for (j in 0 until particleCount) {
                    val angle = (j.toFloat() / particleCount) * 2 * PI
                    val px = centerX + cos(angle) * currentRadius
                    val py = centerY + sin(angle) * currentRadius
                    
                    drawCircle(
                        color = explosionColor.copy(alpha = alphaFade * globalAlpha),
                        radius = 4f,
                        center = Offset(px.toFloat(), py.toFloat())
                    )
                }
            }
        }
    }
}

@Composable
fun ShuttlecockBackground(globalAlpha: Float) {
    BouncingBallBackground(globalAlpha, Color.White)
}

@Composable
fun FloatingShapesBackground(globalAlpha: Float, color: Color) {
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

