package vadimerenkov.aucards

import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.navigation.toRoute
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NavigationTest {

	@get:Rule
	val composeTestRule = createAndroidComposeRule<ComponentActivity>()

	private lateinit var navController: TestNavHostController

	@Before
	fun SetupNavHost() {
		composeTestRule.setContent {
			navController = TestNavHostController(LocalContext.current).apply {
				navigatorProvider.addNavigator(ComposeNavigator())
			}
			MainNavHost(navController = navController)
		}
	}

	@Test
	fun AssertStartDestination() {
		val state = navController.currentBackStackEntry?.toRoute<ListScreen>()
		assertEquals(state, ListScreen)
	}

	@Test
	fun AssertInitialStateString() {
		val txt = composeTestRule.activity.getString(R.string.empty_list_prompt)
		composeTestRule.onNodeWithText(txt).assertIsDisplayed()
	}

	@Test
	fun NavigateToCardEditOnAddClick() {
		val txt = composeTestRule.activity.getString(R.string.add_card)
		composeTestRule.onNodeWithContentDescription(txt)
			.performClick()
		val state = navController.currentBackStackEntry?.toRoute<FullscreenCard>()
		assertEquals(state, FullscreenCard(0, true))
	}

	@Test
	fun NavigateToSettings() {
		val txt = composeTestRule.activity.getString(R.string.open_settings)
		composeTestRule.onNodeWithContentDescription(txt)
			.performClick()
		val state = navController.currentBackStackEntry?.toRoute<SettingsScreen>()
		assertEquals(state, SettingsScreen)
	}

	@Test
	fun OpenFullScreenCard() {
		// click on the card, assert we are on fullscreen AND it's the same card
	}
}