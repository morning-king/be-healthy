package com.behealthy.app.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.isActive
import kotlin.math.*
import kotlin.random.Random

// ==========================================
// BADMINTON THEME (羽毛球)
// Characteristics: Light, Agile, Fast, Technical
// Colors: Fresh Green (#43A047), White
// ==========================================

@Composable
fun AdvancedBadmintonBackground(modifier: Modifier = Modifier, alpha: Float) {
    // 1. Background: Badminton Court Green with Lines
    val courtColor = Color(0xFF43A047) // Fresh Green
    val linesColor = Color.White.copy(alpha = 0.5f * alpha)

    Box(modifier = modifier.fillMaxSize().background(courtColor)) {
        // Animation States
        val infiniteTransition = rememberInfiniteTransition(label = "badminton")
        
        // Shuttlecock Flight Simulation (Parabolic)
        // Cycles: Serve -> High Arc -> Smash -> Net Drop
        val flightPhase by infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)),
            label = "flight"
        )

        // Floating Feathers
        BadmintonFeatherSystem(alpha)

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // --- 1. Court Floor (Perspective Lines) ---
            // Draw simple perspective grid
            val stroke = Stroke(width = 2.dp.toPx())
            drawLine(linesColor, Offset(w * 0.1f, h), Offset(w * 0.4f, h * 0.4f), strokeWidth = stroke.width) // Left Line
            drawLine(linesColor, Offset(w * 0.9f, h), Offset(w * 0.6f, h * 0.4f), strokeWidth = stroke.width) // Right Line
            drawLine(linesColor, Offset(w * 0.1f, h * 0.8f), Offset(w * 0.9f, h * 0.8f), strokeWidth = stroke.width) // Service Line
            
            // Net (Implied at horizon)
            drawLine(Color.White.copy(alpha=0.8f * alpha), Offset(0f, h * 0.4f), Offset(w, h * 0.4f), strokeWidth = 4.dp.toPx())
            // Net Mesh pattern
            for(i in 0..20) {
                val x = w * (i / 20f)
                drawLine(Color.White.copy(alpha=0.2f * alpha), Offset(x, h * 0.4f), Offset(x, h * 0.4f + 20f), strokeWidth = 1f)
            }

            // --- 2. Shuttlecock Animation ---
            // Path logic based on phase
            val t = flightPhase
            var shuttleX = 0f
            var shuttleY = 0f
            var rotation = 0f
            
            // Simple 3-stage flight path
            if (t < 0.5f) {
                // High Arc (Serve/Lob)
                val localT = t * 2f // 0..1
                shuttleX = w * 0.1f + (w * 0.8f) * localT
                // Parabola: y = 4h * x * (1-x)
                val arcHeight = h * 0.5f
                shuttleY = h * 0.7f - arcHeight * sin(localT * PI.toFloat())
                rotation = 45f + localT * 90f // Rotate forward
            } else if (t < 0.6f) {
                // Smash preparation (Hover/Turn)
                val localT = (t - 0.5f) * 10f // 0..1
                shuttleX = w * 0.9f
                shuttleY = h * 0.2f + localT * 50f
                rotation = 135f + localT * 45f // Turn downwards
            } else {
                // Smash! (Fast diagonal down)
                val localT = (t - 0.6f) * 2.5f // 0..1
                shuttleX = w * 0.9f - (w * 0.8f) * localT
                shuttleY = h * 0.25f + (h * 0.6f) * localT
                rotation = 225f // Pointing down-left
                
                // Smash Trail
                if (localT > 0 && localT < 1f) {
                    drawLine(
                        color = Color.White.copy(alpha = (1f - localT) * 0.5f * alpha),
                        start = Offset(w * 0.9f, h * 0.25f),
                        end = Offset(shuttleX, shuttleY),
                        strokeWidth = 10f
                    )
                }
            }
            
            drawShuttlecock(shuttleX, shuttleY, rotation, alpha)
            
            // --- 3. Smash Impact Effect ---
            if (t > 0.6f && t < 0.65f) {
                 drawCircle(
                     color = Color.White.copy(alpha = 0.8f * alpha),
                     radius = 30f + (t - 0.6f) * 500f,
                     center = Offset(w * 0.9f, h * 0.25f),
                     style = Stroke(width = 5f)
                 )
            }
        }
    }
}

