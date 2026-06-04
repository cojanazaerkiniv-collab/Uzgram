package com.uzgram.messenger.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val UzGramShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

// Custom shapes for message bubbles
val MessageBubbleShapeSent = RoundedCornerShape(
    topStart = 18.dp, topEnd = 18.dp,
    bottomStart = 18.dp, bottomEnd = 4.dp
)
val MessageBubbleShapeReceived = RoundedCornerShape(
    topStart = 4.dp, topEnd = 18.dp,
    bottomStart = 18.dp, bottomEnd = 18.dp
)
val MessageBubbleShapeGrouped = RoundedCornerShape(18.dp)
val BottomNavShape = RoundedCornerShape(24.dp)
