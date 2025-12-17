package vadimerenkov.aucards

/*
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

 */