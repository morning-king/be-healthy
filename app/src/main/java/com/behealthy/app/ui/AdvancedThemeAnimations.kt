package com.behealthy.app.ui

import android.graphics.BitmapFactory
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.behealthy.app.R
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.*
import kotlin.random.Random

// --- Minions Theme Implementation ---

@Composable
fun AdvancedMinionsBackground(globalAlpha: Float) {
    val context = LocalContext.current
    val density = LocalDensity.current
    
    // 1. Blurred Background
    Box(modifier = Modifier.fillMaxSize()) {
        // Attempt to load resource, fallback to gradient
        val brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFFFF176), Color(0xFFFFD54F), Color(0xFFFFB300))
        )
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush)
                .graphicsLayer {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        renderEffect = RenderEffect.createBlurEffect(
                            20f, 20f, Shader.TileMode.CLAMP
                        ).asComposeRenderEffect()
                    }
                    alpha = globalAlpha
                }
        )
    }

    // 2. 3D Minions Animation System
    val squad = remember { MinionSquad() }
    
    // Game Loop
    LaunchedEffect(Unit) {
        val frameRate = 1000L / 60L
        var lastTime = System.nanoTime()
        
        while (isActive) {
            val now = System.nanoTime()
            val dt = (now - lastTime) / 1_000_000_000f // seconds
            lastTime = now
            
            squad.update(dt)
            kotlinx.coroutines.delay(frameRate)
        }
    }
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        squad.updateBounds(size.width, size.height)
        squad.draw(this, globalAlpha)
    }
}

class MinionSquad {
    private val minions = mutableListOf<MinionActor>()
    private var boundsWidth = 1000f
    private var boundsHeight = 2000f
    
    // Squad Logic
    private var leaderTarget = Offset(500f, 500f)
    private var timeSinceTargetChange = 0f
    
    // Effects System
    private val particles = mutableListOf<EffectParticle>()
    
    init {
        // Create 5 minions in formation
        // 0: Leader
        // 1, 2: Middle
        // 3, 4: Rear
        val startPos = Offset(200f, 200f)
        for (i in 0 until 5) {
            minions.add(MinionActor(i, startPos))
        }
    }
    
    fun updateBounds(w: Float, h: Float) {
        boundsWidth = w
        boundsHeight = h
    }
    
    fun update(dt: Float) {
        timeSinceTargetChange += dt
        
        // 1. Update Leader Logic (Bezier-like random movement)
        val leader = minions[0]
        val distToTarget = (leaderTarget - leader.position).getDistance()
        
        if (distToTarget < 100f || timeSinceTargetChange > 3f) {
            // Pick new random target
            leaderTarget = Offset(
                Random.nextFloat() * boundsWidth,
                Random.nextFloat() * boundsHeight
            )
            timeSinceTargetChange = 0f
        }
        
        // Steering leader towards target
        val desiredVelocity = (leaderTarget - leader.position).normalized() * leader.maxSpeed
        val steering = (desiredVelocity - leader.velocity) * 2f // Steering force
        leader.velocity = (leader.velocity + steering * dt).limit(leader.maxSpeed)
        
        // Bounce off walls
        if (leader.position.x < 0 || leader.position.x > boundsWidth) leader.velocity = leader.velocity.copy(x = -leader.velocity.x)
        if (leader.position.y < 0 || leader.position.y > boundsHeight) leader.velocity = leader.velocity.copy(y = -leader.velocity.y)
        
        leader.update(dt)
        
        // Spawn particles (Dust/Banana)
        if (Random.nextFloat() < 0.05f) { // Occasional
             particles.add(EffectParticle(leader.position.x, leader.position.y + 60f, ParticleType.DUST))
        }
        if (Random.nextFloat() < 0.005f) { // Rare Banana
             particles.add(EffectParticle(leader.position.x, leader.position.y + 60f, ParticleType.BANANA))
        }
        
        // 2. Update Followers (Formation)
        // Triangle offsets relative to velocity direction
        val heading = -atan2(leader.velocity.x, leader.velocity.y) // Rotation
        
        val formationOffsets = listOf(
            Offset(0f, 0f),       // Leader
            Offset(-120f, 150f),  // Mid Left
            Offset(120f, 150f),   // Mid Right
            Offset(-240f, 300f),  // Rear Left
            Offset(240f, 300f)    // Rear Right
        )
        
        for (i in 1 until 5) {
            val minion = minions[i]
            // Calculate target position in world space based on leader
            val offset = formationOffsets[i]
            // Rotate offset by leader heading
            val rotX = offset.x * cos(heading) - offset.y * sin(heading)
            val rotY = offset.x * sin(heading) + offset.y * cos(heading)
            
            val targetPos = leader.position + Offset(rotX, rotY)
            
            // Spring force to target
            val toTarget = targetPos - minion.position
            val force = toTarget * 3.0f // Spring constant
            
            minion.velocity = (minion.velocity + force * dt).limit(minion.maxSpeed * 1.1f) // Followers can go faster to catch up
            minion.update(dt)
            
            // Follower dust
            if (Random.nextFloat() < 0.02f) {
                particles.add(EffectParticle(minion.position.x, minion.position.y + 60f, ParticleType.DUST))
            }
        }
        
        // Update Particles
        particles.forEach { it.update(dt) }
        particles.removeAll { it.life <= 0 }
    }
    
