package vadimerenkov.aucards

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.media3.exoplayer.ExoPlayer
import vadimerenkov.aucards.screens.fullscreencard.CardViewModel
import vadimerenkov.aucards.screens.list.ListViewModel
import vadimerenkov.aucards.screens.settings.SettingsViewModel

object ViewModelFactory {
	fun Factory(
		isDarkTheme: Boolean = false,
		id: Int = 0,
		index: Int? = null,
		initialPage: Int = 0
	) = viewModelFactory {
		initializer {
			ListViewModel(
				aucardDao = app().database.aucardDao(),
				dispatchers = DefaultDispatchers(),
				initialPage = initialPage
			)
		}
		initializer {
			CardViewModel(
				settings = app().settings,
				aucardDao = app().database.aucardDao(),
				dispatchers = DefaultDispatchers(),
				isDarkTheme = isDarkTheme,
				id = id,
				index = index,
				soundPlayer = ExoPlayer.Builder(app().applicationContext).build()
			)
		}
		initializer {
			SettingsViewModel(app().settings, app().database)
		}
	}
}

fun CreationExtras.app(): AucardsApplication {
	return this[APPLICATION_KEY] as AucardsApplication
}