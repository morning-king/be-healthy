package com.behealthy.app.ui

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.isActive
import kotlin.math.*
import kotlin.random.Random

// ==========================================
// ZEN THEME (禅)
// ==========================================

@Composable
fun AdvancedZenBackground(modifier: Modifier = Modifier) {
    // 1. Background with Gaussian Blur & Overlay
    // Saturation 40% + Warmth (5500K approx)
    val colorMatrix = remember {
        ColorMatrix().apply {
            setToSaturation(0.4f)
            // 5500K approximation: slightly boost Red, reduce Blue slightly
            // R, G, B, A, Bias
            val warmth = floatArrayOf(
                1.05f, 0.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 0.95f, 0.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f, 0.0f
            )
            timesAssign(ColorMatrix(warmth))
        }
    }

    // Breathing Brightness: 0.05Hz => 20 seconds period
    val infiniteTransition = rememberInfiniteTransition(label = "zen_breathing")
    val brightness by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = FastOutSlowInEasing), // Half period
            repeatMode = RepeatMode.Reverse
        ),
        label = "brightness"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Background Image (Placeholder logic)
        // Ideally loading "bg_zen.jpg"
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("file:///android_asset/bg_zen.jpg") // Placeholder
                .crossfade(true)
                .build(),
            contentDescription = "Zen Background",
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.colorMatrix(colorMatrix),
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // Progressive Blur simulation (Android 12+)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        renderEffect = RenderEffect.createBlurEffect(
                            20f, 20f, // Average of 15-25px
                            Shader.TileMode.MIRROR
                        ).asComposeRenderEffect()
                    }
                    // Apply breathing brightness via alpha or direct tint?
                    // Brightness is better done via ColorMatrix, but here we can just tweak alpha of a black/white overlay
                    // or just use the overlay below.
                }
        )

        // Beige Overlay #F5F5DC at 30%
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5DC).copy(alpha = 0.3f))
        )
        
        // Breathing effect overlay (Black/White with alpha to modulate brightness)
        // Brightness > 1.0 -> Add White, < 1.0 -> Add Black
        val brightnessOverlayColor = if (brightness > 1f) Color.White else Color.Black
        val brightnessOverlayAlpha = abs(brightness - 1f)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brightnessOverlayColor.copy(alpha = brightnessOverlayAlpha))
        )

        // Dust Particle System
        ZenDustSystem()
    }
}

@Composable
fun ZenDustSystem() {
    val particles = remember { mutableStateListOf<ZenParticle>() }
    val lastFrameTime = remember { mutableStateOf(0L) }
    
    // Spawn logic
    LaunchedEffect(Unit) {
        while (isActive) {
            // Density 0.3/sec -> Spawn every ~3.33s
            // To be smoother, we can spawn periodically
            if (particles.size < 20) { // Cap max particles
                 particles.add(ZenParticle())
            }
            kotlinx.coroutines.delay(3000)
        }
    }

    // Update Loop
    LaunchedEffect(Unit) {
        while (isActive) {
            val currentTime = withFrameNanos { it }
            if (lastFrameTime.value != 0L) {
                val dt = (currentTime - lastFrameTime.value) / 1_000_000_000f // seconds
                
                // Update particles
                val iterator = particles.iterator()
                while (iterator.hasNext()) {
                    val p = iterator.next()
                    p.update(dt)
                    if (p.life <= 0) {
                        iterator.remove()
                    }
                }
            }
            lastFrameTime.value = currentTime
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { p ->
            val alpha = p.currentAlpha
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = p.size,
                center = Offset(p.x * size.width, p.y * size.height)
            )
        }
    }
}

class ZenParticle {
    // Normalized coordinates (0..1)
    var x = Random.nextFloat()
    var y = Random.nextFloat()
    
    // Slow drift
    var vx = (Random.nextFloat() - 0.5f) * 0.02f
    var vy = (Random.nextFloat() - 0.5f) * 0.02f
    
    var life = 8.0f
    val maxLife = 8.0f
    
    val size = Random.nextFloat() * 2f + 1f // Small dust
    
    val targetAlpha = Random.nextFloat() * 0.3f + 0.1f // 0.1 - 0.4
    
    val currentAlpha: Float
        get() {
            // Fade in and out
            val progress = 1f - (life / maxLife)
            return if (progress < 0.2f) {
                (progress / 0.2f) * targetAlpha
            } else if (progress > 0.8f) {
                ((1f - progress) / 0.2f) * targetAlpha
            } else {
                targetAlpha
            }
        }

    fun update(dt: Float) {
        x += vx * dt
        y += vy * dt
        life -= dt
        
        // Wrap around
        if (x < 0) x += 1f
        if (x > 1) x -= 1f
        if (y < 0) y += 1f
        if (y > 1) y -= 1f
    }
}


// ==========================================
// DAO THEME (道)
// ==========================================