    fun draw(scope: DrawScope, alpha: Float) {
        // Draw Particles (Bottom Layer)
        particles.forEach { p ->
            val opacity = (p.life / p.maxLife).coerceIn(0f, 1f) * alpha
            if (p.type == ParticleType.BANANA) {
                scope.withTransform({
                    translate(p.x, p.y)
                    rotate(p.life * 100f)
                }) {
                     // Draw Banana Peel
                     val path = Path().apply {
                        moveTo(0f,0f)
                        quadraticTo(10f, -10f, 20f, 0f)
                        quadraticTo(10f, 10f, 0f, 0f)
                     }
                     scope.drawPath(path, Color(0xFFFFEB3B).copy(alpha=opacity), style=Stroke(3f))
                }
            } else {
                // Dust
                scope.drawCircle(Color.Gray.copy(alpha=opacity * 0.5f), radius=p.size, center=Offset(p.x, p.y))
            }
        }
        
        // Draw minions sorted by Y (simple z-sorting)
        minions.sortedBy { it.position.y }.forEach { minion ->
            minion.draw(scope, alpha)
        }
    }
}

enum class ParticleType { DUST, BANANA, SCREW }

class EffectParticle(var x: Float, var y: Float, val type: ParticleType) {
    var life = if (type == ParticleType.BANANA) 5.0f else 1.0f
    val maxLife = life
    val size = if (type == ParticleType.DUST) Random.nextFloat() * 10f + 5f else 15f
    
    fun update(dt: Float) {
        life -= dt
    }
}

class MinionActor(val id: Int, var position: Offset) {
    var velocity = Offset(0f, 0f)
    val maxSpeed = 300f + Random.nextFloat() * 200f // 1.2 - 2.0 m/s roughly mapped to pixels
    private var animationTime = Random.nextFloat() * 10f
    private val scale = 0.6f // Scale down to fit 5 on screen
    
    // Q-Version Proportions
    private val bodyWidth = 80f * scale
    private val bodyHeight = 120f * scale
    
    fun update(dt: Float) {
        position += velocity * dt
        animationTime += dt
    }
    
    fun draw(scope: DrawScope, alpha: Float) {
        val runCycle = sin(animationTime * 15f)
        val bounce = abs(runCycle) * 10f * scale
        
        // 1. Shadow
        val groundY = position.y + bodyHeight/2 + 20f * scale
        // Shadow shrinks and fades when minion bounces up
        val shadowScale = 1f - (bounce / (20f * scale)) * 0.3f
        val shadowAlpha = (0.4f * alpha) * shadowScale
        
        scope.drawOval(
            color = Color.Black.copy(alpha = shadowAlpha.coerceIn(0f, 1f)),
            topLeft = Offset(position.x - (30f * scale * shadowScale), groundY - 5f),
            size = Size(60f * scale * shadowScale, 10f * scale * shadowScale)
        )

        // 2. Reflection
        scope.withTransform({
            translate(position.x, groundY + 5f)
            scale(1f, -0.4f) // Flattened reflection
            rotate(velocity.x * 0.05f)
        }) {
             drawMinionBody(this, alpha * 0.2f, runCycle)
        }

        // 3. Main Body
        scope.withTransform({
            translate(position.x, position.y - bounce)
            // Tilt based on velocity x
            rotate(velocity.x * 0.05f)
        }) {
            drawMinionBody(this, alpha, runCycle)
        }
    }
    
