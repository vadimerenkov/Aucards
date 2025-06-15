@file:OptIn(ExperimentalCoroutinesApi::class)

package vadimerenkov.aucards.ui

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import vadimerenkov.aucards.data.Aucard
import vadimerenkov.aucards.fakes.FakeDao
import vadimerenkov.aucards.fakes.TestDispatchers

class ListViewModelTest {

	lateinit var dao: FakeDao
	lateinit var viewModel: ListViewModel

	val card1 = Aucard(id = 1, text = "we are the champions", description = "my friend")
	val card2 = Aucard(id = 2, text = "lorem ipsum", description = "dorime")

	private suspend fun addTwoCards() {
		dao.saveAucard(card1)
		dao.saveAucard(card2)
	}

	@Before
	fun SetupViewModel() {
		dao = FakeDao()
		viewModel = ListViewModel(
			aucardDao = dao,
			dispatchers = TestDispatchers()
		)
	}

	@Test
	fun SelectACard() = runTest {
		launch {
			viewModel.listState.test {
				skipItems(1)
				val state = awaitItem()
				assertThat(state.isSelectMode).isTrue()
				assertThat(state.selectedList).contains(card1.id)
				cancelAndConsumeRemainingEvents()
			}
		}
		addTwoCards()
		viewModel.EnterSelectMode(card1.id)
	}

	@Test
	fun SelectTwoCards() = runTest {
		launch {
			viewModel.listState.test {
				skipItems(1)
				val state = awaitItem()
				assertThat(state.selectedList).containsExactly(card1.id, card2.id)
				cancelAndConsumeRemainingEvents()
			}
		}
		addTwoCards()
		viewModel.EnterSelectMode(card1.id)
		viewModel.SelectId(card2.id)
	}

	@Test
	fun DeselectCard() = runTest {
		addTwoCards()
		backgroundScope.launch {
			viewModel.listState.test {
				awaitItem()
				val state = awaitItem()
				assertThat(state.selectedList).contains(card1.id)
				val state2 = awaitItem()
				assertThat(state2.selectedList).isEmpty()
				cancelAndConsumeRemainingEvents()
			}
		}

		viewModel.EnterSelectMode(card1.id)
		viewModel.DeselectId(card1.id)
	}

	@Test
	fun ExitSelection() = runTest {
		backgroundScope.launch {
			viewModel.listState.test {
				val state = awaitItem()
				assertThat(state.selectedList).contains(card1.id)
				val state2 = awaitItem()
				assertThat(state2.selectedList).isEmpty()
				assertThat(state2.isSelectMode).isFalse()
				cancelAndConsumeRemainingEvents()
			}
		}
		addTwoCards()
		viewModel.EnterSelectMode(card1.id)
		viewModel.ExitSelectMode()
	}

	@Test
	fun DeleteCard() = runTest {
		launch {
			viewModel.listState.test {
				skipItems(1)
				val state = awaitItem()
				assertThat(state.isSelectMode).isFalse()
				assertThat(state.selectedList).isEmpty()
				assertThat(dao.getAllCards().first()).doesNotContain(card1)
			}
		}
		addTwoCards()
		viewModel.EnterSelectMode(card1.id)
		viewModel.DeleteSelected()
	}
}