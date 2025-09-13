package vadimerenkov.aucards

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import vadimerenkov.aucards.data.AucardsDatabase
import vadimerenkov.aucards.screens.settings.Settings

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("aucards_settings")

class AucardsApplication: Application() {
	lateinit var database: AucardsDatabase
	lateinit var settings: Settings

	override fun onCreate() {
		super.onCreate()
		database = AucardsDatabase.getDatabase(this)
		settings = Settings(dataStore)
	}
}