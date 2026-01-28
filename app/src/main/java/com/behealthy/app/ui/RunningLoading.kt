package com.behealthy.app.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.behealthy.app.ui.theme.ThemeStyle
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RunningLoading(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    themeStyle: ThemeStyle = ThemeStyle.Default
) {
    val infiniteTransition = rememberInfiniteTransition(label = "running_animation")

    // Bobbing animation (up and down movement)
    val bobbing by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bobbing"
    )

    // Running cycle (legs and arms)
    val runCycle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "runCycle"
    )

    // Shadow scale
    val shadowScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shadow"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val width = this.size.width
            val height = this.size.height
            val centerX = width / 2
            val centerY = height / 2
            
            // Bobbing effect offset
            val bobOffset = height * 0.05f * bobbing
            val baseY = centerY - height * 0.1f + bobOffset

            // Draw Shadow
            drawOval(
                color = color.copy(alpha = 0.2f),
                topLeft = Offset(centerX - width * 0.3f * shadowScale, height * 0.85f),
                size = Size(width * 0.6f * shadowScale, height * 0.1f)
            )

            when (themeStyle) {
                ThemeStyle.WallE -> drawWallE(color, centerX, baseY, width, height, runCycle)
                ThemeStyle.Doraemon -> drawDoraemon(color, centerX, baseY, width, height, runCycle)
                ThemeStyle.Minions -> drawMinion(color, centerX, baseY, width, height, runCycle)
                ThemeStyle.NBA -> drawNBA(color, centerX, baseY, width, height, runCycle)
                ThemeStyle.Badminton -> drawBadminton(color, centerX, baseY, width, height, runCycle)
                ThemeStyle.FootballWorldCup -> drawWorldCup(color, centerX, baseY, width, height, runCycle)
                ThemeStyle.Tech -> drawRobot(color, centerX, baseY, width, height, runCycle)
                else -> drawDefaultRunner(color, centerX, baseY, width, height, runCycle)
            }
        }
    }
}

private fun DrawScope.drawBadminton(
    color: Color, centerX: Float, baseY: Float, width: Float, height: Float, runCycle: Float
) {
    val shuttleHeight = height * 0.6f
    val shuttleWidth = width * 0.5f
    
    // Cork (Head) - Semi-circle
    val corkRadius = shuttleWidth * 0.25f
    drawArc(
        color = Color.White,
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(centerX - corkRadius, baseY + shuttleHeight/2 - corkRadius),
        size = Size(corkRadius * 2, corkRadius * 2)
    )
    
    // Feathers (Cone)
    val path = androidx.compose.ui.graphics.Path()
    path.moveTo(centerX - corkRadius, baseY + shuttleHeight/2)
    path.lineTo(centerX + corkRadius, baseY + shuttleHeight/2)
    path.lineTo(centerX + shuttleWidth/2, baseY - shuttleHeight/2)
    path.lineTo(centerX - shuttleWidth/2, baseY - shuttleHeight/2)
    path.close()
    
    drawPath(path = path, color = Color.White)
    
    // Stripes on feathers (simplified)
    drawLine(
        color = Color.Black,
        start = Offset(centerX - shuttleWidth * 0.4f, baseY - shuttleHeight * 0.2f),
        end = Offset(centerX + shuttleWidth * 0.4f, baseY - shuttleHeight * 0.2f),
        strokeWidth = 2.dp.toPx()
    )
}

private fun DrawScope.drawWorldCup(
    color: Color, centerX: Float, baseY: Float, width: Float, height: Float, runCycle: Float
) {
    val ballRadius = width * 0.35f
    
    // Ball Body (White)
    drawCircle(
        color = Color.White,
        radius = ballRadius,
        center = Offset(centerX, baseY)
    )
    
    // Pentagons (Black) - Simplified pattern
    val pentagonRadius = ballRadius * 0.5f
    val path = androidx.compose.ui.graphics.Path()
    for (i in 0 until 5) {
        val angle = Math.toRadians((i * 72 - 90).toDouble())
        val x = centerX + pentagonRadius * cos(angle).toFloat()
        val y = baseY + pentagonRadius * sin(angle).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path = path, color = Color.Black)
    
    // Connecting lines to edge
    for (i in 0 until 5) {
        val angle = Math.toRadians((i * 72 - 90).toDouble())
        val innerX = centerX + pentagonRadius * cos(angle).toFloat()
        val innerY = baseY + pentagonRadius * sin(angle).toFloat()
        
        val outerX = centerX + ballRadius * cos(angle).toFloat()
        val outerY = baseY + ballRadius * sin(angle).toFloat()
        
        drawLine(
            color = Color.Black,
            start = Offset(innerX, innerY),
            end = Offset(outerX, outerY),
            strokeWidth = 3.dp.toPx()
        )
    }
}

