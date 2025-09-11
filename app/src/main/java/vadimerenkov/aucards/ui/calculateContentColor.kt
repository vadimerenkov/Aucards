package vadimerenkov.aucards.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

private const val TAG = "calculateContentColor"
/**
 * Calculates the content color based on the darkness of the provided background color.
 * @param color background color for this content
 */

fun calculateContentColor(color: Color): Color = if (color.luminance() >= 0.3f) Color.Black else Color.White