package vadimerenkov.aucards.ui

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

private const val TAG = "calculateContentColor"
/**
 * Calculates the content color based on the darkness of the provided background color.
 * @param color background color for this content
 */

fun calculateContentColor(color: Color): Color {

	val hsv = FloatArray(3)
	android.graphics.Color.colorToHSV(color.toArgb(), hsv)

	Log.i(TAG, "HSV is ${hsv[0]}, ${hsv[1]}, ${hsv[2]} }")

	val animatedColor =
		if (hsv[0] in (210f..280f)
			&& hsv[1] in (0.75f..1f)
			|| hsv[2] in (0f..0.5f)
			) {
			Color.White
		} else Color.Black

	Log.i(TAG, "Color is $animatedColor")

	return animatedColor
}