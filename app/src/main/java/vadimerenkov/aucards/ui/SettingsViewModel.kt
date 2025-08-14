package vadimerenkov.aucards.ui

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate.getApplicationLocales
import androidx.appcompat.app.AppCompatDelegate.setApplicationLocales
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.intl.Locale
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
import vadimerenkov.aucards.data.Aucard
import vadimerenkov.aucards.data.AucardsDatabase
import vadimerenkov.aucards.settings.Keys.BRIGHTNESS_STRING
import vadimerenkov.aucards.settings.Keys.LANDSCAPE_STRING
import vadimerenkov.aucards.settings.Keys.THEME_STRING
import vadimerenkov.aucards.settings.Language
import vadimerenkov.aucards.settings.Settings
import vadimerenkov.aucards.settings.Theme
import java.io.File

private const val TAG = "SettingsViewModel"

class SettingsViewModel(
	private val settings: Settings,
	private val database: AucardsDatabase
): ViewModel() {
	private val state = MutableStateFlow(SettingsState())

	private val settings_observer = settings.settingsFlow
		.onStart {
			val isEmpty = database.aucardDao().getAllCards().first().isEmpty()
			state.update { it.copy(isDbEmpty = isEmpty) }
		}
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

	fun exportDatabase(uri: Uri, context: Context) {
		viewModelScope.launch {
			try {
				database.openHelper.setWriteAheadLoggingEnabled(false)

				val currentDb = context.getDatabasePath(AucardsDatabase.NAME)
				val file = context.contentResolver.openOutputStream(uri)
				val currentBytes = currentDb.readBytes()
				file?.write(currentBytes)
				file?.close()

				Log.i(TAG, "Database path is ${database.openHelper.readableDatabase.path}")
				database.openHelper.setWriteAheadLoggingEnabled(true)
			} catch (e: Exception) {
				Log.e(TAG, "Export database error: $e")
			}
		}
	}

	fun importDatabase(uri: Uri, context: Context) {
		viewModelScope.launch {
			try {
				var current_index = database.aucardDao().getAllCards().first().size

				val file = context.contentResolver.openInputStream(uri)
				val temp = File.createTempFile("asdf", "qwer")
				val stream = temp.outputStream()
				file?.copyTo(stream)

				val database = SQLiteDatabase.openDatabase(temp.path, null, SQLiteDatabase.OPEN_READONLY)
				val cursor = database.rawQuery("SELECT * FROM aucard", null)
				cursor.use {
					if (it.moveToFirst()) {
						do {
							val text = it.getColumnIndex("text")
							val color = it.getColumnIndex("color")
							val desc = it.getColumnIndex("description")
							val fav = it.getColumnIndex("isFavourite")

							val text_value = it.getString(text)
							val color_value = it.getInt(color)
							val desc_value = it.getString(desc)
							val fav_value = it.getInt(fav)

							val card = Aucard(
								text = text_value,
								color = Color(color_value),
								description = desc_value,
								isFavourite = fav_value.toBoolean(),
								index = current_index + 1
							)

							this@SettingsViewModel.database.aucardDao().saveAucard(card)
							Log.d(TAG, "We made a new card: $card")
							current_index++

						} while (it.moveToNext())
					}
				}

				stream.close()
				file?.close()
				temp.delete()

			} catch (e: Exception) {
				Log.e(TAG, "Import database error: $e")
			}
		}
	}
}

data class SettingsState(
	val theme: Theme = Theme.DEVICE,
	val isMaxBrightness: Boolean = false,
	val isLandscapeMode: Boolean = false,
	val language: Language = Language.ENGLISH,
	val isDbEmpty: Boolean = true
)

class InvalidSettingsException(setting: Any): Exception() {
	override val message: String = "Tried to save invalid settings: $setting"
}

fun Int.toBoolean(): Boolean {
	return this != 0
}