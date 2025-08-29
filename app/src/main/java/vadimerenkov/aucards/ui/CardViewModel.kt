package vadimerenkov.aucards.ui

import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vadimerenkov.aucards.DispatchersProvider
import vadimerenkov.aucards.data.Aucard
import vadimerenkov.aucards.data.AucardDao
import vadimerenkov.aucards.settings.Settings

private const val TAG = "CardViewModel"

class CardViewModel(
	val settings: Settings,
	private val aucardDao: AucardDao,
	private val dispatchers: DispatchersProvider,
	isDarkTheme: Boolean,
	private val id: Int,
	private val index: Int?
): ViewModel() {

	private val color = if (isDarkTheme) Color.Black else Color.White

	private var card_state = MutableStateFlow(CardState(
		aucard = Aucard(
			text = "",
			color = color
		)
	))

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
			val playSound = settings.playSound.first() ?: false
			val ringtoneUri = settings.readSoundUri()
			if (id != 0) {
				val card = aucardDao.getAucardByID(id).first()
				card_state.update {
					it.copy(
						aucard = card,
						isMaxBrightness = brightness,
						isLandscapeMode = landscape,
						isPlaySoundEnabled = playSound,
						ringtoneUri = ringtoneUri,
						isValid = card.text.isNotEmpty()
					)
				}
			} else {
				card_state.update {
					it.copy(
						isLandscapeMode = landscape
					)
				}
			}
		}
	}

	fun saveAucard(aucard: Aucard) {
		if (index != null) {
			aucard.index = index
		}
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

	fun updateHexCode(hex: String) {
		var hex_code = if (hex.startsWith("#")) hex else "#$hex"
		if (hex_code.endsWith("#")) hex_code = hex_code.dropLast(1)
		card_state.update { it.copy(hexColor = hex_code) }
		try {
			val color_int = hex_code.toColorInt()
			val color = Color(color_int).copy(alpha = 1f)
			card_state.update { it.copy(isHexCodeValid = true, aucard = it.aucard.copy(color = color)) }
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
	val isPlaySoundEnabled: Boolean = false,
	val ringtoneUri: Uri? = null,
	val hexColor: String = "",
	val isHexCodeValid: Boolean = true
)