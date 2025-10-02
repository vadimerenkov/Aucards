package vadimerenkov.aucards.screens.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.map

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
	val soundUri = dataStore.data
		.map { settings ->
			settings[stringPreferencesKey(RINGTONE_URI)]
		}
	val materialYou = dataStore.data
		.map { settings ->
			settings[booleanPreferencesKey(MATERIAL_STRING)]
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
}