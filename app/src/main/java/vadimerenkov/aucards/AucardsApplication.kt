package vadimerenkov.aucards

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import vadimerenkov.aucards.di.appModule

val Context.dataStore: DataStore<Preferences> by preferencesDataStore("aucards_settings")

class AucardsApplication: Application() {

	override fun onCreate() {
		super.onCreate()

		startKoin {
			androidContext(this@AucardsApplication)
			modules(appModule)
		}
	}
}