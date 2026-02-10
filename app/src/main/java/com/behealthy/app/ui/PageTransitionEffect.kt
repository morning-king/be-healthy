package com.behealthy.app.ui

import androidx.compose.foundation.pager.PagerState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp
import kotlin.math.absoluteValue

enum class PageTransitionEffect(val label: String) {
    Default("默认"),
    Fade("淡入淡出"),
    Zoom("缩放"),
    Depth("深度"),
    Rotate("旋转")
}

fun Modifier.applyPageTransition(
    pagerState: PagerState,
    page: Int,
    effect: PageTransitionEffect
): Modifier = graphicsLayer {
    val pageOffset = (
        (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
    )

    when (effect) {
        PageTransitionEffect.Default -> {
            // No effect
        }
        PageTransitionEffect.Fade -> {
            alpha = lerp(
                start = 0.5f,
                stop = 1f,
                fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
            )
        }
        PageTransitionEffect.Zoom -> {
            val scale = lerp(
                start = 0.85f,
                stop = 1f,
                fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
            )
            scaleX = scale
            scaleY = scale
            alpha = lerp(
                start = 0.5f,
                stop = 1f,
                fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
            )
        }
        PageTransitionEffect.Depth -> {
             if (pageOffset.absoluteValue <= 1) {
                 val scale = lerp(0.85f, 1f, 1f - pageOffset.absoluteValue)
                 scaleX = scale
                 scaleY = scale
                 alpha = lerp(0.5f, 1f, 1f - pageOffset.absoluteValue)
             } else {
                 alpha = 0f
             }
        }
        PageTransitionEffect.Rotate -> {
            val rotation = lerp(
                start = 90f,
                stop = 0f,
                fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
            )
            rotationY = rotation * if (pageOffset > 0) 1f else -1f
            alpha = lerp(
                start = 0.5f,
                stop = 1f,
                fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
            )
            cameraDistance = 8 * density
        }
    }
}