    private fun drawMinionBody(scope: DrawScope, alpha: Float, runCycle: Float) {
        scope.apply {
            // 1. Legs (moving)
            val legOffset = runCycle * 10f
            drawLine(
                Color(0xFF37474F), // Dark Grey pants/legs
                start = Offset(-20f * scale, bodyHeight/2),
                end = Offset(-20f * scale, bodyHeight/2 + 20f * scale + legOffset),
                strokeWidth = 15f * scale,
                cap = StrokeCap.Round
            )
            drawLine(
                Color(0xFF37474F),
                start = Offset(20f * scale, bodyHeight/2),
                end = Offset(20f * scale, bodyHeight/2 + 20f * scale - legOffset),
                strokeWidth = 15f * scale,
                cap = StrokeCap.Round
            )
            
            // 2. Arms (swinging)
            val armAngle = runCycle * 30f
            withTransform({
                translate(-bodyWidth/2, 0f)
                rotate(armAngle)
            }) {
                drawRoundRect(
                    Color(0xFFFFEB3B),
                    topLeft = Offset(-10f * scale, 0f),
                    size = Size(20f * scale, 60f * scale),
                    cornerRadius = CornerRadius(10f * scale)
                )
                // Glove
                drawCircle(Color.Black, radius = 12f * scale, center = Offset(0f, 55f * scale))
            }
             withTransform({
                translate(bodyWidth/2, 0f)
                rotate(-armAngle)
            }) {
                drawRoundRect(
                    Color(0xFFFFEB3B),
                    topLeft = Offset(-10f * scale, 0f),
                    size = Size(20f * scale, 60f * scale),
                    cornerRadius = CornerRadius(10f * scale)
                )
                drawCircle(Color.Black, radius = 12f * scale, center = Offset(0f, 55f * scale))
            }
            
            // 3. Body (Capsule)
            drawRoundRect(
                color = Color(0xFFFFEB3B), // Minion Yellow
                topLeft = Offset(-bodyWidth/2, -bodyHeight/2),
                size = Size(bodyWidth, bodyHeight),
                cornerRadius = CornerRadius(bodyWidth/2)
            )
            
            // 4. Overalls (Blue)
            val pantHeight = bodyHeight * 0.4f
            drawRect(
                color = Color(0xFF1976D2),
                topLeft = Offset(-bodyWidth/2, bodyHeight/2 - pantHeight),
                size = Size(bodyWidth, pantHeight)
            )
            drawArc(
                color = Color(0xFF1976D2),
                startAngle = 0f, sweepAngle = 180f, useCenter = true,
                topLeft = Offset(-bodyWidth/2, bodyHeight/2 - pantHeight - 10f), // Curve bottom
                size = Size(bodyWidth, 20f)
            )
            // Logo on pocket
            drawCircle(Color.Black, radius = 8f * scale, center = Offset(0f, bodyHeight/2 - pantHeight/2))
            
            // 5. Goggles (Strap + Eye)
            drawLine(Color.Black, Offset(-bodyWidth/2, -bodyHeight/4), Offset(bodyWidth/2, -bodyHeight/4), strokeWidth = 8f * scale)
            
            // Eye (One or Two based on ID)
            if (id % 2 == 0) {
                // One Eye
                drawCircle(Color.Gray, radius = 28f * scale, center = Offset(0f, -bodyHeight/4))
                drawCircle(Color.White, radius = 22f * scale, center = Offset(0f, -bodyHeight/4))
                // Pupil (Looking direction)
                val lookX = (velocity.x / maxSpeed) * 5f
                drawCircle(Color(0xFF5D4037), radius = 8f * scale, center = Offset(lookX, -bodyHeight/4))
                drawCircle(Color.Black, radius = 3f * scale, center = Offset(lookX, -bodyHeight/4))
            } else {
                // Two Eyes
                drawCircle(Color.Gray, radius = 18f * scale, center = Offset(-15f * scale, -bodyHeight/4))
                drawCircle(Color.Gray, radius = 18f * scale, center = Offset(15f * scale, -bodyHeight/4))
                
                drawCircle(Color.White, radius = 14f * scale, center = Offset(-15f * scale, -bodyHeight/4))
                drawCircle(Color.White, radius = 14f * scale, center = Offset(15f * scale, -bodyHeight/4))
                
                val lookX = (velocity.x / maxSpeed) * 3f
                drawCircle(Color(0xFF5D4037), radius = 5f * scale, center = Offset(-15f * scale + lookX, -bodyHeight/4))
                drawCircle(Color(0xFF5D4037), radius = 5f * scale, center = Offset(15f * scale + lookX, -bodyHeight/4))
            }
            
            // 6. Mouth (Smile)
            drawArc(
                color = Color.Black,
                startAngle = 20f, sweepAngle = 140f, useCenter = false,
                topLeft = Offset(-10f * scale, -bodyHeight/8),
                size = Size(20f * scale, 10f * scale),
                style = Stroke(width = 2f * scale)
            )
        }
    }
}

