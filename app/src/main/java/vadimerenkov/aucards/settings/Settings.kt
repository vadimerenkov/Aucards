package vadimerenkov.aucards.settings

import android.net.Uri
import androidx.annotation.StringRes
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import vadimerenkov.aucards.R
import vadimerenkov.aucards.settings.Keys.BRIGHTNESS_STRING
import vadimerenkov.aucards.settings.Keys.LANDSCAPE_STRING
import vadimerenkov.aucards.settings.Keys.RINGTONE_URI
import vadimerenkov.aucards.settings.Keys.SOUND_STRING
import vadimerenkov.aucards.settings.Keys.THEME_STRING

enum class Language(
	@StringRes val uiText: Int,
	val code: String,
	@StringRes val translator: Int? = R.string.translator
) {
	ENGLISH(R.string.english, "en", null),
	RUSSIAN(R.string.russian, "ru", null),
	UKRAINIAN(R.string.ukrainian, "uk"),
	GEORGIAN(R.string.georgian, "ka")
}

enum class Theme(
	@StringRes val uiText: Int
) {
	LIGHT(R.string.light),
	DARK(R.string.dark),
	DEVICE(R.string.device)
}

class Settings(
	private val dataStore: DataStore<Preferences>
) {
	val settingsFlow = dataStore.data
	val themeSetting = dataStore.data
		.map { settings ->
			settings[stringPreferencesKey(THEME_STRING)]
		}
	val brightness = dataStore.data
		.map { settings ->
			settings[booleanPreferencesKey(BRIGHTNESS_STRING)]
		}
	val landscape = dataStore.data
		.map { settings ->
			settings[booleanPreferencesKey(LANDSCAPE_STRING)]
		}
	val playSound = dataStore.data
		.map { settings ->
			settings[booleanPreferencesKey(SOUND_STRING)]
		}
	private val soundUri = dataStore.data
		.map { settings ->
			settings[stringPreferencesKey(RINGTONE_URI)]
		}

	suspend fun saveStringSettings(key: String, value: String) {
		val key = stringPreferencesKey(key)
		dataStore.edit { settings ->
			settings[key] = value
		}
	}

	suspend fun saveBoolSettings(key: String, value: Boolean) {
		val key = booleanPreferencesKey(key)
		dataStore.edit { settings ->
			settings[key] = value
		}
	}

	suspend fun readSoundUri(): Uri? {
		val setting = soundUri.first()
		return setting?.toUri()
	}
}