package vadimerenkov.aucards.ui

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
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

private const val TAG = "CardViewModel"

class CardViewModel(
	savedStateHandle: SavedStateHandle,
	val settings: Settings,
	private val aucardDao: AucardDao,
	private val dispatchers: DispatchersProvider,
	isDarkTheme: Boolean
): ViewModel() {

	private val route = savedStateHandle.toRoute<FullscreenCard>()
	private val id: Int = route.id
	val color = if (isDarkTheme) Color.Black else Color.White

	private var card_state = MutableStateFlow(CardState(
		aucard = Aucard(
			text = "",
			color = color
		)
	))

	init {
		//loadInitialData()
	}

	var cardState = card_state
		.onStart {
			loadInitialData()
		}
		.flowOn(dispatchers.main)
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5000L),
			initialValue = card_state.value
		)

	private fun loadInitialData() {
		viewModelScope.launch(dispatchers.main) {
			val brightness = settings.brightness.first() ?: false
			val landscape = settings.landscape.first()
			if (id != 0) {
				val card = aucardDao.getAucardByID(id).first()
				card_state.update {
					it.copy(
						aucard = card,
						isMaxBrightness = brightness,
						isLandscapeMode = landscape,
						isValid = card.text.isNotEmpty()
					)
				}
			} else {
				card_state.update {
					it.copy(
						isMaxBrightness = brightness,
						isLandscapeMode = landscape
					)
				}
			}
		}
	}

	fun saveAucard(aucard: Aucard) {
		viewModelScope.launch(dispatchers.main) {
			aucardDao.saveAucard(aucard)
		}
	}

	fun updateText(text: String) {
		card_state.update { it.copy(aucard = it.aucard.copy(text = text), isValid = text.isNotEmpty()) }
	}

	fun updateDescription(description: String) {
		card_state.update { it.copy(aucard = it.aucard.copy(description = description)) }
	}

	fun updateColor(color: Color) {
		card_state.update { it.copy(aucard = it.aucard.copy(color = color)) }
	}

	fun updateBackgroundImage(imageUri: String?) {
		card_state.update { it.copy(aucard = it.aucard.copy(backgroundImageUri = imageUri)) }
	}

	fun updateHexCode(hex: String) {
		card_state.update { it.copy(hexColor = hex) }
		try {
			val color = ("#$hex").toColorInt()
			card_state.update { it.copy(isHexCodeValid = true, aucard = it.aucard.copy(color = Color(color))) }
		}
		catch (e: Exception) {
			card_state.update { it.copy(isHexCodeValid = false) }
		}
	}
}

data class CardState(
	val aucard: Aucard,
	val isValid: Boolean = false,
	val isMaxBrightness: Boolean = false,
	val isLandscapeMode: Boolean? = null,
	val hexColor: String = "",
	val isHexCodeValid: Boolean = true
)