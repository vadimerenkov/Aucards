package vadimerenkov.aucards.settings

import androidx.annotation.StringRes
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
import vadimerenkov.aucards.settings.Keys.THEME_STRING

enum class Language(
	@StringRes val uiText: Int,
	val code: String,
	@StringRes val translator: Int?
) {
	ENGLISH(R.string.english, "en", null),
	RUSSIAN(R.string.russian, "ru", null),
	UKRAINIAN(R.string.ukrainian, "uk", R.string.translator)
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

	/*
	fun hasPermission(): Boolean {
		return Settings.System.canWrite(context)
	}
	 */


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

	suspend fun saveEnumSettings(key: String, value: String) {
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

	suspend fun readBoolSettings(key: String): Boolean? {
		val key = booleanPreferencesKey(key)
		return dataStore.data.first()[key]
	}

	suspend fun readEnumSettings(key: String): String? {
		val key = stringPreferencesKey(key)
		return dataStore.data.first()[key]
	}
}