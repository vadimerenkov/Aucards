package vadimerenkov.aucards.ui

import android.util.Log
import androidx.appcompat.app.AppCompatDelegate.getApplicationLocales
import androidx.appcompat.app.AppCompatDelegate.setApplicationLocales
import androidx.compose.ui.text.intl.Locale
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vadimerenkov.aucards.settings.Keys.BRIGHTNESS_STRING
import vadimerenkov.aucards.settings.Keys.LANDSCAPE_STRING
import vadimerenkov.aucards.settings.Keys.THEME_STRING
import vadimerenkov.aucards.settings.Language
import vadimerenkov.aucards.settings.Settings
import vadimerenkov.aucards.settings.Theme

private const val TAG = "SettingsViewModel"

class SettingsViewModel(
	private val settings: Settings
): ViewModel() {
	private val state = MutableStateFlow(SettingsState())

	private val settings_observer = settings.settingsFlow
		.onEach {
			val landscape = settings.landscape.first() ?: false
			val brightness = settings.brightness.first() ?: false
			val theme = readThemeSetting()
			val language = readLanguageSetting()
			state.update { it.copy(theme, brightness, landscape, language) }
		}
		.launchIn(viewModelScope)

	val settingsState = state
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5000L),
			initialValue = state.value
		)

	fun readLanguageSetting(): Language {
		val locales = getApplicationLocales()

		Log.i(TAG, "Loaded $locales as saved locale.")

		Language.entries.forEach { it ->
			if (locales.toString().contains(it.code)) {
				return it
			}
		}

		Log.w(TAG, "Could not load language settings.")
		val current_locale = Locale.current
		val default_language = current_locale.language

		Log.i(TAG, "Current locale is $current_locale, defaulting to $default_language.")

		Language.entries.forEach { it ->
			if (default_language.contains(it.code)) {
				return it
			}
		}

		Log.e(TAG, "$default_language is not supported. Defaulting to English.")

		return Language.ENGLISH
	}

	suspend fun readThemeSetting(): Theme {
		val theme = settings.themeSetting.first()

		Theme.entries.forEach { it ->
			if (theme == it.name) {
				return it
			}
		}

		Log.e(TAG, "Could not load theme settings; defaulting to Device.")
		return Theme.DEVICE
	}

	fun saveThemeSetting(theme_string: Int) {
		var theme: Theme? = null

		Theme.entries.forEach { it ->
			if (it.uiText == theme_string) {
				theme = it
			}
		}

		if (theme == null) {
			throw InvalidSettingsException(theme_string)
		}

		viewModelScope.launch {
			settings.saveEnumSettings(
				key = THEME_STRING,
				value = theme.name
			)
		}

	}

	fun saveLanguageSetting(lang_string: Int) {
		var language: Language? = null

		Language.entries.forEach { it ->
			if (lang_string == it.uiText) {
				language = it
			}
		}

		if (language == null) {
			throw InvalidSettingsException(lang_string)
		}

		val locale = LocaleListCompat.forLanguageTags(language.code)
		Log.d(TAG, "Saved $locale as new language.")

		setApplicationLocales(locale)
		state.update { it.copy(language = language) }
	}

	fun saveLandscapeSetting(landscape: Boolean) {
		viewModelScope.launch {
			settings.saveBoolSettings(LANDSCAPE_STRING, landscape)
		}
	}

	fun saveBrightnessSetting(brightness: Boolean) {
		viewModelScope.launch {
			settings.saveBoolSettings(
				key = BRIGHTNESS_STRING,
				value = brightness
			)
		}
	}
}

data class SettingsState(
	val theme: Theme = Theme.DEVICE,
	val isMaxBrightness: Boolean = false,
	val isLandscapeMode: Boolean = false,
	val language: Language = Language.ENGLISH
)

class InvalidSettingsException(setting: Any): Exception() {
	override val message: String = "Tried to save invalid settings: $setting"
}