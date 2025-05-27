package com.example.bubbledo.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SplashEffect(
    center: Offset,
    radius: Float,
    onAnimationEnd: () -> Unit
) {
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 400, easing = LinearOutSlowInEasing)
        )
        onAnimationEnd()
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val animatedRadiusPx = (radius * 2f * animationProgress.value).toDp().toPx()
        val centerPx = Offset(center.x.dp.toPx(), center.y.dp.toPx())
        val strokeWidthPx = (radius / 6f).toDp().toPx() * (1f - animationProgress.value)

        repeat(12) { i ->
            val angle = 2 * Math.PI / 12 * i
            val dx = (cos(angle) * animatedRadiusPx).toFloat()
            val dy = (sin(angle) * animatedRadiusPx).toFloat()

            drawLine(
                color = Color.White.copy(alpha = 1f - animationProgress.value),
                start = centerPx,
                end = centerPx + Offset(dx, dy),
                strokeWidth = strokeWidthPx
            )
        }
    }
}