fun DrawScope.drawShuttlecock(x: Float, y: Float, rotation: Float, alpha: Float) {
    withTransform({
        translate(x, y)
        rotate(rotation)
    }) {
        // Cork (Head)
        drawCircle(
            color = Color.White.copy(alpha = alpha),
            radius = 10f,
            center = Offset(0f, 0f)
        )
        // Skirt (Feathers) - Cone shape
        val path = Path().apply {
            moveTo(-10f, 0f) // Left of cork
            lineTo(-20f, -30f) // Left Top
            quadraticTo(0f, -35f, 20f, -30f) // Arc top
            lineTo(10f, 0f) // Right of cork
            close()
        }
        drawPath(path, Color.White.copy(alpha = 0.8f * alpha))
        // Skirt struts
        drawLine(Color.Gray.copy(alpha = 0.5f * alpha), Offset(-10f, 0f), Offset(-20f, -30f))
        drawLine(Color.Gray.copy(alpha = 0.5f * alpha), Offset(10f, 0f), Offset(20f, -30f))
    }
}

@Composable
fun BadmintonFeatherSystem(globalAlpha: Float) {
    val feathers = remember { List(10) { BadmintonFeather() } }
    val lastFrameTime = remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        while (isActive) {
            val currentTime = withFrameNanos { it }
            if (lastFrameTime.value != 0L) {
                val dt = (currentTime - lastFrameTime.value) / 1_000_000_000f
                feathers.forEach { it.update(dt) }
            }
            lastFrameTime.value = currentTime
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        feathers.forEach { f ->
            withTransform({
                translate(f.x * size.width, f.y * size.height)
                rotate(f.rotation + sin(f.time * 2) * 20f) // Sway
            }) {
                // Draw Feather Shape
                val path = Path().apply {
                    moveTo(0f, 0f)
                    quadraticTo(10f, 10f, 0f, 40f) // Stem
                    moveTo(0f, 0f)
                    quadraticTo(-15f, 20f, 0f, 35f) // Left vane
                    moveTo(0f, 0f)
                    quadraticTo(15f, 20f, 0f, 35f) // Right vane
                }
                drawPath(path, Color.White.copy(alpha = 0.4f * globalAlpha), style = Stroke(width=2f))
            }
        }
    }
}

class BadmintonFeather {
    var x = Random.nextFloat()
    var y = Random.nextFloat()
    var speedY = 0.05f + Random.nextFloat() * 0.05f
    var rotation = Random.nextFloat() * 360f
    var time = Random.nextFloat() * 10f

    fun update(dt: Float) {
        y += speedY * dt
        time += dt
        if (y > 1.1f) {
            y = -0.1f
            x = Random.nextFloat()
        }
    }
}

// ==========================================
// FOOTBALL WORLD CUP THEME (足球世界杯)
// Characteristics: Passion, Teamwork, Carnival
// Colors: Turf Green (#2E7D32), Flag Colors
// ==========================================

@Composable
fun AdvancedFootballBackground(modifier: Modifier = Modifier, alpha: Float) {
    // 1. Background: Stadium Turf
    val turfDark = Color(0xFF2E7D32)
    val turfLight = Color(0xFF388E3C)
    
    Box(modifier = modifier.fillMaxSize().background(turfDark)) {
        // Turf Stripes
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stripeHeight = 100f
            val count = (size.height / stripeHeight).toInt() + 1
            for (i in 0 until count) {
                if (i % 2 == 0) {
                    drawRect(
                        color = turfLight,
                        topLeft = Offset(0f, i * stripeHeight),
                        size = Size(size.width, stripeHeight)
                    )
                }
            }
        }
        
        // Crowd Sound Wave Visualizer (Bottom)
        FootballSoundWave(alpha)

        // Confetti / Flag Particles
        FootballConfettiSystem(alpha)
        
        // Ball Animation
        FootballActionScene(alpha)
    }
}