private fun Offset.getDistance() = sqrt(x*x + y*y)
private fun Offset.normalized(): Offset {
    val d = getDistance()
    return if (d > 0) this / d else this
}
private fun Offset.limit(max: Float): Offset {
    val d = getDistance()
    return if (d > max) this / d * max else this
}

// --- Wall-E Theme Implementation ---

@Composable
fun AdvancedWallEBackground(globalAlpha: Float) {
    // 1. Blurred Background
    Box(modifier = Modifier.fillMaxSize()) {
         val brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF263238), Color(0xFF37474F), Color(0xFF455A64))
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush)
                .graphicsLayer {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                         renderEffect = RenderEffect.createBlurEffect(
                            20f, 20f, Shader.TileMode.CLAMP
                        ).asComposeRenderEffect()
                    }
                    alpha = globalAlpha
                }
        )
        
        // Stars
        Canvas(modifier = Modifier.fillMaxSize()) {
            val r = Random(42)
            for (i in 0..100) {
                drawCircle(
                    Color.White.copy(alpha = r.nextFloat() * 0.5f * globalAlpha),
                    radius = r.nextFloat() * 2f,
                    center = Offset(r.nextFloat() * size.width, r.nextFloat() * size.height)
                )
            }
        }
    }

    // 2. Wall-E Animation System
    val walle = remember { WallEActor() }
    
    LaunchedEffect(Unit) {
        val frameRate = 1000L / 60L
        var lastTime = System.nanoTime()
        while (isActive) {
            val now = System.nanoTime()
            val dt = (now - lastTime) / 1_000_000_000f
            lastTime = now
            walle.update(dt)
            kotlinx.coroutines.delay(frameRate)
        }
    }
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        walle.updateBounds(size.width, size.height)
        walle.draw(this, globalAlpha)
    }
}

class WallEActor {
    private var position = Offset(100f, 0f) // Y is updated in draw
    private var velocityX = 100f // pixels per second
    private var state = WallEState.MOVING
    private var stateTimer = 0f
    
    private var animationTime = 0f
    private val tracks = mutableListOf<TrackMark>()
    
    // Model Parts
    private var headRotation = 0f
    private var eyeBlink = 0f
    
    enum class WallEState { MOVING, IDLE, LOOKING, HELLO }
    data class TrackMark(val x: Float, val y: Float, val time: Float)
    
    fun updateBounds(w: Float, h: Float) {
        // Wall-E stays at bottom
        if (position.y == 0f) position = position.copy(y = h * 0.85f)
        
        // Edge Turn Logic
        if (position.x < 100f && velocityX < 0) {
            velocityX = -velocityX
            state = WallEState.IDLE // Pause before turn
            stateTimer = 1f
        } else if (position.x > w - 100f && velocityX > 0) {
            velocityX = -velocityX
            state = WallEState.IDLE
            stateTimer = 1f
        }
    }
    
    fun update(dt: Float) {
        animationTime += dt
        
        // State Machine
        if (stateTimer > 0) {
            stateTimer -= dt
            if (stateTimer <= 0) {
                // Pick new state
                state = when (Random.nextInt(4)) {
                    0, 1 -> WallEState.MOVING
                    2 -> WallEState.LOOKING
                    else -> WallEState.HELLO
                }
                stateTimer = Random.nextFloat() * 3f + 2f // 2-5 seconds duration
            }
        } else {
            // Default to moving if timer expired
            state = WallEState.MOVING
            stateTimer = 5f
        }
        
        // Movement Logic
        if (state == WallEState.MOVING) {
            position = position.copy(x = position.x + velocityX * dt)
            
            // Add track marks
            if (animationTime % 0.1f < dt) {
                tracks.add(TrackMark(position.x, position.y, animationTime))
            }
        }
        
        // Animation Logic
        when (state) {
            WallEState.LOOKING -> headRotation = sin(animationTime * 2f) * 30f
            WallEState.HELLO -> headRotation = -20f // Tilt head
            else -> headRotation = lerp(headRotation, 0f, dt * 5f)
        }
        
        // Cleanup old tracks
        tracks.removeAll { animationTime - it.time > 2.0f }
    }
    
