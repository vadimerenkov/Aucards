package vadimerenkov.aucards.screens.settings

import androidx.annotation.StringRes
import vadimerenkov.aucards.R

enum class Language(
	@StringRes val uiText: Int,
	val code: String,
	@StringRes val translator: Int? = R.string.translator
) {
	ENGLISH(R.string.english, "en-US", null),
	RUSSIAN(R.string.russian, "ru-RU", null),
	UKRAINIAN(R.string.ukrainian, "uk-UA"),
	GEORGIAN(R.string.georgian, "ka-GE"),
	POLISH(R.string.polish, "pl-PL"),
	PORTUGUESE_BRAZIL(R.string.portuguese_brazil, "pt-BR"),
	FRENCH(R.string.french, "fr-FR"),
	SPANISH(R.string.spanish, "es-MX")
}