private fun DrawScope.drawNBA(
    color: Color, centerX: Float, baseY: Float, width: Float, height: Float, runCycle: Float
) {
    val ballRadius = width * 0.35f
    
    // Ball Body (Orange)
    drawCircle(
        color = Color(0xFFE65100), // Basketball Orange
        radius = ballRadius,
        center = Offset(centerX, baseY)
    )
    
    val strokeWidth = width * 0.04f
    
    // Vertical Line
    drawLine(
        color = Color.Black,
        start = Offset(centerX, baseY - ballRadius),
        end = Offset(centerX, baseY + ballRadius),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    
    // Horizontal Line
    drawLine(
        color = Color.Black,
        start = Offset(centerX - ballRadius, baseY),
        end = Offset(centerX + ballRadius, baseY),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    
    // Curves (Side curves)
    val curveRadius = ballRadius * 0.7f
    
    // Left Curve
    drawArc(
        color = Color.Black,
        startAngle = -45f,
        sweepAngle = 90f,
        useCenter = false,
        topLeft = Offset(centerX - ballRadius - curveRadius/2, baseY - ballRadius),
        size = Size(ballRadius, ballRadius * 2),
        style = Stroke(width = strokeWidth)
    )

    // Right Curve
    drawArc(
        color = Color.Black,
        startAngle = 135f,
        sweepAngle = 90f,
        useCenter = false,
        topLeft = Offset(centerX + curveRadius/2, baseY - ballRadius),
        size = Size(ballRadius, ballRadius * 2),
        style = Stroke(width = strokeWidth)
    )
}

private fun DrawScope.drawWallE(
    color: Color, centerX: Float, baseY: Float, width: Float, height: Float, runCycle: Float
) {
    val bodyWidth = width * 0.4f
    val bodyHeight = height * 0.35f
    val headSize = width * 0.25f
    
    // Tracks (Wheels) - Triangle shape roughly
    val trackHeight = height * 0.15f
    val trackWidth = width * 0.5f
    
    // Draw Tracks
    drawRoundRect(
        color = Color(0xFF34495E), // Dark grey tracks
        topLeft = Offset(centerX - trackWidth/2, baseY + bodyHeight/2),
        size = Size(trackWidth, trackHeight),
        cornerRadius = CornerRadius(trackHeight/2, trackHeight/2)
    )
    
    // Body (Cube)
    drawRect(
        color = color, // Rusty Orange
        topLeft = Offset(centerX - bodyWidth/2, baseY - bodyHeight/2),
        size = Size(bodyWidth, bodyHeight)
    )
    
    // Neck
    drawLine(
        color = Color(0xFF95A5A6),
        start = Offset(centerX, baseY - bodyHeight/2),
        end = Offset(centerX, baseY - bodyHeight/2 - headSize/2),
        strokeWidth = width * 0.05f
    )
    
    // Head (Binoculars)
    val eyeWidth = headSize * 0.8f
    val eyeHeight = headSize * 0.5f
    
    // Left Eye
    drawOval(
        color = Color(0xFFECF0F1), // Silver/White
        topLeft = Offset(centerX - eyeWidth, baseY - bodyHeight/2 - headSize),
        size = Size(eyeWidth, eyeHeight)
    )
    // Right Eye
    drawOval(
        color = Color(0xFFECF0F1),
        topLeft = Offset(centerX, baseY - bodyHeight/2 - headSize),
        size = Size(eyeWidth, eyeHeight)
    )
    
    // Pupils
    drawCircle(
        color = Color.Black,
        radius = eyeHeight * 0.2f,
        center = Offset(centerX - eyeWidth/2, baseY - bodyHeight/2 - headSize + eyeHeight/2)
    )
    drawCircle(
        color = Color.Black,
        radius = eyeHeight * 0.2f,
        center = Offset(centerX + eyeWidth/2, baseY - bodyHeight/2 - headSize + eyeHeight/2)
    )
    
    // Arms (Simple lines)
    val armAngle = sin(runCycle.toDouble()).toFloat() * 0.5f
    val armLength = width * 0.3f
    
    drawLine(
        color = Color(0xFF34495E),
        start = Offset(centerX - bodyWidth/2, baseY),
        end = Offset(centerX - bodyWidth/2 - cos(armAngle.toDouble()).toFloat()*armLength, baseY + sin(armAngle.toDouble()).toFloat()*armLength),
        strokeWidth = width * 0.04f,
        cap = StrokeCap.Round
    )
    
    drawLine(
        color = Color(0xFF34495E),
        start = Offset(centerX + bodyWidth/2, baseY),
        end = Offset(centerX + bodyWidth/2 + cos(armAngle.toDouble()).toFloat()*armLength, baseY - sin(armAngle.toDouble()).toFloat()*armLength),
        strokeWidth = width * 0.04f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawDoraemon(
    color: Color, centerX: Float, baseY: Float, width: Float, height: Float, runCycle: Float
) {
    val headRadius = width * 0.35f
    val bodyHeight = height * 0.3f
    
    // Body (Blue)
    drawRoundRect(
        color = color, // Doraemon Blue
        topLeft = Offset(centerX - headRadius*0.9f, baseY),
        size = Size(headRadius*1.8f, bodyHeight),
        cornerRadius = CornerRadius(20f, 20f)
    )
    
    // Belly (White)
    drawCircle(
        color = Color.White,
        radius = headRadius * 0.7f,
        center = Offset(centerX, baseY + bodyHeight * 0.5f)
    )
    
    // Bell (Yellow)
    drawCircle(
        color = Color(0xFFFFD700),
        radius = width * 0.05f,
        center = Offset(centerX, baseY + width * 0.05f)
    )
    
    // Head (Blue)
    drawCircle(
        color = color,
        radius = headRadius,
        center = Offset(centerX, baseY - headRadius * 0.6f)
    )
    
    // Face (White)
    drawCircle(
        color = Color.White,
        radius = headRadius * 0.8f,
        center = Offset(centerX, baseY - headRadius * 0.5f)
    )
    
    // Nose (Red)
    drawCircle(
        color = Color.Red,
        radius = headRadius * 0.15f,
        center = Offset(centerX, baseY - headRadius * 0.6f)
    )
    
    // Whiskers
    val whiskerLength = headRadius * 0.4f
    for (i in -1..1) {
        val yOff = i * 10f
        drawLine(
            color = Color.Black,
            start = Offset(centerX + headRadius * 0.2f, baseY - headRadius * 0.6f + yOff),
            end = Offset(centerX + headRadius * 0.2f + whiskerLength, baseY - headRadius * 0.6f + yOff + i*5),
            strokeWidth = 2f
        )
        drawLine(
            color = Color.Black,
            start = Offset(centerX - headRadius * 0.2f, baseY - headRadius * 0.6f + yOff),
            end = Offset(centerX - headRadius * 0.2f - whiskerLength, baseY - headRadius * 0.6f + yOff + i*5),
            strokeWidth = 2f
        )
    }

    // Arms & Legs (Simple)
    val limbLength = width * 0.15f
    val armAngle = sin(runCycle.toDouble()).toFloat() * 0.8f
    
    // Arms
    drawLine(
        color = color,
        start = Offset(centerX - headRadius*0.8f, baseY + bodyHeight*0.2f),
        end = Offset(centerX - headRadius*1.2f, baseY + bodyHeight*0.2f + sin(armAngle.toDouble()).toFloat()*limbLength),
        strokeWidth = width * 0.08f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = color,
        start = Offset(centerX + headRadius*0.8f, baseY + bodyHeight*0.2f),
        end = Offset(centerX + headRadius*1.2f, baseY + bodyHeight*0.2f - sin(armAngle.toDouble()).toFloat()*limbLength),
        strokeWidth = width * 0.08f,
        cap = StrokeCap.Round
    )
    
    // White Hands
    drawCircle(
        color = Color.White,
        radius = width * 0.06f,
        center = Offset(centerX - headRadius*1.2f, baseY + bodyHeight*0.2f + sin(armAngle.toDouble()).toFloat()*limbLength)
    )
    drawCircle(
        color = Color.White,
        radius = width * 0.06f,
        center = Offset(centerX + headRadius*1.2f, baseY + bodyHeight*0.2f - sin(armAngle.toDouble()).toFloat()*limbLength)
    )
}

private fun DrawScope.drawMinion(
    color: Color, centerX: Float, baseY: Float, width: Float, height: Float, runCycle: Float
) {
    val bodyWidth = width * 0.5f
    val bodyHeight = height * 0.7f
    
    // Body (Yellow)
    drawRoundRect(
        color = color, // Yellow
        topLeft = Offset(centerX - bodyWidth/2, baseY - bodyHeight/2),
        size = Size(bodyWidth, bodyHeight),
        cornerRadius = CornerRadius(bodyWidth/2, bodyWidth/2)
    )
    
    // Overalls (Blue)
    drawRect(
        color = Color(0xFF1E62A6),
        topLeft = Offset(centerX - bodyWidth/2, baseY + bodyHeight * 0.1f),
        size = Size(bodyWidth, bodyHeight * 0.4f)
    )
    
    // Goggle Strap
    drawRect(
        color = Color.Black,
        topLeft = Offset(centerX - bodyWidth/2, baseY - bodyHeight * 0.25f),
        size = Size(bodyWidth, height * 0.05f)
    )
    
    // Eye (One big eye)
    val eyeRadius = bodyWidth * 0.3f
    drawCircle(
        color = Color.Gray, // Goggle rim
        radius = eyeRadius,
        center = Offset(centerX, baseY - bodyHeight * 0.2f)
    )
    drawCircle(
        color = Color.White,
        radius = eyeRadius * 0.8f,
        center = Offset(centerX, baseY - bodyHeight * 0.2f)
    )
    drawCircle(
        color = Color.Black, // Pupil
        radius = eyeRadius * 0.3f,
        center = Offset(centerX, baseY - bodyHeight * 0.2f)
    )

    // Arms
    val armAngle = sin(runCycle.toDouble()).toFloat() * 0.8f
    val armLength = width * 0.2f
    
    drawLine(
        color = color,
        start = Offset(centerX - bodyWidth/2, baseY),
        end = Offset(centerX - bodyWidth/2 - armLength, baseY + sin(armAngle.toDouble()).toFloat()*armLength),
        strokeWidth = width * 0.06f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = color,
        start = Offset(centerX + bodyWidth/2, baseY),
        end = Offset(centerX + bodyWidth/2 + armLength, baseY - sin(armAngle.toDouble()).toFloat()*armLength),
        strokeWidth = width * 0.06f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawRobot(
    color: Color, centerX: Float, baseY: Float, width: Float, height: Float, runCycle: Float
) {
    // Tech/Robot style - Geometric
    val headSize = width * 0.25f
    
    // Head (Square)
    drawRect(
        color = color,
        topLeft = Offset(centerX - headSize/2, baseY - headSize * 1.5f),
        size = Size(headSize, headSize)
    )
    
    // Antenna
    drawLine(
        color = color,
        start = Offset(centerX, baseY - headSize * 1.5f),
        end = Offset(centerX, baseY - headSize * 2.0f),
        strokeWidth = 4f
    )
    drawCircle(
        color = Color.Red,
        radius = 4f,
        center = Offset(centerX, baseY - headSize * 2.0f)
    )
    
    // Body (Trapezoid-like, simplified to Rect)
    drawRect(
        color = color.copy(alpha = 0.8f),
        topLeft = Offset(centerX - headSize * 0.6f, baseY - headSize * 0.4f),
        size = Size(headSize * 1.2f, headSize * 1.5f)
    )
    
    // Limbs (Lines with joints)
    val limbLength = height * 0.18f
    
    // Legs
    val leftLegAngle = sin(runCycle.toDouble()) * 0.5f
    drawLine(
        color = color,
        start = Offset(centerX - headSize * 0.3f, baseY + headSize * 1.1f),
        end = Offset(centerX - headSize * 0.3f + sin(leftLegAngle).toFloat()*limbLength, baseY + headSize * 1.1f + cos(leftLegAngle).toFloat()*limbLength),
        strokeWidth = 6f,
        cap = StrokeCap.Square
    )
    
    val rightLegAngle = sin(runCycle.toDouble() + Math.PI).toFloat() * 0.5f
    drawLine(
        color = color,
        start = Offset(centerX + headSize * 0.3f, baseY + headSize * 1.1f),
        end = Offset(centerX + headSize * 0.3f + sin(rightLegAngle.toDouble()).toFloat()*limbLength, baseY + headSize * 1.1f + cos(rightLegAngle.toDouble()).toFloat()*limbLength),
        strokeWidth = 6f,
        cap = StrokeCap.Square
    )
}

private fun DrawScope.drawDefaultRunner(
    color: Color, centerX: Float, baseY: Float, width: Float, height: Float, runCycle: Float
) {
    // Q-style proportions
    val headRadius = width * 0.25f
    val bodyLength = height * 0.25f
    val limbLength = height * 0.18f
    val strokeWidth = width * 0.08f
    
    // Draw Body (Small and cute)
    drawLine(
        color = color,
        start = Offset(centerX, baseY),
        end = Offset(centerX, baseY + bodyLength),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )

    // Draw Head (Big and round)
    drawCircle(
        color = color,
        radius = headRadius,
        center = Offset(centerX, baseY - headRadius * 0.8f)
    )

    // Draw Legs
    // Left Leg
    val leftLegAngle = sin(runCycle.toDouble()) * 0.8f
    val leftKneeX = centerX + sin(leftLegAngle).toFloat() * limbLength
    val leftKneeY = baseY + bodyLength + cos(leftLegAngle).toFloat() * limbLength
    
    drawLine(
        color = color,
        start = Offset(centerX, baseY + bodyLength),
        end = Offset(leftKneeX, leftKneeY),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    
    // Lower Left Leg (Simple bending)
    val leftLowerLegAngle = leftLegAngle + (if (sin(runCycle.toDouble()) > 0) 1.0f else 0.2f)
    drawLine(
        color = color,
        start = Offset(leftKneeX, leftKneeY),
        end = Offset(
            leftKneeX - sin(leftLowerLegAngle).toFloat() * limbLength,
            leftKneeY + cos(leftLowerLegAngle).toFloat() * limbLength
        ),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )

    // Right Leg
    val rightLegAngle = sin(runCycle.toDouble() + Math.PI).toFloat() * 0.8f
    val rightKneeX = centerX + sin(rightLegAngle.toDouble()).toFloat() * limbLength
    val rightKneeY = baseY + bodyLength + cos(rightLegAngle.toDouble()).toFloat() * limbLength
    
    drawLine(
        color = color,
        start = Offset(centerX, baseY + bodyLength),
        end = Offset(rightKneeX, rightKneeY),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )

    // Lower Right Leg
    val rightLowerLegAngle = rightLegAngle + (if (sin(runCycle.toDouble() + Math.PI) > 0) 1.0f else 0.2f)
    drawLine(
        color = color,
        start = Offset(rightKneeX, rightKneeY),
        end = Offset(
            rightKneeX - sin(rightLowerLegAngle.toDouble()).toFloat() * limbLength,
            rightKneeY + cos(rightLowerLegAngle.toDouble()).toFloat() * limbLength
        ),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )

    // Draw Arms
    // Left Arm
    val leftArmAngle = sin(runCycle.toDouble() + Math.PI).toFloat() * 0.8f
    val leftElbowX = centerX + sin(leftArmAngle.toDouble()).toFloat() * limbLength
    val leftElbowY = baseY + bodyLength * 0.2f + cos(leftArmAngle.toDouble()).toFloat() * limbLength
    
    drawLine(
        color = color,
        start = Offset(centerX, baseY + bodyLength * 0.2f),
        end = Offset(leftElbowX, leftElbowY),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    
     // Right Arm
    val rightArmAngle = sin(runCycle.toDouble()).toFloat() * 0.8f
    val rightElbowX = centerX + sin(rightArmAngle.toDouble()).toFloat() * limbLength
    val rightElbowY = baseY + bodyLength * 0.2f + cos(rightArmAngle.toDouble()).toFloat() * limbLength
    
    drawLine(
        color = color,
        start = Offset(centerX, baseY + bodyLength * 0.2f),
        end = Offset(rightElbowX, rightElbowY),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    
    // Sweat drop (occasionally)
    if (sin(runCycle.toDouble() * 0.5f) > 0.8f) {
         drawCircle(
            color = Color(0xFF4FC3F7),
            radius = strokeWidth * 0.6f,
            center = Offset(centerX + headRadius * 1.2f, baseY - headRadius * 0.5f)
        )
    }
}

