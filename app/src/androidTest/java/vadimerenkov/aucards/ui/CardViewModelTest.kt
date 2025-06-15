@file:OptIn(ExperimentalCoroutinesApi::class)

package vadimerenkov.aucards.ui

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.testing.ViewModelScenario
import androidx.lifecycle.viewmodel.testing.viewModelScenario
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import vadimerenkov.aucards.data.Aucard
import vadimerenkov.aucards.fakes.FakeDao
import vadimerenkov.aucards.fakes.TestDispatchers
import vadimerenkov.aucards.fakes.getSettings

class CardViewModelTest {

	lateinit var viewModel: CardViewModel
	lateinit var dispatchers: TestDispatchers
	lateinit var scenario: ViewModelScenario<CardViewModel>
	lateinit var dao: FakeDao

	@Before
	fun SetupViewModel() {
		dao = FakeDao()
		scenario = viewModelScenario {
			CardViewModel(
				savedStateHandle = SavedStateHandle(mapOf("id" to 0, "isEditable" to true)),
				aucardDao = dao,
				settings = getSettings(),
				dispatchers = dispatchers
			)
		}

		dispatchers = TestDispatchers()
		viewModel = scenario.viewModel
	}

	@After
	fun tearDown() {
		scenario.close()
	}

	/**
	 * This test for some reason fails if run with all others with TurbineAssertionError.
	 * Would appreciate some insight on that.
	 */
	@Test
	fun ValidInputValidatesInput() = runTest() {
		val card = Aucard(id = 1, text = "we are the champions", description = "my friend")
		launch {
			viewModel.cardState.test {
				val state = awaitItem()
				assertThat(state).isEqualTo(CardState())
				val state2 = awaitItem()
				assertThat(state2.isValid).isTrue()
				cancelAndConsumeRemainingEvents()
			}
		}

		viewModel.UpdateState(card)
	}

	@Test
	fun InvalidInputInvalidatesInput() = runTest {
		val card = Aucard(id = 1, text = "", description = "my friend")
		launch {
			viewModel.cardState.test {
				skipItems(1)
				val state = awaitItem()
				Log.i("CardViewModel", state.toString())
				assertThat(state.isValid).isFalse()
				cancelAndConsumeRemainingEvents()
			}
		}
		viewModel.UpdateState(card)
	}

	@Test
	fun SaveCardSavesCard() = runTest {
		val card = Aucard(id = 1, text = "we are the champions", description = "my friend")
		viewModel.SaveAucard(card)
		assertThat(dao.getAllCards().first()).contains(card)
	}
}