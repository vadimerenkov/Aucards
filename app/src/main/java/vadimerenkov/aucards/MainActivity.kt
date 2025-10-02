package vadimerenkov.aucards

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.getApplicationLocales
import androidx.appcompat.app.AppCompatDelegate.setApplicationLocales
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import vadimerenkov.aucards.screens.settings.Theme
import vadimerenkov.aucards.ui.theme.AucardsTheme

class MainActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val locales = getApplicationLocales()
		setApplicationLocales(locales)

		enableEdgeToEdge()
		setContent {
			val app = this.application as AucardsApplication
			val theme_string by app.settings.themeSetting.collectAsStateWithLifecycle("")
			val materialYou by app.settings.materialYou.collectAsStateWithLifecycle(false)

			val isDarkTheme = when (theme_string) {
				Theme.LIGHT.name -> false
				Theme.DARK.name -> true
				else -> isSystemInDarkTheme()
			}

			val isDynamicTheme = materialYou == true

			if (BuildConfig.DEBUG) {
				SetInitialState(
					scope = lifecycleScope,
					dao = app.database.aucardDao()
				)
			}

			AucardsTheme(
				darkTheme = isDarkTheme,
				dynamicColor = isDynamicTheme
			) {
				MainNavHost(isDarkTheme)
			}
		}
	}
}
