package com.file.material.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val ExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

// Dedicated Expressive Tokens
val CardShapeExpressive = RoundedCornerShape(24.dp)
val ChipShapeExpressive = RoundedCornerShape(20.dp)
val ButtonShapeExpressive = RoundedCornerShape(20.dp)
val DialogShapeExpressive = RoundedCornerShape(28.dp)
val BottomSheetShapeExpressive = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
val FloatingBarShapeExpressive = CircleShape
val IconContainerShapeExpressive = RoundedCornerShape(16.dp)
