package vadimerenkov.aucards.converters

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.room.TypeConverter

class Converters {

	@TypeConverter
	fun colorToInt(color: Color): Int {
		return color.toArgb()
	}

	@TypeConverter
	fun intToColor(int: Int): Color {
		return Color(int)
	}
}