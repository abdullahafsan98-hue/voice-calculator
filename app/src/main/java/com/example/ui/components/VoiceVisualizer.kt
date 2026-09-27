package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun VoiceWaveVisualizer(
    isListening: Boolean,
    rmsDb: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_anim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    // Smoothly interpolate rmsDb
    val animatedRms = remember { Animatable(0f) }
    LaunchedEffect(rmsDb, isListening) {
        val target = if (isListening) (rmsDb.coerceIn(0f, 10f) / 10f) else 0f
        animatedRms.animateTo(
            targetValue = target,
            animationSpec = tween(120, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerY = height / 2f

            if (!isListening && animatedRms.value < 0.05f) {
                // Subtle resting line
                drawLine(
                    color = Color(0x3338BDF8),
                    start = Offset(0f, centerY),
                    end = Offset(width, centerY),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                return@Canvas
            }

            val amplitude = 12.dp.toPx() + (animatedRms.value * 22.dp.toPx())
            val barCount = 36
            val step = width / (barCount + 1)

            val primaryBrush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF38BDF8),
                    Color(0xFF818CF8),
                    Color(0xFFC084FC),
                    Color(0xFF38BDF8)
                )
            )

            for (i in 0..barCount) {
                val x = step * (i + 0.5f)
                val normalizedX = (i.toFloat() / barCount)
                // Windowing function (Hanning) to taper the edges
                val window = (0.5f * (1f - kotlin.math.cos(2 * Math.PI.toFloat() * normalizedX)))
                val waveOffset = sin(normalizedX * 4 * Math.PI.toFloat() + phase)
                val barHeight = (amplitude * window * (0.3f + 0.7f * waveOffset.coerceAtLeast(0.1f))).coerceAtLeast(4.dp.toPx())

                drawLine(
                    brush = primaryBrush,
                    start = Offset(x, centerY - barHeight),
                    end = Offset(x, centerY + barHeight),
                    strokeWidth = 3.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
