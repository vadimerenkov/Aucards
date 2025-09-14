package vadimerenkov.aucards.screens.settings

import androidx.annotation.StringRes
import vadimerenkov.aucards.R

enum class Language(
	@StringRes val uiText: Int,
	val code: String,
	@StringRes val translator: Int? = R.string.translator
) {
	ENGLISH(R.string.english, "en", null),
	RUSSIAN(R.string.russian, "ru", null),
	UKRAINIAN(R.string.ukrainian, "uk"),
	GEORGIAN(R.string.georgian, "ka"),
	POLISH(R.string.polish, "pl")
}