@Composable
fun FootballActionScene(alpha: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "football")
    
    // Ball Movement: Curve Shot
    val shotProgress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)),
        label = "shot"
    )

    // Net Vibration (Triggered at end of shot)
    val netVibration by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), initialStartOffset = StartOffset(2000)),
        label = "net"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        
        // Draw Goal Net (Background)
        val netColor = Color.White.copy(alpha = 0.3f * alpha)
        val netRect = androidx.compose.ui.geometry.Rect(w * 0.2f, h * 0.3f, w * 0.8f, h * 0.7f)
        
        // Net Distortion based on impact
        val impactTime = 0.7f // When ball hits net
        val isImpact = shotProgress > impactTime && shotProgress < impactTime + 0.3f
        val distortAmount = if (isImpact) sin((shotProgress - impactTime) * 30f) * 20f else 0f
        
        // Vertical Lines
        for (i in 0..10) {
            val x = netRect.left + (netRect.width / 10f) * i
            val distortX = if (isImpact && i in 4..6) distortAmount else 0f
            drawLine(netColor, Offset(x + distortX, netRect.top), Offset(x + distortX, netRect.bottom), strokeWidth = 2f)
        }
        // Horizontal Lines
        for (i in 0..6) {
            val y = netRect.top + (netRect.height / 6f) * i
            val distortY = if (isImpact && i in 2..4) distortAmount else 0f
            drawLine(netColor, Offset(netRect.left, y + distortY), Offset(netRect.right, y + distortY), strokeWidth = 2f)
        }
        
        // Goal Posts
        val postColor = Color.White.copy(alpha = 0.9f * alpha)
        drawLine(postColor, Offset(netRect.left, h * 0.7f), Offset(netRect.left, h * 0.3f), strokeWidth = 8f) // Left Post
        drawLine(postColor, Offset(netRect.right, h * 0.7f), Offset(netRect.right, h * 0.3f), strokeWidth = 8f) // Right Post
        drawLine(postColor, Offset(netRect.left, h * 0.3f), Offset(netRect.right, h * 0.3f), strokeWidth = 8f) // Crossbar
        
        // Football Animation
        val t = shotProgress
        var bx = 0f
        var by = 0f
        var bScale = 1f
        var bRot = t * 720f
        
        if (t < 0.7f) {
            // Curve Shot towards goal
            // Start: Bottom Right
            // End: Center Net
            val startX = w * 0.8f
            val startY = h * 0.9f
            val endX = w * 0.5f
            val endY = h * 0.5f
            
            val localT = t / 0.7f
            bx = lerp(startX, endX, localT) - sin(localT * PI.toFloat()) * 100f // Curve (Magnus effect)
            by = lerp(startY, endY, localT)
            bScale = lerp(1.5f, 0.5f, localT) // Moving away
        } else {
            // Hit net and bounce/drop
            bx = w * 0.5f
            by = h * 0.5f + (t - 0.7f) * 200f // Drop
            bScale = 0.5f
        }
        
        if (t < 0.9f) {
            drawFootball(bx, by, bScale * 40f, bRot, alpha)
        }
    }
}

fun DrawScope.drawFootball(x: Float, y: Float, radius: Float, rotation: Float, alpha: Float) {
    withTransform({
        translate(x, y)
        rotate(rotation)
    }) {
        drawCircle(Color.White.copy(alpha = alpha), radius = radius)
        // Hexagon patterns (Simplified)
        val polyColor = Color.Black.copy(alpha = 0.8f * alpha)
        // Center pentagon
        drawCircle(polyColor, radius = radius * 0.4f, style = Stroke(width = radius * 0.1f))
        // Connecting lines
        for (i in 0 until 5) {
            val angle = (i * 72f) * (PI / 180f)
            val sx = cos(angle).toFloat() * radius * 0.4f
            val sy = sin(angle).toFloat() * radius * 0.4f
            val ex = cos(angle).toFloat() * radius
            val ey = sin(angle).toFloat() * radius
            drawLine(polyColor, Offset(sx, sy), Offset(ex, ey), strokeWidth = radius * 0.1f)
        }
    }
}

@Composable
fun FootballSoundWave(globalAlpha: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "soundwave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label = "phase"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val barCount = 30
        val barWidth = size.width / barCount
        val centerY = size.height * 0.85f
        
        for (i in 0 until barCount) {
            // Simulated audio data using sine waves
            val amp = (sin(i * 0.5f + phase) + 1f) * 0.5f + (sin(i * 1.2f - phase * 2) + 1f) * 0.3f
            val height = amp * 100f * globalAlpha
            
            // Draw bar
            val color = Color.White.copy(alpha = 0.3f * globalAlpha)
            drawRect(
                color = color,
                topLeft = Offset(i * barWidth + 5f, centerY - height),
                size = Size(barWidth - 10f, height)
            )
        }
    }
}

@Composable
fun FootballConfettiSystem(globalAlpha: Float) {
    // Flag Colors: Brazil(Yellow/Green), Argentina(Blue/White), Germany(Black/Red/Yellow), France(Blue/White/Red)
    val colors = listOf(
        Color(0xFFFFEB3B), Color(0xFF4CAF50), // Brazil
        Color(0xFF2196F3), Color.White, // Argentina
        Color(0xFFD32F2F), Color.Black // Germany
    )
    
    val confetti = remember { List(30) { FootballConfetti(colors.random()) } }
    val lastFrameTime = remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        while (isActive) {
            val currentTime = withFrameNanos { it }
            if (lastFrameTime.value != 0L) {
                val dt = (currentTime - lastFrameTime.value) / 1_000_000_000f
                confetti.forEach { it.update(dt) }
            }
            lastFrameTime.value = currentTime
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        confetti.forEach { c ->
            withTransform({
                translate(c.x * size.width, c.y * size.height)
                rotate(c.rotation)
            }) {
                drawRect(
                    color = c.color.copy(alpha = 0.8f * globalAlpha),
                    topLeft = Offset(-5f, -10f),
                    size = Size(10f, 20f)
                )
            }
        }
    }
}

