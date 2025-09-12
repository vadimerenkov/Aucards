package vadimerenkov.aucards.ui

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vadimerenkov.aucards.DispatchersProvider
import vadimerenkov.aucards.data.Aucard
import vadimerenkov.aucards.data.AucardDao
import vadimerenkov.aucards.data.CardLayout
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
	val titleInteractionSource = MutableInteractionSource()
	val descriptionInteractionSource = MutableInteractionSource()

	private val titleInteractions = titleInteractionSource.interactions
	private val descInteractions = descriptionInteractionSource.interactions
	private val interactions = merge(titleInteractions, descInteractions)
		.onEach {
			Log.i(TAG, "Interaction just interacted: $it")
			if (it is FocusInteraction.Focus || it is PressInteraction.Press) {
				changePopup(OpenPopup.NONE)
			}
		}
		.launchIn(viewModelScope)

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
			val ringtoneUri = settings.soundUri.first()?.toUri()
			if (id != 0) {
				val card = aucardDao.getAucardByID(id).first()
				card_state.update {
					it.copy(
						aucard = card,
						isMaxBrightness = brightness,
						isLandscapeMode = landscape,
						isPlaySoundEnabled = playSound,
						ringtoneUri = ringtoneUri
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
		card_state.update { it.copy(
			aucard = it.aucard.copy(text = text)
		) }
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

	fun changePopup(popup: OpenPopup) {
		val new_popup = if (popup == cardState.value.openPopup) OpenPopup.NONE else popup
		card_state.update { it.copy(openPopup = new_popup) }
	}

	fun changeTextFontSize(size: Int) {
		card_state.update { it.copy(aucard = cardState.value.aucard.copy(titleFontSize = size)) }
	}

	fun changeDescFontSize(size: Int) {
		card_state.update { it.copy(aucard = cardState.value.aucard.copy(descriptionFontSize = size)) }
	}

	fun changeLayout(layout: CardLayout) {
		card_state.update { it.copy(aucard = cardState.value.aucard.copy(layout = layout)) }
	}

	fun hasPermission(context: Context): Boolean {
		return android.provider.Settings.System.canWrite(context) && card_state.value.isMaxBrightness
	}
}

data class CardState(
	val aucard: Aucard,
	val openPopup: OpenPopup = OpenPopup.NONE,
	val isMaxBrightness: Boolean = false,
	val isLandscapeMode: Boolean? = null,
	val isPlaySoundEnabled: Boolean = false,
	val ringtoneUri: Uri? = null,
	val hexColor: String = "",
	val isHexCodeValid: Boolean = true
) {
	val isValid: Boolean
		get() = aucard.text.isNotBlank()
}

enum class OpenPopup {
	NONE,
	PALETTE,
	FONT_SIZE,
	LAYOUT
}