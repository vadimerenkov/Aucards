package vadimerenkov.aucards.converters

import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.net.toUri
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

	@TypeConverter
	fun stringToUri(string: String): Uri {
		return string.toUri()
	}

	@TypeConverter
	fun uriToString(uri: Uri): String {
		return uri.toString()
	}

	@TypeConverter
	fun longToOffset(long: Long): Offset {
		return Offset(long)
	}

	@TypeConverter
	fun offsetToLong(offset: Offset): Long {
		return offset.packedValue
	}
}