class FootballConfetti(val color: Color) {
    var x = Random.nextFloat()
    var y = Random.nextFloat()
    var speedY = 0.1f + Random.nextFloat() * 0.2f
    var rotation = Random.nextFloat() * 360f
    var rotSpeed = (Random.nextFloat() - 0.5f) * 360f
    
    fun update(dt: Float) {
        y += speedY * dt
        rotation += rotSpeed * dt
        
        if (y > 1.1f) {
            y = -0.1f
            x = Random.nextFloat()
        }
    }
}

// Helper
fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction
}

// ==========================================
// NBA THEME (Basketball)
// Characteristics: Passion, Rhythm, Confrontation
// Colors: Lakers Purple/Gold or Bulls Red/Black (Using Generic Orange/Black/Wood)
// ==========================================

@Composable
fun AdvancedNBABackground(modifier: Modifier = Modifier, alpha: Float) {
    // 1. Background: Wood Floor Texture
    val woodColor = Color(0xFFE67E22) // Wood Orange
    val woodLight = Color(0xFFFFCC80)
    
    Box(modifier = modifier.fillMaxSize().background(woodColor)) {
        // Wood Floor Planks
        Canvas(modifier = Modifier.fillMaxSize()) {
            val plankWidth = 40f
            val count = (size.width / plankWidth).toInt() + 1
            for (i in 0 until count) {
                if (i % 3 == 0) { // Variation
                    drawRect(
                        color = woodLight.copy(alpha = 0.3f),
                        topLeft = Offset(i * plankWidth, 0f),
                        size = Size(plankWidth, size.height)
                    )
                }
            }
            
            // Court Lines (Key area)
            val stroke = Stroke(width = 4.dp.toPx())
            val w = size.width
            val h = size.height
            val keyWidth = w * 0.4f
            
            // Lane lines
            drawLine(Color.White.copy(alpha=0.8f), Offset((w-keyWidth)/2, h), Offset((w-keyWidth)/2, h*0.6f), strokeWidth = stroke.width)
            drawLine(Color.White.copy(alpha=0.8f), Offset((w+keyWidth)/2, h), Offset((w+keyWidth)/2, h*0.6f), strokeWidth = stroke.width)
            // Free throw line circle
            drawCircle(Color.White.copy(alpha=0.8f), center = Offset(w/2, h*0.6f), radius = keyWidth/2, style = Stroke(width=4.dp.toPx()))
        }
        
        // Audience Silhouettes (Bottom/Top)
        NBAAudienceSystem(alpha)

        // Scoreboard Element (Top Center)
        NBAScoreboard(alpha)

        // Basketball Animation
        NBAActionScene(alpha)
    }
}

@Composable
fun NBAAudienceSystem(alpha: Float) {
    // Simple flashing lights to simulate camera flashes
    val flashes = remember { mutableStateListOf<NBAFlash>() }
    
    LaunchedEffect(Unit) {
        while (isActive) {
            if (Random.nextFloat() < 0.3f) {
                flashes.add(NBAFlash())
            }
            kotlinx.coroutines.delay(100)
        }
    }
    
    // Update
    LaunchedEffect(Unit) {
        val frameRate = 1000L / 60L
        var lastTime = System.nanoTime()
        while (isActive) {
            val now = System.nanoTime()
            val dt = (now - lastTime) / 1_000_000_000f
            lastTime = now
            
            val iterator = flashes.iterator()
            while (iterator.hasNext()) {
                val f = iterator.next()
                f.life -= dt
                if (f.life <= 0) iterator.remove()
            }
            kotlinx.coroutines.delay(frameRate)
        }
    }
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        flashes.forEach { f ->
            drawCircle(
                Color.White.copy(alpha = (f.life / 0.2f).coerceIn(0f, 1f) * 0.8f * alpha),
                radius = Random.nextFloat() * 10f + 5f,
                center = Offset(f.x * size.width, f.y * size.height * 0.4f) // Top audience
            )
        }
    }
}

