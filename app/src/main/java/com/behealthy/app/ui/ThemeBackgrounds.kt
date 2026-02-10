package com.behealthy.app.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.behealthy.app.ui.theme.*
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
        ThemeStyle.FootballWorldCup -> FootballWorldCupBackground(alpha) // Reuse
    }
}

@Composable
fun BouncingBallBackground(globalAlpha: Float, color: Color) {
    val balls = remember {
        List(10) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                speedX = (Random.nextFloat() - 0.5f) * 0.02f,
                speedY = (Random.nextFloat() - 0.5f) * 0.02f,
                radius = 0.03f + Random.nextFloat() * 0.04f,
                alpha = 0.4f + Random.nextFloat() * 0.4f
            )
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "bouncing")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing)),
        label = "time"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        balls.forEachIndexed { index, ball ->
            // Simulate simple movement with wall bouncing
            // Using time to drive position is tricky without state, so we use a periodic function
            // Position = abs((initial + speed * time) % 2 - 1) which creates a triangle wave (bounce 0..1..0)
            
            val totalSpeed = 10f // multiplier
            val rawX = ball.x + ball.speedX * time * 1000f + index * 0.1f
            val rawY = ball.y + ball.speedY * time * 1000f + index * 0.1f
            
            // Triangle wave for bouncing effect: 2 * abs(round(x) - x) is not quite it.
            // Let's use: abs((x % 2) - 1) * 2? No.
            // Correct triangle wave 0->1->0: abs((t % 2) - 1)
            // But we want to stay in 0..1.
            // Let's just use simple modulo for now as wrapping, or sine wave for bouncing
            
            val cx = (sin(rawX * 2 * PI) + 1) / 2 * size.width
            val cy = (cos(rawY * 2 * PI) + 1) / 2 * size.height
            
            drawCircle(
                color = color.copy(alpha = ball.alpha * globalAlpha),
                radius = size.minDimension * ball.radius,
                center = Offset(cx.toFloat(), cy.toFloat())
            )
        }
    }
}

@Composable
fun TechMatrixBackground(globalAlpha: Float) {
    // Matrix Rain Effect
    val infiniteTransition = rememberInfiniteTransition(label = "matrix")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label = "time"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cols = 20
        val colWidth = size.width / cols
        
        for (i in 0 until cols) {
            val speed = 0.5f + (i % 5) * 0.1f
            val offset = i * 0.3f
            val yPos = ((time * speed + offset) % 1f) * size.height
            
            // Draw trail
            for (j in 0 until 5) {
                val alpha = (1f - j * 0.2f) * globalAlpha
                if (alpha > 0) {
                    val charY = yPos - j * 30f
                    if (charY > 0) {
                        drawRect(
                            color = Color(0xFF00FF00).copy(alpha = alpha),
                            topLeft = Offset(i * colWidth, charY),
                            size = Size(colWidth * 0.6f, 20f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NBABackground(globalAlpha: Float) {
    AdvancedNBABackground(modifier = Modifier.alpha(globalAlpha), alpha = globalAlpha)
}

@Composable
fun ZenBackground(globalAlpha: Float) {
    AdvancedZenBackground(Modifier.alpha(globalAlpha))
}

@Composable
fun DaoBackground(globalAlpha: Float) {
    AdvancedDaoBackground(Modifier.alpha(globalAlpha))
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

    // Optimize: Create particles once with normalized coordinates (0..1)
    val particles = remember {
        List(20) {
            val r = Random(it)
            Particle(
                x = r.nextFloat(), // Normalized 0..1
                y = r.nextFloat(), // Normalized 0..1
                speedX = 0f,
                speedY = -0.5f - r.nextFloat(), // Relative speed
                radius = 0.02f + r.nextFloat() * 0.03f, // Relative radius base
                alpha = 0.2f + r.nextFloat() * 0.4f,
                phase = r.nextFloat() * 2 * PI.toFloat()
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEachIndexed { index, p ->
            val offset = (time * 0.5f * (1 + index % 3)) // Normalized offset
            // Wrap y: (p.y - offset) modulo 1.0 (but handling negative correctly)
            val rawY = p.y - offset
            val currentYNormalized = rawY - floor(rawY)
            
            val currentX = p.x * size.width
            val currentY = currentYNormalized * size.height
            
            val baseRadius = size.minDimension * p.radius
            
            // Simple breathing effect
            val breathing = sin(time * 20 + p.phase) * 0.2f + 1f
            
            drawCircle(
                color = color.copy(alpha = p.alpha * globalAlpha),
                radius = baseRadius * breathing,
                center = Offset(currentX, currentY)
            )
        }
    }
}

@Composable
fun WallEBackground(globalAlpha: Float) {
    AdvancedWallEBackground(globalAlpha)
}

@Composable
fun SpaceBackground(globalAlpha: Float) {
   WallEBackground(globalAlpha)
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
    AdvancedBadmintonBackground(alpha = globalAlpha)
}

@Composable
fun FootballWorldCupBackground(globalAlpha: Float) {
    AdvancedFootballBackground(alpha = globalAlpha)
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
            val twinkle = (sin(floatOffset * 2 + i) + 1f) / 2f 
            
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
     val doraX = width * (0.5f + 0.35f * sin(time))
     val doraY = height * (0.35f + 0.2f * sin(time * 1.3f)) 
     
     val doraSize = width * 0.15f
     
     Canvas(modifier = Modifier.fillMaxSize()) {
         withTransform({
             translate(left = doraX - doraSize/2, top = doraY - doraSize/2)
             rotate(10f * cos(time), pivot = Offset(doraSize/2, doraSize/2))
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
    AdvancedMinionsBackground(globalAlpha)
}

