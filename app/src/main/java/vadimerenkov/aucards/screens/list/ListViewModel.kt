package vadimerenkov.aucards.screens.list

import android.content.Context
import android.util.Log
import androidx.core.net.toFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vadimerenkov.aucards.DispatchersProvider
import vadimerenkov.aucards.R
import vadimerenkov.aucards.data.Aucard
import vadimerenkov.aucards.data.AucardDao
import vadimerenkov.aucards.ui.SnackbarController
import vadimerenkov.aucards.ui.getPluralString

private const val TAG = "ListViewModel"

class ListViewModel(
	private val aucardDao: AucardDao,
	private val dispatchers: DispatchersProvider,
	initialPage: Int
): ViewModel() {
	private val list_state: MutableStateFlow<ListState> = MutableStateFlow(ListState(currentPage = initialPage))

	init {
		aucardDao.getAllCards().onEach { list ->
			list_state.update { it.copy(list = list, isLoading = false) }
		}
			.launchIn(viewModelScope)

		aucardDao.getFavouriteCards()
			.onEach { list ->
				list_state.update { it.copy(favouritesList = list) }
			}
			.launchIn(viewModelScope)
	}
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
		viewModelScope.launch(dispatchers.default) {
			aucardDao.saveAucard(aucard)
		}
	}

	fun saveAllCards(newList: List<Aucard>) {
		viewModelScope.launch {
			aucardDao.saveAllCards(newList)
		}
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

	fun deleteSelected(context: Context) {
		val selected_cards = list_state.value.list.filter {
			it.id in list_state.value.selectedList
		}
			selected_cards.forEach { card ->
				viewModelScope.launch(dispatchers.io) {
					if (card.imagePath != null) {
						try {
							val image = card.imagePath.toFile()
							image.delete()
						} catch (e: Exception) {
							if (e is CancellationException) throw e
							Log.e(TAG, "Error deleting the image: $e")
						}
					}
					aucardDao.deleteById(card.id)
				}
			}
		val message = context.getPluralString(R.plurals.number_deleted, selected_cards.size, selected_cards.size)
			viewModelScope.launch {
				SnackbarController.send(message)
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