package vadimerenkov.aucards

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import vadimerenkov.aucards.ui.CardViewModel
import vadimerenkov.aucards.ui.ListViewModel
import vadimerenkov.aucards.ui.SettingsViewModel

object ViewModelFactory {
	fun Factory(isDarkTheme: Boolean = false) = viewModelFactory {
		initializer {
			ListViewModel(app().database.aucardDao(), DefaultDispatchers())
		}
		initializer {
			CardViewModel(
				this.createSavedStateHandle(), app().settings, app().database.aucardDao(),
				dispatchers = DefaultDispatchers(),
				isDarkTheme = isDarkTheme
			)
		}
		initializer {
			SettingsViewModel(app().settings, app().database, app().applicationContext)
		}
	}
}

fun CreationExtras.app(): AucardsApplication {
	return this[APPLICATION_KEY] as AucardsApplication
}