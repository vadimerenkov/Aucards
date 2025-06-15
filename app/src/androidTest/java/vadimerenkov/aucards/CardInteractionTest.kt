@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalTestApi::class)

package vadimerenkov.aucards

import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedDispatcher
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import vadimerenkov.aucards.data.Aucard
import vadimerenkov.aucards.data.AucardDao
import vadimerenkov.aucards.ui.theme.AucardsTheme

@RunWith(AndroidJUnit4::class)
class CardsInteractionTest {

	lateinit var application: AucardsApplication
	lateinit var navController: TestNavHostController
	lateinit var dao: AucardDao

	val card1 = Aucard(id = 1, text = "we are the champions", description = "my friend")
	val card2 = Aucard(id = 2, text = "lorem ipsum", description = "dorime")

	private fun AddOneCard() {
		val txt = composeTestRule.activity.getString(R.string.add_card)
		composeTestRule.onNodeWithContentDescription(txt)
			.performClick()
		composeTestRule.onNodeWithTag("TextField")
			.performTextInput(card1.text)
		//OnBackPressedDispatcher().onBackPressed()
		val save = composeTestRule.activity.getString(R.string.save)
		composeTestRule.onNodeWithContentDescription(save)
			.performClick()
	}

	private suspend fun AddTwoCards() {
		dao.saveAucard(card1)
		dao.saveAucard(card2)
	}

	@get:Rule
	val composeTestRule = createAndroidComposeRule<ComponentActivity>()

	@Before
	fun SetupApp() {

		composeTestRule.setContent {
			navController = TestNavHostController(LocalContext.current).apply {
				navigatorProvider.addNavigator(ComposeNavigator())
			}
			AucardsTheme {
				AucardsApp(navController)
			}
		}
		application = composeTestRule.activity.application as AucardsApplication
		dao = application.database.aucardDao()
	}

	@Test
	fun AssertInitialState() = runTest {
		AddTwoCards()
		composeTestRule.onNodeWithText(card1.text).assertIsDisplayed()
		composeTestRule.onNodeWithText(card2.text).assertIsDisplayed()
	}

	@Test
	fun SelectOneCard() = runTest {
		AddTwoCards()
		composeTestRule.onNodeWithText(card1.text)
			.performTouchInput {
				longClick()
			}
		val selected = "Selected: 1"
		composeTestRule.onNodeWithText(selected).assertIsDisplayed()

		val edit = composeTestRule.activity.getString(R.string.edit)
		composeTestRule.onNodeWithContentDescription(edit)
			.assertIsEnabled()

		val delete = composeTestRule.activity.getString(R.string.delete)
		composeTestRule.onNodeWithContentDescription(delete)
			.assertIsEnabled()
	}

	@Test
	fun SelectTwoCards() = runTest {
		AddTwoCards()
		composeTestRule.onNodeWithText(card1.text)
			.performTouchInput {
				longClick()
			}
		composeTestRule.onNodeWithText(card2.text)
			.performClick()
		val selected = "Selected: 2"
		composeTestRule.onNodeWithText(selected).assertIsDisplayed()

		val edit = composeTestRule.activity.getString(R.string.edit)
		composeTestRule.onNodeWithContentDescription(edit)
			.assertIsNotEnabled()

		val delete = composeTestRule.activity.getString(R.string.delete)
		composeTestRule.onNodeWithContentDescription(delete)
			.assertIsEnabled()
	}

	@Test
	fun Deselect() = runTest {
		AddTwoCards()
		composeTestRule.onNodeWithText(card1.text)
			.performTouchInput {
				longClick()
			}
		composeTestRule.onNodeWithText(card1.text)
			.performClick()
		val selected = "Selected: 0"
		composeTestRule.onNodeWithText(selected).assertIsDisplayed()

		val edit = composeTestRule.activity.getString(R.string.edit)
		composeTestRule.onNodeWithContentDescription(edit)
			.assertIsNotEnabled()

		val delete = composeTestRule.activity.getString(R.string.delete)
		composeTestRule.onNodeWithContentDescription(delete)
			.assertIsNotEnabled()
	}

	@Test
	fun DeleteACard() = runTest {
		AddTwoCards()
		composeTestRule.onNodeWithText(card1.text)
			.performTouchInput {
				longClick()
			}
		val delete = composeTestRule.activity.getString(R.string.delete)
		composeTestRule.onNodeWithContentDescription(delete)
			.performClick()

		val confirmation = "Are you sure you want to delete this card?"
		composeTestRule.onNodeWithText(confirmation)
			.assertIsDisplayed()

		val delete_button = composeTestRule.activity.getString(R.string.delete_button)
		composeTestRule.onNodeWithText(delete_button)
			.performClick()

		backgroundScope.launch {
			dao.getAllCards().test {
				val cards = awaitItem()
				assertThat(cards).doesNotContain(card1)
			}
		}
	}

	@Test
	fun AddACard() = runTest {
		val txt = composeTestRule.activity.getString(R.string.add_card)
		composeTestRule.onNodeWithContentDescription(txt)
			.performClick()
		composeTestRule.waitUntil(5000L) {
			composeTestRule.onNodeWithTag("TextField")
				.isDisplayed()
		}
		composeTestRule.onNodeWithTag("TextField")
			.performTextInput(card1.text)
		OnBackPressedDispatcher().onBackPressed()
		val save = composeTestRule.activity.getString(R.string.save)
		composeTestRule.onNodeWithContentDescription(save)
			.performClick()
		composeTestRule.waitForIdle()

		backgroundScope.launch {
			dao.getAllCards().test {
				skipItems(1)
				val cards = awaitItem()
				assertThat(cards).contains(card1)
			}
		}
	}

}