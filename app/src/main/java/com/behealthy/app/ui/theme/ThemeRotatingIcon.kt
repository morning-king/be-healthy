package com.behealthy.app.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun ThemeRotatingIcon(
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    rotationEnabled: Boolean = true,
    rotationSpeedSeconds: Float = 5f,
    rotationDirection: String = "Clockwise"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "zen_rotation")
    
    // Determine rotation direction
    val directionMultiplier = if (rotationDirection == "Clockwise") 1f else -1f
    
    // Calculate duration in milliseconds (default to 5000ms if speed is 0 or invalid)
    val durationMillis = if (rotationSpeedSeconds > 0) (rotationSpeedSeconds * 1000).toInt() else 5000

    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f * directionMultiplier,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_angle"
    )

    // Only rotate if enabled and selected (or always if that's the design intent, but usually active tab icon rotates)
    // Based on user request "Zen character dynamic rotation", likely always rotating or when active.
    // The usage in MainApp passes `isSelected`, so we might want to rotate only when selected or always?
    // User requirement: "Zen '禅' character dynamic rotation... toggle switch".
    // If toggle is off (rotationEnabled=false), don't rotate.
    
    val currentRotation = if (rotationEnabled) angle else 0f

    Box(contentAlignment = Alignment.Center) {
        // Zen Background (Circle with Yin-Yang or simple Zen aesthetic)
        // For Zen theme, let's keep it simple: a circle background and the rotating icon?
        // Or is the icon itself the "Zen character"?
        // In MainApp, it's passed an icon (e.g. Person, Home).
        // Let's draw a simple Zen-style background (Enso circle maybe?)
        // For now, simple circle background matching other themes.
        
        // Draw Enso-like circle (imperfect circle)
        // Since we are in Compose, simple Circle is safer.
        
        androidx.compose.foundation.Canvas(modifier = Modifier.size(24.dp)) {
             drawCircle(Color(0xFF8D6E63)) // Wood/Zen color
             drawCircle(Color(0xFFFBFDF9), radius = size.minDimension / 2 - 2.dp.toPx()) // Inner light
        }

        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isSelected) Color(0xFF5D4037) else Color(0xFF8D6E63),
            modifier = Modifier
                .scale(0.6f)
                .rotate(currentRotation)
        )
    }
}
