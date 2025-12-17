package vadimerenkov.aucards.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import vadimerenkov.aucards.DefaultDispatchers
import vadimerenkov.aucards.DispatchersProvider
import vadimerenkov.aucards.data.AucardsDatabase
import vadimerenkov.aucards.dataStore
import vadimerenkov.aucards.screens.fullscreencard.CardViewModel
import vadimerenkov.aucards.screens.list.ListViewModel
import vadimerenkov.aucards.screens.settings.Settings
import vadimerenkov.aucards.screens.settings.SettingsViewModel

val appModule = module {
	single { AucardsDatabase.getDatabase(androidContext()) }
	single { get<AucardsDatabase>().aucardDao() }
	single { CoroutineScope(Dispatchers.Default + SupervisorJob()) }
	single<DataStore<Preferences>> { androidContext().dataStore }
	single { ExoPlayer.Builder(androidContext()).build() }

	factory { DefaultDispatchers() }.bind<DispatchersProvider>()

	singleOf(::Settings)

	viewModelOf(::ListViewModel)
	viewModelOf(::SettingsViewModel)
	viewModelOf(::CardViewModel)
}