package vadimerenkov.aucards

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import vadimerenkov.aucards.screens.settings.SettingsScreen

class SettingsTest {
	@get:Rule
	val composeTestRule = createAndroidComposeRule<ComponentActivity>()

	@Before
	fun SetupSettingsScreen() {
		composeTestRule.setContent {
			SettingsScreen(
				onBackClicked = {}
			)
		}
	}

	@Test
	fun ChooseDarkTheme() {
		val txt_light = composeTestRule.activity.getString(R.string.light)
		val txt_dark = composeTestRule.activity.getString(R.string.dark)
		composeTestRule.onNodeWithText(txt_light)
			.performClick()
		composeTestRule.onNodeWithText(txt_dark)
			.performClick()
		// assert dark theme
	}

	@Test
	fun ChooseRussianLanguage() {
		val txt_eng = composeTestRule.activity.getString(R.string.english)
		val txt_ru = composeTestRule.activity.getString(R.string.russian)
		composeTestRule.onNodeWithText(txt_eng)
			.performClick()
		composeTestRule.onNodeWithText(txt_ru)
			.performClick()
		// assert russian language
	}
}