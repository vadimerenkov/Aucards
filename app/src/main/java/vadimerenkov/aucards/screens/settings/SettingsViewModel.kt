package vadimerenkov.aucards.screens.settings

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate.getApplicationLocales
import androidx.appcompat.app.AppCompatDelegate.setApplicationLocales
import androidx.compose.ui.text.intl.Locale
import androidx.core.net.toUri
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vadimerenkov.aucards.data.AucardsDatabase

private const val TAG = "SettingsViewModel"

class SettingsViewModel(
	private val settings: Settings,
	private val database: AucardsDatabase,
	private val sqLiteImporter: SQLiteImporter
): ViewModel() {
	private val state = MutableStateFlow(SettingsState())

	init {
		settings.settingsFlow
			.onStart {
				val isEmpty = database.aucardDao().getAllCards().first().isEmpty()
				state.update { it.copy(isDbEmpty = isEmpty) }
			}
			.onEach {
				val landscape = settings.landscape.first() ?: false
				val brightness = settings.brightness.first() ?: false
				val playSound = settings.playSound.first() ?: false
				val ringtoneUri = settings.soundUri.first()?.toUri()
				val materialYou = settings.materialYou.first() ?: false
				val voice = settings.voice.first() ?: false
				val theme = readThemeSetting()
				val language = readLanguageSetting()
				state.update { it.copy(
					theme = theme,
					isMaxBrightness = brightness,
					isLandscapeMode = landscape,
					language = language,
					playSound = playSound,
					ringtoneUri = ringtoneUri,
					materialYou = materialYou,
					voiceCard = voice
				) }
			}
			.launchIn(viewModelScope)
	}

	val settingsState = state
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5000L),
			initialValue = state.value
		)

	private fun readLanguageSetting(): Language {
		val locales = getApplicationLocales()
			.toLanguageTags()

		Log.i(TAG, "Loaded $locales as saved locale.")

		val lang = Language.entries.find { it.code == locales }

		if (lang != null) {
			return lang
		}

		Log.w(TAG, "Could not load language settings.")
		val current_locale = Locale.current
		val default_language = current_locale.toLanguageTag()

		Log.i(TAG, "Current locale is $current_locale, defaulting to $default_language.")

		Language.entries.forEach { it ->
			if (it.code.contains(default_language.take(2))) {
				return it
			}
		}

		Log.e(TAG, "$default_language is not supported. Defaulting to English.")

		return Language.ENGLISH
	}

	private suspend fun readThemeSetting(): Theme {
		val theme = settings.themeSetting.first()

		Theme.entries.forEach { it ->
			if (theme == it.name) {
				return it
			}
		}

		Log.w(TAG, "Could not load theme settings; defaulting to Device.")
		return Theme.DEVICE
	}

	fun saveThemeSetting(themeValue: Int) {

		val theme = Theme.entries.find { it.ordinal == themeValue } ?: Theme.DEVICE

		viewModelScope.launch {
			settings.saveStringSettings(
				key = THEME_STRING,
				value = theme.name
			)
		}

	}

	fun saveLanguageSetting(lang_string: String, context: Context) {

		val language = Language.entries.find { lang ->
			context.getString(lang.uiText) == lang_string
		} ?: Language.ENGLISH

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

	fun saveMaterialSetting(isDynamic: Boolean) {
		viewModelScope.launch {
			settings.saveBoolSettings(
				key = MATERIAL_STRING,
				value = isDynamic
			)
		}
	}

	fun saveSoundSetting(playSound: Boolean) {
		viewModelScope.launch {
			settings.saveBoolSettings(
				key = SOUND_STRING,
				value = playSound
			)
		}
	}

	fun saveSoundUri(uri: Uri) {
		viewModelScope.launch {
			settings.saveStringSettings(
				key = RINGTONE_URI,
				value = uri.toString()
			)
		}
	}

	fun saveVoiceSetting(voice: Boolean) {
		viewModelScope.launch {
			settings.saveBoolSettings(
				key = VOICE_CARD,
				value = voice
			)
		}
	}

	fun hasPermission(context: Context): Boolean {
		return android.provider.Settings.System.canWrite(context)
	}

	fun exportDatabase(uri: Uri) {
		viewModelScope.launch {
			sqLiteImporter.exportDatabase(uri)
		}
	}

	fun importDatabase(uri: Uri) {
		viewModelScope.launch {
			sqLiteImporter.importDatabase(uri)
		}
	}
}