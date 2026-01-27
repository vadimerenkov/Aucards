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
import vadimerenkov.aucards.data.CardCategory
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
		aucardDao.getAllCards()
			.onEach { list ->
				list_state.update { it.copy(list = list, isLoading = false) }
			}.launchIn(viewModelScope)

		aucardDao.getFavouriteCards()
			.onEach { list ->
				list_state.update { it.copy(favouritesList = list) }
			}.launchIn(viewModelScope)

		aucardDao.getAllCategories()
			.onEach { list ->
				list_state.update { it.copy(categories = list) }
			}.launchIn(viewModelScope)
	}
	val listState = list_state
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5000L),
			initialValue = list_state.value
		)

	fun markAsFavourite(id: Int) {
		val card = list_state.value.list.find { it.id == id }
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

	fun duplicateSelected() {
		viewModelScope.launch {
			val duplicatedCards = list_state.value.selectedCards.map { it.copy(id = 0) }
			aucardDao.saveAllCards(duplicatedCards)
		}
	}

	fun deleteSelected(context: Context) {
		val selectedCards = list_state.value.selectedCards
		selectedCards.forEach { card ->
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
		val message = context.getPluralString(R.plurals.number_deleted, selectedCards.size, selectedCards.size)
			viewModelScope.launch {
				SnackbarController.send(message)
			}
		exitSelectMode()
	}

	fun enterNewCategoryName(name: String) {
		list_state.update { it.copy(newCategoryName = name) }
	}

	fun createNewCategory() {
		val category = CardCategory(
			name = listState.value.newCategoryName,
			index = listState.value.categories.size + 1
		)
		viewModelScope.launch {
			aucardDao.saveCategory(category)
		}
		list_state.update { it.copy(
			newCategoryName = ""
		) }
	}

	suspend fun saveCategories(categories: List<CardCategory>) {
		categories.forEach {
			aucardDao.saveCategory(it)
			Log.i(TAG, "Saved category $it")
		}
	}

	fun selectCategory(category: CardCategory?) {
		list_state.update { it.copy(selectedCategory = category) }
	}

	fun deleteCategory(category: CardCategory) {
		val cards = listState.value.list
			.filter { it.categories.contains(category.id) }
			.map { it.copy(categories = it.categories.minus(category.id)) }
		viewModelScope.launch {
			aucardDao.deleteCategory(category)
			aucardDao.saveAllCards(cards)
		}
		if (listState.value.selectedCategory == category) {
			list_state.update { it.copy(selectedCategory = null) }
		}
	}

	fun renameCategory(category: CardCategory, name: String) {
		viewModelScope.launch {
			aucardDao.saveCategory(category.copy(name = name))
		}
	}

	fun updateCategories(categories: List<Int>) {
		val card = listState.value.selectedCards.first()
		saveAucard(card.copy(categories = categories))
	}
}

data class ListState(
	val list: List<Aucard> = emptyList(),
	val selectedList: List<Int> = emptyList(),
	val favouritesList: List<Aucard> = emptyList(),
	val categories: List<CardCategory> = emptyList(),
	val selectedCategory: CardCategory? = null,
	val newCategoryName: String = "",
	val currentPage: Int,
	val isLoading: Boolean = true,
	val isSelectMode: Boolean = false
) {
	val selectedCards: List<Aucard>
		get() = list.filter {
			it.id in selectedList
		}
}