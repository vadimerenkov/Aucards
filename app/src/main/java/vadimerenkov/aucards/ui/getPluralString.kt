package vadimerenkov.aucards.ui

import android.content.Context
import androidx.annotation.PluralsRes

fun Context.getPluralString(
	@PluralsRes id: Int,
	count: Int,
	vararg arguments: Any
): String {
	return resources.getQuantityString(id, count, *arguments)
}