class NBAFlash {
    val x = Random.nextFloat()
    val y = Random.nextFloat()
    var life = 0.2f // Short flash
}

@Composable
fun NBAScoreboard(alpha: Float) {
    // Static UI Element
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.TopCenter
    ) {
        Canvas(modifier = Modifier.size(200.dp, 60.dp).graphicsLayer(alpha = alpha)) {
            // Board
            drawRoundRect(Color.Black.copy(alpha=0.7f), size = size, cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f))
            
            // Text placeholder (Digital clock style "24")
            val digitColor = Color.Red
            val cx = size.width / 2
            val cy = size.height / 2
            
            // Draw "24"
            // "2"
            val stroke = Stroke(width = 4f, cap = StrokeCap.Round)
            val w = 20f
            val h = 30f
            val x2 = cx - 15f
            val x4 = cx + 15f
            
            // 2
            val path2 = Path().apply {
                moveTo(x2 - w/2, cy - h/2)
                lineTo(x2 + w/2, cy - h/2)
                lineTo(x2 + w/2, cy)
                lineTo(x2 - w/2, cy)
                lineTo(x2 - w/2, cy + h/2)
                lineTo(x2 + w/2, cy + h/2)
            }
            drawPath(path2, digitColor, style = stroke)
            
            // 4
            val path4 = Path().apply {
                moveTo(x4 - w/2, cy - h/2)
                lineTo(x4 - w/2, cy)
                lineTo(x4 + w/2, cy)
                moveTo(x4 + w/2, cy - h/2)
                lineTo(x4 + w/2, cy + h/2)
            }
            drawPath(path4, digitColor, style = stroke)
        }
    }
}

@Composable
fun NBAActionScene(alpha: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "nba")
    
    // Shot Trajectory
    val shotProgress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2500, easing = LinearEasing)),
        label = "shot"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        
        // Hoop (Right side)
        val hoopX = w * 0.85f
        val hoopY = h * 0.4f
        
        // Backboard
        drawLine(Color.White.copy(alpha=alpha), Offset(hoopX + 20f, hoopY - 50f), Offset(hoopX + 20f, hoopY + 50f), strokeWidth = 5f)
        // Rim
        drawLine(Color(0xFFE65100).copy(alpha=alpha), Offset(hoopX, hoopY), Offset(hoopX + 40f, hoopY), strokeWidth = 5f)
        
        // Net
        val netPath = Path().apply {
            moveTo(hoopX, hoopY)
            lineTo(hoopX + 10f, hoopY + 40f)
            lineTo(hoopX + 30f, hoopY + 40f)
            lineTo(hoopX + 40f, hoopY)
        }
        drawPath(netPath, Color.White.copy(alpha=0.3f * alpha), style = Stroke(width=2f))
        
        // Ball Animation
        val t = shotProgress
        var bx = 0f
        var by = 0f
        
        if (t < 0.8f) {
            // Shot phase
            val localT = t / 0.8f
            val startX = w * 0.2f
            val startY = h * 0.7f
            
            // Parabola
            bx = lerp(startX, hoopX, localT)
            // y = a(x-h)^2 + k
            // Height peak at mid
            val peakY = h * 0.2f
            by = lerp(startY, hoopY, localT) - sin(localT * PI.toFloat()) * (startY - peakY)
        } else {
            // Through the hoop
            val localT = (t - 0.8f) / 0.2f
            bx = hoopX + 20f
            by = hoopY + localT * 100f
        }
        
        if (t < 0.95f) {
            drawBasketball(bx, by, 30f, t * 720f, alpha)
        }
    }
}

fun DrawScope.drawBasketball(x: Float, y: Float, radius: Float, rotation: Float, alpha: Float) {
    withTransform({
        translate(x, y)
        rotate(rotation)
    }) {
        // Base Orange
        drawCircle(Color(0xFFE65100).copy(alpha = alpha), radius = radius)
        
        // Lines
        val stroke = Stroke(width = 2.5f)
        val lineCol = Color.Black.copy(alpha = 0.8f * alpha)
        
        // Outline
        drawCircle(lineCol, radius = radius, style = stroke)
        
        // Cross lines
        drawLine(lineCol, Offset(0f, -radius), Offset(0f, radius), strokeWidth = 2.5f)
        drawLine(lineCol, Offset(-radius, 0f), Offset(radius, 0f), strokeWidth = 2.5f)
        
        // Curved lines (Perspective)
        drawArc(
            color = lineCol,
            topLeft = Offset(-radius * 0.6f, -radius),
            size = Size(radius * 1.2f, radius * 2f),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = stroke
        )
    }
}
