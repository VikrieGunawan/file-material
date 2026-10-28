package com.file.material.ui.components

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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Authentic Liquid Glass (Glassmorphism) Modifier.
 *
 * Implements real physical glass characteristics while preserving 100% crystal-sharp
 * readability of foreground text and icons:
 * 1. Translucent refractive surface fill that adapts to light/dark themes.
 * 2. Dual-angle 135° specular rim border simulating physical glass edge refraction.
 * 3. Inner gloss reflection sheen (top-down specular highlight gradient drawn behind content).
 * 4. Elevated diffusion shadow providing physical depth above scrolling content.
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(24.dp),
    elevation: Dp = 6.dp,
    tintColor: Color? = null,
    borderWidth: Dp = 1.2.dp,
    highlightAlpha: Float = 0.50f,
    sheenAlpha: Float = 0.16f,
    isDark: Boolean = false
): Modifier {
    val baseSurfaceColor = tintColor ?: if (isDark) {
        Color(0x991C222B) // Translucent deep obsidian glass (~60% alpha)
    } else {
        Color(0xCCFFFFFF) // Translucent frosted pure glass (~80% alpha)
    }

    val rimHighlightTopLeft = if (isDark) {
        Color.White.copy(alpha = (highlightAlpha * 0.70f).coerceIn(0f, 1f))
    } else {
        Color.White.copy(alpha = highlightAlpha.coerceIn(0f, 1f))
    }

    val rimHighlightMid = if (isDark) {
        Color.White.copy(alpha = 0.14f)
    } else {
        Color.White.copy(alpha = 0.28f)
    }

    val rimHighlightBottomRight = if (isDark) {
        Color.White.copy(alpha = 0.06f)
    } else {
        Color.White.copy(alpha = 0.12f)
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
        0.35f to Color.White.copy(alpha = (sheenAlpha * 0.25f).coerceIn(0f, 1f)),
        1.0f to Color.Transparent
    )

    val shadowSpot = Color.Black.copy(alpha = if (isDark) 0.35f else 0.10f)
    val shadowAmbient = Color.Black.copy(alpha = if (isDark) 0.20f else 0.06f)

    return this
        .shadow(
            elevation = elevation,
            shape = shape,
            spotColor = shadowSpot,
            ambientColor = shadowAmbient
        )
        .clip(shape)
        .background(color = baseSurfaceColor, shape = shape)
        .drawBehind {
            // Draw specular gloss reflection behind the foreground content so text/icons stay 100% sharp
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
 * All child content (text, icons, badges) remains pin-sharp and crystal clear.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    elevation: Dp = 6.dp,
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
 * Preserves crisp, unblurred rendering of child content.
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    elevation: Dp = 12.dp,
    tintColor: Color? = null,
    borderWidth: Dp = 1.2.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()

    Box(
        modifier = modifier
            .liquidGlass(
                shape = shape,
                elevation = elevation,
                tintColor = tintColor,
                borderWidth = borderWidth,
                isDark = isDark
            )
            .clip(shape),
        content = content
    )
}
