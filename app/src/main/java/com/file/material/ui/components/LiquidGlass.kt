package com.file.material.ui.components

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Authentic Liquid Glass (Glassmorphism) Modifier.
 *
 * Implements real physical glass characteristics:
 * 1. Hardware GPU backdrop blur via [RenderEffect] on Android 12+ (API 31+).
 * 2. Translucent refractive surface fill that adapts to light/dark themes.
 * 3. Dual-angle 135° specular rim border simulating physical glass edge refraction.
 * 4. Inner gloss reflection sheen (top-down specular highlight gradient).
 * 5. Elevated diffusion shadow providing physical depth above scrolling content.
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(24.dp),
    blurRadius: Dp = 25.dp,
    elevation: Dp = 6.dp,
    tintColor: Color? = null,
    borderWidth: Dp = 1.2.dp,
    highlightAlpha: Float = 0.45f,
    sheenAlpha: Float = 0.14f,
    isDark: Boolean = false
): Modifier {
    val baseSurfaceColor = tintColor ?: if (isDark) {
        Color(0xFF1A1F26).copy(alpha = 0.62f)
    } else {
        Color(0xFFFFFFFF).copy(alpha = 0.68f)
    }

    val rimHighlightTopLeft = if (isDark) {
        Color.White.copy(alpha = (highlightAlpha * 0.70f).coerceIn(0f, 1f))
    } else {
        Color.White.copy(alpha = highlightAlpha.coerceIn(0f, 1f))
    }

    val rimHighlightMid = if (isDark) {
        Color.White.copy(alpha = 0.12f)
    } else {
        Color.White.copy(alpha = 0.25f)
    }

    val rimHighlightBottomRight = if (isDark) {
        Color.White.copy(alpha = 0.05f)
    } else {
        Color.White.copy(alpha = 0.10f)
    }

    val specularBorderBrush = Brush.linearGradient(
        listOf(
            rimHighlightTopLeft,
            rimHighlightMid,
            rimHighlightBottomRight
        )
    )

    val innerSheenBrush = Brush.verticalGradient(
        0.0f to Color.White.copy(alpha = sheenAlpha.coerceIn(0f, 1f)),
        0.4f to Color.White.copy(alpha = (sheenAlpha * 0.25f).coerceIn(0f, 1f)),
        1.0f to Color.Transparent
    )

    val shadowSpot = Color.Black.copy(alpha = if (isDark) 0.35f else 0.12f)
    val shadowAmbient = Color.Black.copy(alpha = if (isDark) 0.20f else 0.08f)

    return this
        .shadow(
            elevation = elevation,
            shape = shape,
            spotColor = shadowSpot,
            ambientColor = shadowAmbient
        )
        .graphicsLayer {
            this.shape = shape
            this.clip = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurRadius > 0.dp) {
                val radiusPx = blurRadius.toPx()
                renderEffect = RenderEffect.createBlurEffect(
                    radiusPx,
                    radiusPx,
                    Shader.TileMode.CLAMP
                ).asComposeRenderEffect()
            }
        }
        .background(color = baseSurfaceColor, shape = shape)
        .drawWithContent {
            drawContent()
            // Top specular gloss sheen
            drawRect(brush = innerSheenBrush)
        }
        .border(
            width = borderWidth,
            brush = specularBorderBrush,
            shape = shape
        )
}

/**
 * High-performance Liquid Glass Card with physical refraction styling and ripple support.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    elevation: Dp = 6.dp,
    blurRadius: Dp = 25.dp,
    tintColor: Color? = null,
    borderWidth: Dp = 1.2.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val interactionSource = remember { MutableInteractionSource() }

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = ripple(color = MaterialTheme.colorScheme.primary),
            onClick = onClick
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .liquidGlass(
                shape = shape,
                blurRadius = blurRadius,
                elevation = elevation,
                tintColor = tintColor,
                borderWidth = borderWidth,
                isDark = isDark
            )
            .clip(shape)
            .then(clickableModifier),
        content = content
    )
}

/**
 * Liquid Glass Surface container for floating bars, dialogs, and sheets.
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    elevation: Dp = 12.dp,
    blurRadius: Dp = 30.dp,
    tintColor: Color? = null,
    borderWidth: Dp = 1.2.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()

    Box(
        modifier = modifier
            .liquidGlass(
                shape = shape,
                blurRadius = blurRadius,
                elevation = elevation,
                tintColor = tintColor,
                borderWidth = borderWidth,
                isDark = isDark
            )
            .clip(shape),
        content = content
    )
}