    fun draw(scope: DrawScope, alpha: Float) {
        val scale = 0.8f
        val w = 100f * scale
        val h = 100f * scale
        
        // 1. Draw Tracks on Ground
        tracks.forEach { mark ->
            val age = animationTime - mark.time
            val fade = (1f - age / 2f).coerceIn(0f, 1f) * 0.3f
            if (fade > 0) {
                scope.drawRect(
                    color = Color(0xFF546E7A).copy(alpha = fade * alpha),
                    topLeft = Offset(mark.x - w/2, mark.y + h/2 - 10f),
                    size = Size(w, 10f)
                )
            }
        }
        
        // Draw Wall-E
        scope.withTransform({
            translate(position.x, position.y)
            if (velocityX < 0) scale(-1f, 1f) // Flip if moving left
        }) {
            // 2. Tracks (Triangle)
            val trackPath = Path().apply {
                moveTo(-w/2, h/2)
                lineTo(w/2, h/2)
                lineTo(w/3, 0f)
                lineTo(-w/3, 0f)
                close()
            }
            scope.drawPath(trackPath, Color(0xFF455A64).copy(alpha = alpha))
            // Tread details (animate texture)
            val treadOffset = (animationTime * 100f) % 20f
            scope.clipPath(trackPath) {
                for(i in -5..5) {
                    val x = i * 20f + treadOffset
                    scope.drawLine(Color.Black.copy(alpha=0.3f*alpha), Offset(x, 0f), Offset(x, h/2), 2f)
                }
            }
            
            // 3. Body (Cube)
            scope.drawRect(
                Color(0xFFFFA000).copy(alpha = alpha), // Dirty Yellow
                topLeft = Offset(-w/3, -h/2),
                size = Size(w*0.66f, h/2)
            )
            // Chest details
            scope.drawRect(
                Color(0xFF455A64).copy(alpha = alpha),
                topLeft = Offset(-w/6, -h/2 + 10f),
                size = Size(w/3, h/4)
            )
            // Solar charge level
            scope.drawRect(Color.Green, topLeft = Offset(-w/6 + 5f, -h/2 + 15f), Size(5f, 20f))
            
            // 4. Neck
            scope.drawRect(
                Color(0xFFCFD8DC).copy(alpha = alpha),
                topLeft = Offset(-5f, -h/2 - 30f),
                size = Size(10f, 30f)
            )
            
            // 5. Head & Eyes
            scope.withTransform({
                translate(0f, -h/2 - 30f)
                rotate(headRotation)
            }) {
                val eyeW = 35f * scale
                val eyeH = 20f * scale
                
                // Left Eye
                scope.drawOval(
                    Color(0xFFCFD8DC).copy(alpha = alpha),
                    topLeft = Offset(-eyeW - 2f, -eyeH),
                    size = Size(eyeW, eyeH)
                )
                // Right Eye
                scope.drawOval(
                    Color(0xFFCFD8DC).copy(alpha = alpha),
                    topLeft = Offset(2f, -eyeH),
                    size = Size(eyeW, eyeH)
                )
                
                // Pupils (Black)
                scope.drawCircle(Color.Black.copy(alpha=alpha), radius=6f, center=Offset(-eyeW/2 - 2f, -eyeH/2))
                scope.drawCircle(Color.Black.copy(alpha=alpha), radius=6f, center=Offset(eyeW/2 + 2f, -eyeH/2))
                
                // Blue Glow (LED)
                scope.drawCircle(Color(0xFF00B0FF).copy(alpha=0.5f*alpha), radius=3f, center=Offset(-eyeW/2 - 2f, -eyeH/2))
            }
            
            // 6. Arms (Swinging)
            if (state == WallEState.HELLO) {
                // Wave hand
                scope.withTransform({
                    translate(w/3, -h/4)
                    rotate(sin(animationTime * 10f) * 45f - 45f)
                }) {
                     scope.drawRect(Color(0xFFCFD8DC).copy(alpha=alpha), topLeft=Offset(0f, -5f), size=Size(40f, 10f))
                     // Hand
                     scope.drawOval(Color(0xFF455A64).copy(alpha=alpha), topLeft=Offset(35f, -10f), size=Size(15f, 20f))
                }
            } else {
                // Resting
                scope.drawRect(
                    Color(0xFFCFD8DC).copy(alpha = alpha),
                    topLeft = Offset(w/3, -h/4),
                    size = Size(10f, 30f)
                )
            }
        }
    }
    
    private fun lerp(start: Float, stop: Float, fraction: Float): Float {
        return (1 - fraction) * start + fraction * stop
    }
}
