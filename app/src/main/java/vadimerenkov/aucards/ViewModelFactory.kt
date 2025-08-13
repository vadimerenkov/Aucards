package vadimerenkov.aucards

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import vadimerenkov.aucards.ui.CardViewModel
import vadimerenkov.aucards.ui.ListViewModel
import vadimerenkov.aucards.ui.SettingsViewModel

object ViewModelFactory {
	fun Factory(
		isDarkTheme: Boolean = false,
		id: Int = 0,
		index: Int? = null
	) = viewModelFactory {
		initializer {
			ListViewModel(app().database.aucardDao(), DefaultDispatchers())
		}
		initializer {
			CardViewModel(
				settings = app().settings,
				aucardDao = app().database.aucardDao(),
				dispatchers = DefaultDispatchers(),
				isDarkTheme = isDarkTheme,
				id = id,
				index = index
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