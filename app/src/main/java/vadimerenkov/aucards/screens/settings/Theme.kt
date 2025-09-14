package vadimerenkov.aucards.screens.settings

import androidx.annotation.StringRes
import vadimerenkov.aucards.R

enum class Theme(
	@StringRes val uiText: Int
) {
	LIGHT(R.string.light),
	DARK(R.string.dark),
	DEVICE(R.string.device)
}