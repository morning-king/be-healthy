package com.behealthy.app.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.behealthy.app.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

@Composable
fun ThemeRotatingIcon(
    themeStyle: ThemeStyle,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "theme_spin")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    // Container for the icon
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        val density = LocalDensity.current
        // Adjust stroke width based on size? 
        // We'll keep it relative or fixed. 1.5.dp is fine.
        val strokeWidth = with(density) { 1.5.dp.toPx() }
        
        Canvas(modifier = Modifier.fillMaxSize()) { 
            val sizePx = size.minDimension
            val radius = sizePx / 2
            
            rotate(angle) {
                when (themeStyle) {
                    ThemeStyle.FootballWorldCup -> drawWorldCupSoccer(radius, center)
                    ThemeStyle.NBA -> drawBasketball(radius, center, strokeWidth)
                    ThemeStyle.Dao -> drawTaiChi(radius, center, strokeWidth)
                    ThemeStyle.Zen -> drawLotus(radius, center, strokeWidth)
                    ThemeStyle.Tech -> drawReactor(radius, center, strokeWidth)
                    ThemeStyle.Cute -> drawFlower(radius, center, strokeWidth)
                    ThemeStyle.Doraemon -> drawBell(radius, center, strokeWidth)
                    ThemeStyle.Minions -> drawMinionEye(radius, center, strokeWidth)
                    ThemeStyle.WallE -> drawGear(radius, center, strokeWidth)
                    ThemeStyle.NewYear -> drawLanternPattern(radius, center, strokeWidth)
                    ThemeStyle.Badminton -> drawShuttlecockTop(radius, center, strokeWidth)
                    ThemeStyle.Sports -> drawStopwatchWheel(radius, center, strokeWidth)
                    ThemeStyle.Default -> drawHealthCross(radius, center, strokeWidth)
                }
            }
            
            // Optional: Static overlays (e.g. shading) could go here if not rotated
            // But user requested "rotating animation effect", so rotating everything is safer for "spin".
            // Except for lighting effects which should stay static? 
            // For now, let's rotate the graphic.
            if (themeStyle == ThemeStyle.FootballWorldCup) {
                 drawWorldCupShading(radius, size)
            }
        }
    }
}

