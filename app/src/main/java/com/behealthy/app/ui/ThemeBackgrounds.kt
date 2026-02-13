package com.behealthy.app.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.unit.dp
import kotlin.math.*
import kotlin.random.Random

@Composable
fun DynamicThemeBackground(
    theme: ThemeStyle,
    alpha: Float,
    techIntensity: String = "Standard"
) {
    // Global Alpha Wrapper
    // We draw with opacity = alpha.
    
    when (theme) {
        ThemeStyle.Default -> ParticleBackground(alpha, Color(0xFF4CAF50))
        ThemeStyle.Tech -> TechMatrixBackground(alpha, techIntensity)
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
        ThemeStyle.Snooker -> SnookerBackground(alpha)
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
fun TechMatrixBackground(globalAlpha: Float, intensity: String = "Standard") {
    val infiniteTransition = rememberInfiniteTransition(label = "tech")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label = "phase"
    )
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing)),
        label = "rotation"
    )

    // Colors
    val neonBlue = Color(0xFF00D4FF)
    val cyberPurple = Color(0xFF9D4EDD)
    val matrixGreen = Color(0xFF00FF00)

    Canvas(modifier = Modifier.fillMaxSize()) {
        // 0. Base Background Color
        drawRect(color = Color(0xFF0A0A0A))
    
        // 1. Grid (All intensities)
        val gridSize = size.width / 10
        val gridAlpha = 0.2f * globalAlpha
        
        // Vertical Lines
        for (i in 0..10) {
             drawLine(
                color = neonBlue.copy(alpha = gridAlpha),
                start = Offset(i * gridSize, 0f),
                end = Offset(i * gridSize, size.height),
                strokeWidth = 1f
             )
        }
        // Horizontal Lines
        for (i in 0..20) {
             drawLine(
                color = cyberPurple.copy(alpha = gridAlpha),
                start = Offset(0f, i * gridSize),
                end = Offset(size.width, i * gridSize),
                strokeWidth = 1f
             )
        }
    }
    
    // 2. Dynamic Particles (Overlay)
    ParticleBackground(globalAlpha = globalAlpha * 0.5f, color = Color(0xFF00D4FF))

    Canvas(modifier = Modifier.fillMaxSize()) {
        
        if (intensity == "Minimal") return@Canvas

        // 3. Data Flow / Matrix Rain (Standard & Vibrant)
        val colWidth = size.width / 20f
        val cols = (size.width / colWidth).toInt() + 1
        
        for (i in 0 until cols) {
            val speed = 1 + (i % 3)
            val offset = (phase * size.height * speed) % size.height
            val startY = (i * 100f) % size.height
            val currentY = (startY + offset) % size.height
            
            // Randomly skip some columns for cleaner look
            if (i % 3 != 0) {
                drawLine(
                    brush = Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.5f to (if (i % 2 == 0) neonBlue else matrixGreen).copy(alpha = 0.4f * globalAlpha),
                        1f to Color.Transparent,
                        startY = currentY,
                        endY = currentY + size.height * 0.3f
                    ),
                    start = Offset(i * colWidth, currentY),
                    end = Offset(i * colWidth, currentY + size.height * 0.3f),
                    strokeWidth = colWidth * 0.1f
                )
            }
        }

        if (intensity == "Standard") return@Canvas

        // 3. Vibrant: Circuit Board & Holograms
        
        // Circuit Nodes (Static for now, could animate)
        val nodeCount = 6
        for (i in 0 until nodeCount) {
             val x = size.width * (0.1f + 0.15f * i)
             val y = size.height * (0.2f + 0.1f * (i % 3))
             
             drawCircle(
                 color = neonBlue.copy(alpha = 0.8f * globalAlpha),
                 radius = 10f,
                 center = Offset(x, y)
             )
             // Connecting lines
             if (i < nodeCount - 1) {
                 val nextX = size.width * (0.1f + 0.15f * (i + 1))
                 val nextY = size.height * (0.2f + 0.1f * ((i + 1) % 3))
                 drawLine(
                     color = neonBlue.copy(alpha = 0.3f * globalAlpha),
                     start = Offset(x, y),
                     end = Offset(nextX, nextY),
                     strokeWidth = 3f
                 )
             }
        }
        
        // Holographic Rotating Circles
        withTransform({
            rotate(rotation, center)
        }) {
            drawCircle(
                brush = Brush.sweepGradient(
                    listOf(
                        Color.Transparent,
                        cyberPurple.copy(alpha = 0.6f * globalAlpha),
                        Color.Transparent,
                        neonBlue.copy(alpha = 0.6f * globalAlpha),
                        Color.Transparent
                    )
                ),
                radius = size.minDimension * 0.35f,
                center = center,
                style = Stroke(width = 5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 30f), 0f))
            )
        }
        
        withTransform({
            rotate(-rotation * 1.5f, center)
        }) {
             drawCircle(
                brush = Brush.sweepGradient(
                    listOf(
                        Color.Transparent,
                        neonBlue.copy(alpha = 0.4f * globalAlpha),
                        Color.Transparent
                    )
                ),
                radius = size.minDimension * 0.25f,
                center = center,
                style = Stroke(width = 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 20f), 0f))
            )
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
                1f at 500 using FastOutSlowInEasing
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
    val infiniteTransition = rememberInfiniteTransition(label = "doraemon")
    
    // Animation for floating movement
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -1f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )
    
    // Animation for rotation (Bamboo Copter)
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        // 1. Background Gradient (Deep Blue Space)
        // Center lighter blue (#00A0E9) to edges darker blue (#0055AA)
        val centerColor = Color(0xFF00A0E9)
        val edgeColor = Color(0xFF003366)
        
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(centerColor.copy(alpha = 0.3f * globalAlpha), edgeColor.copy(alpha = 0.8f * globalAlpha)),
                center = Offset(width * 0.5f, height * 0.4f),
                radius = height * 0.8f
            )
        )
        
        // 2. Twinkling Stars
        val random = Random(123) // Seed for consistency
        for (i in 0..30) {
            val x = random.nextFloat() * width
            val y = random.nextFloat() * height
            val r = random.nextFloat() * 3f + 1f
            val alphaBase = random.nextFloat() * 0.5f + 0.3f
            // Simple twinkle simulation based on position parity and floatOffset
            val twinkle = ((sin((floatOffset * 2 + i).toDouble()) + 1f) / 2f).toFloat()
            
            drawCircle(
                color = Color.White.copy(alpha = alphaBase * twinkle * globalAlpha),
                radius = r,
                center = Offset(x, y)
            )
        }
        
        // 3. Floating Cloud (Bottom)
        // Drawn as a collection of circles
        val cloudY = height * 0.85f + floatOffset * 20f
        val cloudColor = Color.White.copy(alpha = 0.2f * globalAlpha) // Semi-transparent to not block text
        
        drawCircle(cloudColor, radius = width * 0.4f, center = Offset(width * 0.2f, cloudY + 50f))
        drawCircle(cloudColor, radius = width * 0.5f, center = Offset(width * 0.5f, cloudY))
        drawCircle(cloudColor, radius = width * 0.4f, center = Offset(width * 0.8f, cloudY + 40f))
        
        // 4. Floating Gadgets (Icons)
        
        // 4.1 Anywhere Door (Pink) - Top Right
        val doorX = width * 0.85f
        val doorY = height * 0.15f + floatOffset * 15f
        val doorSize = width * 0.12f
        
        withTransform({
            rotate(10f, pivot = Offset(doorX, doorY))
            translate(left = doorX - doorSize/2, top = doorY - doorSize*0.7f)
        }) {
            val doorColor = Color(0xFFF06292).copy(alpha = 0.8f * globalAlpha)
            val frameColor = Color(0xFFD81B60).copy(alpha = 0.8f * globalAlpha)
            
            // Frame
            drawRoundRect(
                color = frameColor,
                size = Size(doorSize, doorSize * 1.6f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
            // Inner Door
            drawRoundRect(
                color = doorColor,
                topLeft = Offset(doorSize * 0.1f, doorSize * 0.1f),
                size = Size(doorSize * 0.8f, doorSize * 1.4f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
            )
            // Knob
            drawCircle(
                color = frameColor,
                radius = doorSize * 0.05f,
                center = Offset(doorSize * 0.8f, doorSize * 0.8f)
            )
        }
        
        // 4.2 Bamboo Copter (Yellow) - Top Left
        val copterX = width * 0.15f
        val copterY = height * 0.2f - floatOffset * 10f
        val copterSize = width * 0.1f
        
        withTransform({
            translate(left = copterX, top = copterY)
            rotate(-15f)
        }) {
            val copterColor = Color(0xFFFFD54F).copy(alpha = 0.9f * globalAlpha)
            val stickColor = Color(0xFFFFCA28).copy(alpha = 0.9f * globalAlpha)
            
            // Stick
            drawRect(
                color = stickColor,
                topLeft = Offset(-2f, 0f),
                size = Size(4f, copterSize * 0.6f)
            )
            
            // Blades (Spinning)
            // Simulate spin by scaling width
            val spinScale = cos(rotation * PI / 180f).toFloat()
            drawOval(
                color = copterColor,
                topLeft = Offset(-copterSize/2, -4f),
                size = Size(copterSize, 8f)
            )
        }
        
        // 4.3 Bell (Golden) - Floating near center-left
        val bellX = width * 0.2f
        val bellY = height * 0.5f + floatOffset * 25f
        val bellRadius = width * 0.04f
        
        val bellColor = Color(0xFFFFD700).copy(alpha = 0.8f * globalAlpha)
        
        drawCircle(
            color = bellColor,
            radius = bellRadius,
            center = Offset(bellX, bellY)
        )
        // Bell line
        drawRect(
            color = Color(0xFFB78900).copy(alpha = 0.6f * globalAlpha),
            topLeft = Offset(bellX - bellRadius, bellY - 2f),
            size = Size(bellRadius * 2, 4f)
        )
        // Bell hole
        drawCircle(
            color = Color(0xFF5D4037).copy(alpha = 0.8f * globalAlpha),
            radius = bellRadius * 0.2f,
            center = Offset(bellX, bellY + bellRadius * 0.5f)
        )
        
        // 4.4 Dumbbell (Blue) - Bottom Right
        val dumbbellX = width * 0.8f
        val dumbbellY = height * 0.7f - floatOffset * 15f
        val dbColor = Color(0xFF42A5F5).copy(alpha = 0.6f * globalAlpha)
        val dbSize = width * 0.08f
        
        withTransform({
            rotate(45f + floatOffset * 5f, pivot = Offset(dumbbellX, dumbbellY))
            translate(dumbbellX - dbSize/2, dumbbellY - dbSize/4)
        }) {
            // Bar
            drawRoundRect(
                color = dbColor,
                topLeft = Offset(dbSize * 0.2f, dbSize * 0.2f),
                size = Size(dbSize * 0.6f, dbSize * 0.1f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f)
            )
            // Weights
            drawRoundRect(
                color = dbColor,
                topLeft = Offset(0f, 0f),
                size = Size(dbSize * 0.2f, dbSize * 0.5f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
            )
            drawRoundRect(
                color = dbColor,
                topLeft = Offset(dbSize * 0.8f, 0f),
                size = Size(dbSize * 0.2f, dbSize * 0.5f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
            )
        }
    }
    
    // 5. Flying Doraemon (Composable overlay)
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()
        FlyingDoraemon(infiniteTransition, width, height, globalAlpha)
    }
}

@Composable
fun FlyingDoraemon(infiniteTransition: InfiniteTransition, width: Float, height: Float, globalAlpha: Float) {
     val time by infiniteTransition.animateFloat(
         initialValue = 0f, targetValue = 2f * PI.toFloat(),
         animationSpec = infiniteRepeatable(
             animation = tween(20000, easing = LinearEasing)
         ),
         label = "doraemonFlight"
     )
     
     // Position: 
     val doraX = width * (0.5f + 0.35f * sin(time.toDouble()).toFloat())
     val doraY = height * (0.35f + 0.2f * sin((time * 1.3f).toDouble()).toFloat())
     
     val doraSize = width * 0.15f
     
     Canvas(modifier = Modifier.fillMaxSize()) {
         withTransform({
             translate(left = doraX - doraSize/2, top = doraY - doraSize/2)
             rotate((10f * cos(time.toDouble())).toFloat(), pivot = Offset(doraSize/2, doraSize/2))
         }) {
             val headColor = Color(0xFF00A0E9).copy(alpha = 0.9f * globalAlpha)
             val faceColor = Color.White.copy(alpha = 0.95f * globalAlpha)
             val noseColor = Color(0xFFD32F2F).copy(alpha = 0.9f * globalAlpha)
             val collarColor = Color(0xFFD32F2F).copy(alpha = 0.9f * globalAlpha)
             
             // Head (Blue)
             drawOval(color = headColor, size = Size(doraSize, doraSize * 0.9f))
             
             // Face (White)
             drawOval(color = faceColor, topLeft = Offset(doraSize * 0.1f, doraSize * 0.25f), size = Size(doraSize * 0.8f, doraSize * 0.65f))
             
             // Eyes
             val eyeWidth = doraSize * 0.22f
             val eyeHeight = doraSize * 0.28f
             val leftEyeX = doraSize * 0.5f - eyeWidth
             val rightEyeX = doraSize * 0.5f
             val eyeY = doraSize * 0.08f
             
             drawOval(Color.White, topLeft = Offset(leftEyeX, eyeY), size = Size(eyeWidth, eyeHeight), style = androidx.compose.ui.graphics.drawscope.Fill)
             drawOval(Color.Black, topLeft = Offset(leftEyeX, eyeY), size = Size(eyeWidth, eyeHeight), style = Stroke(width = 2f))
             
             drawOval(Color.White, topLeft = Offset(rightEyeX, eyeY), size = Size(eyeWidth, eyeHeight), style = androidx.compose.ui.graphics.drawscope.Fill)
             drawOval(Color.Black, topLeft = Offset(rightEyeX, eyeY), size = Size(eyeWidth, eyeHeight), style = Stroke(width = 2f))
             
             // Pupils
             drawCircle(Color.Black, radius = eyeWidth * 0.15f, center = Offset(leftEyeX + eyeWidth * 0.7f, eyeY + eyeHeight * 0.6f))
             drawCircle(Color.Black, radius = eyeWidth * 0.15f, center = Offset(rightEyeX + eyeWidth * 0.3f, eyeY + eyeHeight * 0.6f))
 
             // Nose
             drawCircle(noseColor, radius = doraSize * 0.07f, center = Offset(doraSize * 0.5f, doraSize * 0.38f))
             drawCircle(Color.White, radius = doraSize * 0.02f, center = Offset(doraSize * 0.48f, doraSize * 0.36f)) 
             
             // Whiskers
             val noseCenterY = doraSize * 0.38f
             val whiskerStartX = doraSize * 0.2f
             val whiskerEndX = doraSize * 0.4f
             val whiskerRightStartX = doraSize * 0.6f
             val whiskerRightEndX = doraSize * 0.8f
             
             drawLine(Color.Black, start = Offset(whiskerStartX, noseCenterY - 10f), end = Offset(whiskerEndX, noseCenterY + 5f), strokeWidth = 2f)
             drawLine(Color.Black, start = Offset(whiskerStartX - 5f, noseCenterY + 15f), end = Offset(whiskerEndX, noseCenterY + 15f), strokeWidth = 2f)
             drawLine(Color.Black, start = Offset(whiskerStartX, noseCenterY + 40f), end = Offset(whiskerEndX, noseCenterY + 25f), strokeWidth = 2f)
             
             drawLine(Color.Black, start = Offset(whiskerRightStartX, noseCenterY + 5f), end = Offset(whiskerRightEndX, noseCenterY - 10f), strokeWidth = 2f)
             drawLine(Color.Black, start = Offset(whiskerRightStartX, noseCenterY + 15f), end = Offset(whiskerRightEndX + 5f, noseCenterY + 15f), strokeWidth = 2f)
             drawLine(Color.Black, start = Offset(whiskerRightStartX, noseCenterY + 25f), end = Offset(whiskerRightEndX, noseCenterY + 40f), strokeWidth = 2f)
             
             // Mouth
             drawLine(Color.Black, start = Offset(doraSize * 0.5f, doraSize * 0.45f), end = Offset(doraSize * 0.5f, doraSize * 0.75f), strokeWidth = 2f)
             drawArc(
                 color = Color.Black,
                 startAngle = 0f,
                 sweepAngle = 180f,
                 useCenter = false,
                 topLeft = Offset(doraSize * 0.25f, doraSize * 0.45f),
                 size = Size(doraSize * 0.5f, doraSize * 0.35f),
                 style = Stroke(width = 2f)
             )
 
             // Collar
             drawRect(
                 color = collarColor,
                 topLeft = Offset(doraSize * 0.2f, doraSize * 0.82f),
                 size = Size(doraSize * 0.6f, doraSize * 0.08f),
                 style = androidx.compose.ui.graphics.drawscope.Fill
             )
             // Bell
             drawCircle(Color(0xFFFFD700), radius = doraSize * 0.08f, center = Offset(doraSize * 0.5f, doraSize * 0.9f))
             drawLine(Color.Black, start = Offset(doraSize * 0.5f - doraSize * 0.08f, doraSize * 0.88f), end = Offset(doraSize * 0.5f + doraSize * 0.08f, doraSize * 0.88f), strokeWidth = 1f) 
             drawCircle(Color.Black, radius = doraSize * 0.02f, center = Offset(doraSize * 0.5f, doraSize * 0.92f)) 
             
             // Bamboo Copter
             // We need independent rotation for copter blades, but we don't have it here easily.
             // We can use 'time' but it's slow. We need a faster spinner.
             // Since we are inside a new Composable, we can just use the infiniteTransition passed in or create a new state?
             // Actually we can't create new state inside Canvas draw block.
             // Let's just use (time * 20) for speed.
             val copterSpin = (time * 50f) 
             val copterBaseY = -doraSize * 0.05f
             
             // Stick
             drawRect(
                 color = Color(0xFFFFCA28).copy(alpha = globalAlpha),
                 topLeft = Offset(doraSize * 0.5f - 2f, copterBaseY),
                 size = Size(4f, doraSize * 0.1f)
             )
             
             // Blades
             val bladeWidth = doraSize * 0.8f
             val bladeHeight = 6f
             val currentBladeWidth = bladeWidth * abs(cos(copterSpin)).toFloat()
             
             drawOval(
                 color = Color(0xFFFFD54F).copy(alpha = globalAlpha),
                 topLeft = Offset(doraSize * 0.5f - currentBladeWidth/2, copterBaseY - bladeHeight/2),
                 size = Size(currentBladeWidth, bladeHeight)
             )
         }
     }
}

@Composable
fun MinionsBackground(globalAlpha: Float) {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFFD600).copy(alpha = 0.1f * globalAlpha)))
    ParticleBackground(globalAlpha, Color(0xFF005A9C)) // Blue particles on Yellow
}

