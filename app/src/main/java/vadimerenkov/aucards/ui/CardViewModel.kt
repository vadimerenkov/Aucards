package vadimerenkov.aucards.ui

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vadimerenkov.aucards.DispatchersProvider
import vadimerenkov.aucards.FullscreenCard
import vadimerenkov.aucards.data.Aucard
import vadimerenkov.aucards.data.AucardDao
import vadimerenkov.aucards.settings.Settings


class CardViewModel(
	savedStateHandle: SavedStateHandle,
	val settings: Settings,
	private val aucardDao: AucardDao,
	private val dispatchers: DispatchersProvider
): ViewModel() {
	private val TAG = "CardViewModel"

	private val route = savedStateHandle.toRoute<FullscreenCard>()
	private val id: Int = route.id
	private val isEditable: Boolean = route.isEditable

	private var card_state = MutableStateFlow(CardState())

	var cardState = card_state
		.onStart {
			Log.i("CardViewModel", savedStateHandle.toString())
			Log.i("CardViewModel", route.toString())
			val brightness = settings.brightness.first() ?: false
			val landscape = settings.landscape.first()
			if (id != 0) {
				viewModelScope.launch(dispatchers.main) {
					val card = aucardDao.getAucardByID(id).first()
					card_state.update { it.copy(
						aucard = card,
						isEditable = isEditable,
						isMaxBrightness = brightness,
						isLandscapeMode = landscape
					) }
				}
			}
			else {
				card_state.update { it.copy(isEditable = isEditable, isMaxBrightness = brightness, isLandscapeMode = landscape) }
			}
		}
		.flowOn(dispatchers.main)
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5000L),
			initialValue = CardState()
		)

	fun SaveAucard(aucard: Aucard) {
		viewModelScope.launch(dispatchers.main) {
			aucardDao.saveAucard(aucard)
		}
	}

	fun UpdateState(aucard: Aucard) {
		card_state.update { it.copy(aucard) }
		ValidateCard(aucard)
	}

	private fun ValidateCard(aucard: Aucard) {
		card_state.update { it.copy(isValid = aucard.text.isNotEmpty()) }
	}
}

data class CardState(
	val aucard: Aucard = Aucard(text = ""),
	val isEditable: Boolean = false,
	val isValid: Boolean = false,
	val isMaxBrightness: Boolean = false,
	val isLandscapeMode: Boolean? = null
)