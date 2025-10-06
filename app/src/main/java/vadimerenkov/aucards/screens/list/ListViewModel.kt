package vadimerenkov.aucards.screens.list

import android.util.Log
import androidx.core.net.toFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vadimerenkov.aucards.DispatchersProvider
import vadimerenkov.aucards.data.Aucard
import vadimerenkov.aucards.data.AucardDao

private const val TAG = "ListViewModel"

class ListViewModel(
	private val aucardDao: AucardDao,
	private val dispatchers: DispatchersProvider,
	initialPage: Int
): ViewModel() {
	private val list_state: MutableStateFlow<ListState> = MutableStateFlow(ListState(currentPage = initialPage))

	private val all_cards = aucardDao.getAllCards().onEach { list ->
		list_state.update { it.copy(list = list, isLoading = false) }
	}
		.flowOn(dispatchers.main)
		.launchIn(viewModelScope)

	private val favourite_cards = aucardDao.getFavouriteCards()
		.onEach { list ->
			list_state.update { it.copy(favouritesList = list) }
		}
		.flowOn(dispatchers.main)
		.launchIn(viewModelScope)

	val listState = list_state
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5000L),
			initialValue = list_state.value
		)

	fun markAsFavourite(id: Int) {
		val card = list_state.value.list.find { it -> it.id == id }
		if (card != null) {
			saveAucard(card.copy(isFavourite = !card.isFavourite))
		}
	}

	private fun saveAucard(aucard: Aucard) {
		viewModelScope.launch(dispatchers.main) {
			aucardDao.saveAucard(aucard)
		}
	}

	fun saveAllCards(newList: List<Aucard>) {
		viewModelScope.launch {
			aucardDao.saveAllCards(newList)
		}
		Log.i(TAG, "Saved new cards: $newList")
	}

	fun turnPage(number: Int) {
		list_state.update { it.copy(currentPage = number) }
	}

	fun enterSelectMode(id: Int) {
		list_state.update { it.copy(isSelectMode = true) }
		selectId(id)
	}

	fun exitSelectMode() {
		list_state.update { it.copy(isSelectMode = false, selectedList = emptyList()) }
	}

	fun selectId(id: Int) {
		list_state.update { it.copy(selectedList = it.selectedList + id) }
	}

	fun deselectId(id: Int) {
		list_state.update { it.copy(selectedList = it.selectedList - id) }
	}

	fun deleteSelected() {
		val selected_cards = list_state.value.list.filter {
			it.id in list_state.value.selectedList
		}
			selected_cards.forEach { card ->
				viewModelScope.launch(dispatchers.main) {
					if (card.imagePath != null) {
						try {
							val image = card.imagePath.toFile()
							image.delete()
						} catch (e: Exception) {
							if (e is CancellationException) throw e
							e.printStackTrace()
						}
					}
					aucardDao.deleteById(card.id)
				}
			}
		exitSelectMode()
	}
}

data class ListState(
	val list: List<Aucard> = emptyList(),
	val selectedList: List<Int> = emptyList(),
	val favouritesList: List<Aucard> = emptyList(),
	val currentPage: Int,
	val isLoading: Boolean = true,
	val isSelectMode: Boolean = false
)