@Composable
fun AdvancedDaoBackground(modifier: Modifier = Modifier) {
    // 1. Color Grading
    // Contrast 1.2-1.4 (using 1.3)
    // Cyan-Blue tint (RGB+5,10,15) -> Offset
    val colorMatrix = remember {
        ColorMatrix().apply {
            // Contrast
            val contrast = 1.3f
            val scale = floatArrayOf(
                contrast, 0f, 0f, 0f, 0f,
                0f, contrast, 0f, 0f, 0f,
                0f, 0f, contrast, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
            timesAssign(ColorMatrix(scale))
            
            // Tint (approximate conversion for 0-255 range to matrix offset? 
            // Actually matrix offset is usually 0-255 in Android framework if unnormalized, 
            // but Compose ColorMatrix uses 0..255 range for offset column? 
            // Let's assume small shift.
            val tint = floatArrayOf(
                1f, 0f, 0f, 0f, 5f,
                0f, 1f, 0f, 0f, 10f,
                0f, 0f, 1f, 0f, 15f,
                0f, 0f, 0f, 1f, 0f
            )
            timesAssign(ColorMatrix(tint))
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Background Image
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("file:///android_asset/bg_dao.jpg") // Placeholder
                .crossfade(true)
                .build(),
            contentDescription = "Dao Background",
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.colorMatrix(colorMatrix),
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // Base Blur (Background 15px)
                    // We can't easily do Z-buffer blur on single image without segmentation.
                    // Applying average blur.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        renderEffect = RenderEffect.createBlurEffect(
                            15f, 15f,
                            Shader.TileMode.MIRROR
                        ).asComposeRenderEffect()
                    }
                }
        )

        // Fog & Smoke System
        DaoEffectsSystem()
    }
}

@Composable
fun DaoEffectsSystem() {
    val fogLayers = remember { List(3) { DaoFogLayer(it) } }
    val smokeParticles = remember { mutableStateListOf<DaoSmokeParticle>() }
    val lastFrameTime = remember { mutableStateOf(0L) }
    
    // Spawn Smoke (Spiral)
    LaunchedEffect(Unit) {
        while (isActive) {
            // Spawn rate
            smokeParticles.add(DaoSmokeParticle())
            kotlinx.coroutines.delay(100) // 10 per second
        }
    }

    // Update Loop
    LaunchedEffect(Unit) {
        while (isActive) {
            val currentTime = withFrameNanos { it }
            if (lastFrameTime.value != 0L) {
                val dt = (currentTime - lastFrameTime.value) / 1_000_000_000f
                
                // Update Fog
                fogLayers.forEach { it.update(dt) }
                
                // Update Smoke
                val iterator = smokeParticles.iterator()
                while (iterator.hasNext()) {
                    val p = iterator.next()
                    p.update(dt)
                    if (p.life <= 0) {
                        iterator.remove()
                    }
                }
            }
            lastFrameTime.value = currentTime
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        
        // Draw Volumetric Fog Layers
        fogLayers.forEach { layer ->
            drawFogLayer(layer, w, h)
        }
        
        // Draw Spiral Smoke (Fluid simulation approx)
        // Origin: Center bottom area
        smokeParticles.forEach { p ->
            val alpha = (p.life / p.maxLife).coerceIn(0f, 1f) * 0.6f
            drawCircle(
                color = Color(0xFF708090).copy(alpha = alpha), // Cool Gray
                radius = p.size,
                center = Offset(p.x * w, p.y * h)
            )
        }
    }
}

fun DrawScope.drawFogLayer(layer: DaoFogLayer, w: Float, h: Float) {
    // Simulate fog using gradient strips or simplified noise drawing
    // Since real perlin noise per pixel is too slow for Canvas draw, 
    // we'll draw large overlapping circles/ovals moving horizontally
    
    val color = Color(0xFF708090).copy(alpha = 0.1f * layer.density)
    
    layer.blobs.forEach { blob ->
        drawOval(
            color = color,
            topLeft = Offset((blob.x + layer.offsetX) * w, blob.y * h),
            size = androidx.compose.ui.geometry.Size(blob.size * w, blob.size * h / 2f)
        )
        // Draw wrap around
        drawOval(
            color = color,
            topLeft = Offset((blob.x + layer.offsetX - 1f) * w, blob.y * h),
            size = androidx.compose.ui.geometry.Size(blob.size * w, blob.size * h / 2f)
        )
    }
}

class DaoFogLayer(seed: Int) {
    var offsetX = 0f
    val speed = 0.05f + (seed * 0.02f)
    val density = 0.5f
    
    // Pre-generate some "noise blobs"
    val blobs = List(10) {
        FogBlob(
            x = Random.nextFloat(),
            y = Random.nextFloat() * 0.8f, // Mostly top/mid
            size = 0.3f + Random.nextFloat() * 0.3f
        )
    }
    
    fun update(dt: Float) {
        offsetX += speed * dt
        if (offsetX > 1f) offsetX -= 1f
    }
}

data class FogBlob(val x: Float, val y: Float, val size: Float)

class DaoSmokeParticle {
    // Start at bottom 1/3, center-ish
    var x = 0.5f + (Random.nextFloat() - 0.5f) * 0.2f
    var y = 0.8f + (Random.nextFloat() - 0.5f) * 0.1f
    
    var life = 5.0f
    val maxLife = 5.0f
    var size = 10f
    
    // Spiral logic
    var angle = Random.nextFloat() * 6.28f
    val riseSpeed = 0.1f // 0.2 unit/sec vertical? "0.2 unit/sec"
    val spiralSpeed = 2.0f // Rad/s
    val radiusGrowth = 0.05f
    var currentRadius = 0.05f
    
    // Fluid properties (Simulated)
    // Curl 0.15rad/s -> affects rotation
    
    fun update(dt: Float) {
        life -= dt
        
        // Rise
        y -= riseSpeed * dt
        
        // Spiral
        angle += spiralSpeed * dt
        currentRadius += radiusGrowth * dt
        
        // Apply spiral offset to visual X (simulating 3D spiral on 2D plane)
        val offsetX = cos(angle) * currentRadius
        // val offsetZ = sin(angle) * currentRadius // Depth ignored for 2D draw
        
        // Add some noise/fluidity
        x += offsetX * dt
        
        size += 5f * dt // Expand as it rises
    }
}
