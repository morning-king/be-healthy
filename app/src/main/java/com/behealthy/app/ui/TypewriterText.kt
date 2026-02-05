package com.behealthy.app.ui

import androidx.compose.animation.core.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current,
    animDurationMillis: Int = 50, // Duration per character
    rainbow: Boolean = true // Enable rainbow effect by default as requested
) {
    var visibleCharCount by remember { mutableStateOf(0) }
    
    LaunchedEffect(text) {
        visibleCharCount = 0
        for (i in 1..text.length) {
            visibleCharCount = i
            delay(animDurationMillis.toLong())
        }
    }

    val finalStyle = if (rainbow) {
        val infiniteTransition = rememberInfiniteTransition(label = "rainbow")
        val offset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2000f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "offset"
        )
        
        val brush = Brush.linearGradient(
            colors = listOf(
                Color.Red, Color(0xFFFF7F00), Color(0xFFDAA520), Color(0xFF228B22), 
                Color(0xFF1E90FF), Color(0xFF4B0082), Color(0xFF8B00FF)
            ),
            start = Offset(offset, 0f),
            end = Offset(offset + 1000f, 1000f),
            tileMode = TileMode.Mirror
        )
        style.copy(brush = brush)
    } else {
        style
    }

    Text(
        text = text.take(visibleCharCount),
        modifier = modifier,
        color = if (rainbow) Color.Unspecified else color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        onTextLayout = onTextLayout,
        style = finalStyle
    )
}
