package com.behealthy.app.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ThemeRotatingIcon(
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    rotationEnabled: Boolean,
    rotationSpeedSeconds: Float, // 0-10s
    rotationDirection: String // "Clockwise" or "CounterClockwise"
) {
    // Rotation Animation
    val infiniteTransition = rememberInfiniteTransition(label = "zen_rotation")
    
    // Convert speed (seconds per rotation) to durationMillis
    // If speed is 0, it's static (or infinite speed? User likely means 0 = stop or very fast? usually 0 means stop in this context, but "0-10s/circle" implies duration. 0 duration is impossible. 
    // Let's assume 0 means "stop" or "very fast". User prompt: "0-10秒/圈". 0 usually means static.
    
    val durationMillis = (rotationSpeedSeconds * 1000).toInt()
    
    val rotation by if (rotationEnabled && durationMillis > 0) {
        val targetValue = if (rotationDirection == "Clockwise") 360f else -360f
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = targetValue,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotation"
        )
    } else {
        remember { mutableStateOf(0f) } // Static
    }

    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(24.dp)) {
            // Draw Zen Circle (Enso)
            drawArc(
                color = Color(0xFF4A5D23),
                startAngle = 45f,
                sweepAngle = 300f,
                useCenter = false,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        
        // The Icon itself rotates? Or the "Zen" character?
        // User request: "请实现禅主题中\"禅\"字的动态旋转效果" -> "Implement dynamic rotation effect of 'Zen' character in Zen theme".
        // The current icon is an ImageVector (generic). The user wants a "Zen" CHARACTER (Text).
        // So I should replace the Icon with a Text "禅" if it's Zen theme?
        // Or is the "Zen" character PART of the icon?
        // The prompt says: "Zen theme 'Zen' character dynamic rotation".
        // In MainApp.kt, ThemedIcon takes an `icon: ImageVector`.
        // If I am to replace the icon with "Zen" character, I should ignore the `icon` parameter or overlay it?
        // Usually bottom navigation icons are fixed (Home, Profile etc).
        // Maybe the user means the DECORATION rotates, or the icon itself is replaced by "Zen" character?
        // "要求该字体元素支持360度平滑旋转动画" -> "Require this font element to support 360 degree smooth rotation".
        // This suggests there is a "Zen" character displayed.
        // Let's assume the icon is replaced by "禅" text, OR the icon rotates.
        // Given "Zen theme", usually the central element or a decorative element.
        // If it's the bottom nav icon, replacing "Home" icon with "禅" text might be confusing.
        // BUT, looking at `MainApp.kt` line 235: `Icon(icon, ...)` is used.
        // If the user specifically asked for "Zen character rotation", maybe they mean the central decorative element in the background or a specific "Zen" widget.
        // HOWEVER, the context is "ThemeRotatingIcon" which sounds like it replaces `ThemedIcon`.
        // Let's implement it such that the CONTENT rotates.
        // If I look at the previous analysis: "Replaced Zen icon with static "禅" character...".
        // So I should probably render a "禅" character instead of the `icon` ImageVector?
        // OR maybe the `icon` passed IS the "Zen" icon in some context? No, `MainApp` passes `Icons.Default.Home` etc.
        
        // Let's stick to the prompt: "Zen character dynamic rotation".
        // I will render the text "禅" and rotate IT.
        // But wait, if I replace "Home" icon with "禅", how does the user know it's Home?
        // Maybe the user means the DECORATION circle rotates?
        // "禅"字 -> "Zen" character.
        // "该字体元素" -> "This font element".
        // Okay, I will render "禅" text in the center and rotate it.
        // And maybe show the original icon small? Or just the text?
        // Let's render "禅" text.
        
        // Wait, if I replace the icon with text, it breaks the semantic of the button.
        // Maybe the user wants the "Zen" character to be the "Logo" or "Decoration"?
        // Let's assume for now I will rotate the "content" of the box.
        // If I look at the existing `ThemedIcon` for Zen:
        // It draws an Arc and an Icon.
        // I will replace the Icon with "禅" Text if the user wants "Zen character".
        // Let's try to render "禅" text using `drawText` or a `Text` composable.
        // Using `Text` composable inside `Box` is easier.
        
        Box(
            modifier = Modifier
                .rotate(rotation) // Rotate the text
                .align(Alignment.Center)
        ) {
             Text(
                text = "禅",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A5D23)
                )
            )
        }
    }
}