// --- Drawing Implementations ---

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawWorldCupSoccer(radius: Float, center: Offset) {
    // Ball Background
    drawCircle(Color.White, radius = radius)
    
    // Telstar Pattern (Simplified Pentagon + Connections)
    val pentagonPath = Path()
    val pRadius = radius * 0.35f
    for (i in 0 until 5) {
        val angleRad = Math.toRadians((i * 72 - 90).toDouble())
        val x = center.x + pRadius * cos(angleRad).toFloat()
        val y = center.y + pRadius * sin(angleRad).toFloat()
        if (i == 0) pentagonPath.moveTo(x, y) else pentagonPath.lineTo(x, y)
    }
    pentagonPath.close()
    drawPath(pentagonPath, Color.Black)
    
    val outerRadius = radius * 0.9f
    for (i in 0 until 5) {
        val angleRad = Math.toRadians((i * 72 - 90).toDouble())
        val pX = center.x + pRadius * cos(angleRad).toFloat()
        val pY = center.y + pRadius * sin(angleRad).toFloat()
        val endX = center.x + outerRadius * cos(angleRad).toFloat()
        val endY = center.y + outerRadius * sin(angleRad).toFloat()
        
        drawLine(Color.Black, Offset(pX, pY), Offset(endX, endY), strokeWidth = radius * 0.08f)
        drawCircle(Color.Black, radius = radius * 0.15f, center = Offset(endX, endY))
    }
    
    // Outer stroke
    drawCircle(Color.Black, radius = radius, style = Stroke(1.dp.toPx()))
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawWorldCupShading(radius: Float, size: Size) {
    // Static 3D Shading (Overlay) - NOT ROTATED
    // Highlight
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha=0.6f), Color.Transparent),
            center = Offset(size.width*0.3f, size.height*0.3f),
            radius = radius * 0.8f
        ),
        radius = radius
    )
    // Shadow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.Transparent, Color.Black.copy(alpha=0.4f)),
            center = Offset(size.width*0.4f, size.height*0.4f),
            radius = radius * 1.2f
        ),
        radius = radius
    )
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBasketball(radius: Float, center: Offset, strokeWidth: Float) {
    drawCircle(Color(0xFFFF6D00), radius = radius) // Orange
    
    val lineStroke = Stroke(strokeWidth * 1.5f)
    val lineColor = Color.Black
    
    // Cross lines
    drawLine(lineColor, Offset(center.x - radius, center.y), Offset(center.x + radius, center.y), strokeWidth = strokeWidth * 1.5f)
    drawLine(lineColor, Offset(center.x, center.y - radius), Offset(center.x, center.y + radius), strokeWidth = strokeWidth * 1.5f)
    
    // Curved lines (Sine wave approximation or simple arcs)
    drawCircle(lineColor, radius = radius, style = Stroke(strokeWidth * 1.5f))
    
    // Curves
    val path = Path()
    path.moveTo(center.x - radius * 0.7f, center.y - radius * 0.7f)
    path.quadraticBezierTo(center.x, center.y, center.x + radius * 0.7f, center.y + radius * 0.7f)
    // Simplify: Just draw a circle + X + a curve?
    // Let's do a simple cross + circle outline + one large curve
    drawArc(lineColor, startAngle = 0f, sweepAngle = 180f, useCenter = false, topLeft = Offset(center.x - radius/2, center.y - radius), size = Size(radius, radius*2), style = lineStroke)
    drawArc(lineColor, startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(center.x - radius/2, center.y - radius), size = Size(radius, radius*2), style = lineStroke)
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTaiChi(radius: Float, center: Offset, strokeWidth: Float) {
    // White background
    drawCircle(Color.White, radius = radius)
    // Black half
    drawArc(Color.Black, startAngle = -90f, sweepAngle = 180f, useCenter = true, size = Size(radius*2, radius*2), topLeft = Offset(center.x - radius, center.y - radius))
    
    // Small circles
    drawCircle(Color.Black, radius = radius/2, center = Offset(center.x, center.y - radius/2))
    drawCircle(Color.White, radius = radius/2, center = Offset(center.x, center.y + radius/2))
    
    // Dots
    drawCircle(Color.White, radius = radius/6, center = Offset(center.x, center.y - radius/2))
    drawCircle(Color.Black, radius = radius/6, center = Offset(center.x, center.y + radius/2))
    
    // Outline
    drawCircle(Color.Black, radius = radius, style = Stroke(strokeWidth))
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLotus(radius: Float, center: Offset, strokeWidth: Float) {
    drawCircle(Color(0xFFF1F8E9), radius = radius) // Light background
    
    val petalColor = Color(0xFF8D6E63) // Wood/Zen color
    val petals = 8
    for (i in 0 until petals) {
        val angle = (i * 360f / petals)
        rotate(angle) {
            drawOval(petalColor, topLeft = Offset(center.x - radius/4, center.y - radius), size = Size(radius/2, radius))
        }
    }
    drawCircle(Color(0xFF4A5D23), radius = radius/3) // Center
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawReactor(radius: Float, center: Offset, strokeWidth: Float) {
    val colorPrimary = Color(0xFF00B0FF)
    val colorSecondary = Color(0xFF6200EA)
    
    drawCircle(Color(0xFF0B132B), radius = radius) // Dark BG
    
    // Core
    drawCircle(colorPrimary, radius = radius/3)
    drawCircle(Color.White, radius = radius/6)
    
    // Orbiting Arcs
    val arcRect = Size(radius*1.6f, radius*1.6f)
    val arcOffset = Offset(center.x - radius*0.8f, center.y - radius*0.8f)
    
    drawArc(colorSecondary, startAngle = 0f, sweepAngle = 90f, useCenter = false, topLeft = arcOffset, size = arcRect, style = Stroke(strokeWidth*3))
    drawArc(colorSecondary, startAngle = 120f, sweepAngle = 90f, useCenter = false, topLeft = arcOffset, size = arcRect, style = Stroke(strokeWidth*3))
    drawArc(colorSecondary, startAngle = 240f, sweepAngle = 90f, useCenter = false, topLeft = arcOffset, size = arcRect, style = Stroke(strokeWidth*3))
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFlower(radius: Float, center: Offset, strokeWidth: Float) {
    val pink = Color(0xFFFF80AB)
    val yellow = Color(0xFFFFF59D)
    
    drawCircle(Color.White, radius = radius)
    
    val petals = 5
    for (i in 0 until petals) {
        rotate(i * 360f / petals) {
            drawCircle(pink, radius = radius/2, center = Offset(center.x, center.y - radius/2))
        }
    }
    drawCircle(yellow, radius = radius/3)
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBell(radius: Float, center: Offset, strokeWidth: Float) {
    val gold = Color(0xFFFFD700)
    val red = Color(0xFFDD0000) // Doraemon Red
    
    drawCircle(gold, radius = radius)
    
    // Center lines
    val yOffset = radius * 0.2f
    drawLine(Color.Black, Offset(center.x - radius * 0.9f, center.y - yOffset), Offset(center.x + radius * 0.9f, center.y - yOffset), strokeWidth = strokeWidth)
    drawLine(Color.Black, Offset(center.x - radius * 0.9f, center.y + yOffset), Offset(center.x + radius * 0.9f, center.y + yOffset), strokeWidth = strokeWidth)
    
    // Hole
    drawCircle(Color.Black, radius = radius * 0.15f, center = Offset(center.x, center.y + radius * 0.5f))
    // Line to hole
    drawLine(Color.Black, Offset(center.x, center.y + yOffset), Offset(center.x, center.y + radius * 0.5f), strokeWidth = strokeWidth)
    
    // Outline
    drawCircle(Color.Black, radius = radius, style = Stroke(strokeWidth))
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMinionEye(radius: Float, center: Offset, strokeWidth: Float) {
    // Goggle Frame
    drawCircle(Color(0xFF808080), radius = radius)
    // Strap
    drawLine(Color.Black, Offset(center.x - radius, center.y), Offset(center.x + radius, center.y), strokeWidth = radius * 0.2f)
    
    // Eye White
    drawCircle(Color.White, radius = radius * 0.8f)
    
    // Iris (Brown)
    drawCircle(Color(0xFF8D6E63), radius = radius * 0.3f)
    // Pupil
    drawCircle(Color.Black, radius = radius * 0.15f)
    // Glint
    drawCircle(Color.White, radius = radius * 0.05f, center = Offset(center.x - radius * 0.1f, center.y - radius * 0.1f))
    
    // Frame Rim
    drawCircle(Color(0xFF607D8B), radius = radius, style = Stroke(strokeWidth * 3))
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGear(radius: Float, center: Offset, strokeWidth: Float) {
    val rust = Color(0xFFD2691E)
    val metal = Color(0xFF5F6A6A)
    
    // Gear teeth
    val teeth = 8
    val outerR = radius
    val innerR = radius * 0.8f
    
    val path = Path()
    for (i in 0 until teeth * 2) {
        val angle = Math.toRadians((i * 360.0 / (teeth * 2)))
        val r = if (i % 2 == 0) outerR else innerR
        val x = center.x + r * cos(angle).toFloat()
        val y = center.y + r * sin(angle).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    
    drawPath(path, rust)
    drawCircle(metal, radius = radius * 0.5f)
    drawCircle(Color.Black, radius = radius * 0.2f)
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLanternPattern(radius: Float, center: Offset, strokeWidth: Float) {
    // Red Diamond
    val red = Color(0xFFFF0000)
    val gold = Color(0xFFFFD700)
    
    rotate(45f) {
        drawRect(red, topLeft = Offset(center.x - radius*0.7f, center.y - radius*0.7f), size = Size(radius*1.4f, radius*1.4f))
        drawRect(gold, topLeft = Offset(center.x - radius*0.7f, center.y - radius*0.7f), size = Size(radius*1.4f, radius*1.4f), style = Stroke(strokeWidth))
    }
    
    // "Fu" lines (Abstract)
    drawLine(gold, Offset(center.x - radius*0.3f, center.y), Offset(center.x + radius*0.3f, center.y), strokeWidth = strokeWidth*2)
    drawLine(gold, Offset(center.x, center.y - radius*0.3f), Offset(center.x, center.y + radius*0.3f), strokeWidth = strokeWidth*2)
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawShuttlecockTop(radius: Float, center: Offset, strokeWidth: Float) {
    // Cork
    drawCircle(Color.White, radius = radius * 0.3f)
    drawCircle(Color.Black, radius = radius * 0.3f, style = Stroke(strokeWidth))
    
    // Feathers (12 lines)
    val feathers = 12
    for (i in 0 until feathers) {
        rotate(i * 360f / feathers) {
            // Draw feather shape
            drawOval(Color.White, topLeft = Offset(center.x - radius*0.1f, center.y - radius), size = Size(radius*0.2f, radius*0.7f))
            drawOval(Color.Black, topLeft = Offset(center.x - radius*0.1f, center.y - radius), size = Size(radius*0.2f, radius*0.7f), style = Stroke(strokeWidth))
        }
    }
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStopwatchWheel(radius: Float, center: Offset, strokeWidth: Float) {
    drawCircle(Color(0xFF212121), radius = radius) // Black tire
    drawCircle(Color(0xFFEEEEEE), radius = radius * 0.6f) // Rim
    
    // Spokes
    val spokes = 5
    for (i in 0 until spokes) {
        rotate(i * 360f / spokes) {
            drawLine(Color(0xFF212121), center, Offset(center.x, center.y - radius * 0.6f), strokeWidth = strokeWidth*2)
        }
    }
    
    drawCircle(Color(0xFFFF5722), radius = radius * 0.15f) // Center cap
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHealthCross(radius: Float, center: Offset, strokeWidth: Float) {
    val green = Color(0xFF006C4C)
    drawCircle(Color.White, radius = radius)
    drawCircle(green, radius = radius, style = Stroke(strokeWidth))
    
    // Cross
    val barW = radius * 0.4f
    val barL = radius * 1.2f
    
    drawRect(green, topLeft = Offset(center.x - barW/2, center.y - barL/2), size = Size(barW, barL))
    drawRect(green, topLeft = Offset(center.x - barL/2, center.y - barW/2), size = Size(barL, barW))
}
