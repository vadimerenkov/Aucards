package vadimerenkov.aucards

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.getApplicationLocales
import androidx.appcompat.app.AppCompatDelegate.setApplicationLocales
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import vadimerenkov.aucards.settings.Theme
import vadimerenkov.aucards.ui.theme.AucardsTheme

class MainActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContent {
			val app = this.application as AucardsApplication
			val theme_string by app.settings.themeSetting.collectAsState("why are we here")
			val isDarkTheme = when (theme_string) {
				Theme.LIGHT.name -> false
				Theme.DARK.name -> true
				else -> isSystemInDarkTheme()
			}

			val locales = getApplicationLocales()
			setApplicationLocales(locales)

			if (BuildConfig.DEBUG) {
				SetInitialState(
					scope = lifecycleScope,
					dao = app.database.aucardDao()
				)
			}

			AucardsTheme(
				darkTheme = isDarkTheme
			) {
				AucardsApp(isDarkTheme)
			}
		}
	}
}
