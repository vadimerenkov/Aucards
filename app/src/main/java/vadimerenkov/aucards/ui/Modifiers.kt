package vadimerenkov.aucards.ui

import androidx.compose.ui.Modifier

fun Modifier.applyIf(
	condition: Boolean,
	modifier: Modifier.() -> Modifier
): Modifier {
	return if (condition) {
		this.then(modifier())
	} else this
}