@Composable
fun SnookerBackground(globalAlpha: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "snooker")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing)),
        label = "time"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        // 1. Felt Texture (Gradient)
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF006400), Color(0xFF004D26)),
                center = center,
                radius = size.maxDimension
            ),
            alpha = globalAlpha
        )
        
        // 2. Balls
        val balls = listOf(
            Color.White, // Cue Ball
            Color(0xFFD32F2F), // Red
            Color.Black, // Black
            Color(0xFFFF4081), // Pink
            Color(0xFF2196F3), // Blue
            Color(0xFFFFEB3B), // Yellow
            Color(0xFF4CAF50)  // Green
        )
        
        balls.forEachIndexed { index, color ->
            // Pseudo-random motion based on time
            val seed = index * 100
            val x = (sin((time * 2 * PI + seed).toDouble()).toFloat() * 0.4f + 0.5f) * size.width
            val y = (cos((time * 3 * PI + seed * 2).toDouble()).toFloat() * 0.4f + 0.5f) * size.height
            
            // Shadow
            drawCircle(
                color = Color.Black.copy(alpha = 0.3f * globalAlpha),
                radius = 12.dp.toPx(),
                center = Offset(x + 5f, y + 5f)
            )
            
            // Ball
            drawCircle(
                color = color.copy(alpha = globalAlpha),
                radius = 12.dp.toPx(),
                center = Offset(x, y)
            )
            
            // Highlight
            drawCircle(
                color = Color.White.copy(alpha = 0.4f * globalAlpha),
                radius = 4.dp.toPx(),
                center = Offset(x - 4.dp.toPx(), y - 4.dp.toPx())
            )
        }
    }
}

