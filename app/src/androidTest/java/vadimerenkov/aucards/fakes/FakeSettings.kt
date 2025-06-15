package vadimerenkov.aucards.fakes

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import vadimerenkov.aucards.settings.Settings

fun getSettings(): Settings {
	val context: Context = ApplicationProvider.getApplicationContext()
	val dataStore = PreferenceDataStoreFactory.create(
		produceFile = { context.preferencesDataStoreFile("TEST_DATASTORE") }
	)
	return Settings(dataStore)
}