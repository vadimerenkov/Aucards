package vadimerenkov.aucards.screens.fullscreencard

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CancellationException
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
import vadimerenkov.aucards.screens.settings.Settings
import java.io.File
import java.util.UUID

private const val TAG = "CardViewModel"

class CardViewModel(
	isDarkTheme: Boolean,
	val soundPlayer: ExoPlayer,
	private val settings: Settings,
	private val aucardDao: AucardDao,
	private val dispatchers: DispatchersProvider,
	private val id: Int,
	private val index: Int?
): ViewModel() {
	private val color = if (isDarkTheme) Color(0xFF263F71) else Color.White
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
			soundPlayer.addListener(
				object : Player.Listener {
					override fun onIsPlayingChanged(isPlaying: Boolean) {
						super.onIsPlayingChanged(isPlaying)
						card_state.update { it.copy(isSoundPlaying = isPlaying) }
					}
				}
			)
		}
		.flowOn(dispatchers.main)
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5000L),
			initialValue = card_state.value
		)

	private fun loadInitialData() {
		viewModelScope.launch(dispatchers.main) {
			val brightness = settings.brightness.first() == true
			val landscape = settings.landscape.first()
			var playSound = settings.playSound.first() == true
			val ringtoneUri = settings.soundUri.first()?.toUri()
			if (id != 0) {
				val card = aucardDao.getAucardByID(id).first()

				if (ringtoneUri == null) {
					playSound = false
				} else {
					if (playSound) {
						val sound = MediaItem.fromUri(ringtoneUri)
						soundPlayer.setMediaItem(sound)
						soundPlayer.prepare()
						soundPlayer.play()
					}
				}

				card_state.update {
					it.copy(
						aucard = card,
						isMaxBrightness = brightness,
						isLandscapeMode = landscape,
						isPlaySoundEnabled = playSound,
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

	private fun saveAucard(
		aucard: Aucard,
		context: Context
	) {
		if (index != null) {
			aucard.index = index
		}
		viewModelScope.launch(dispatchers.main) {
			aucard.imagePath?.let { path ->
				try {
					val file = File(context.filesDir, "${UUID.randomUUID()}")
					val outputStream = file.outputStream()
					val inputStream = context.contentResolver.openInputStream(path)
					inputStream?.copyTo(outputStream)
					inputStream?.close()
					outputStream.close()
					val card = aucard.copy(imagePath = file.toUri())
					aucardDao.saveAucard(card)
					return@launch
				} catch (e: Exception) {
					if (e is CancellationException) throw e
					e.printStackTrace()
					return@launch
				}
			}
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

	private fun updateColor(color: Color) {
		card_state.update { it.copy(aucard = it.aucard.copy(color = color)) }
	}

	private fun updateHexCode(hex: String) {
		var hex_code = if (hex.startsWith("#")) hex else "#$hex"
		if (hex_code.endsWith("#")) hex_code = hex_code.dropLast(1)
		card_state.update { it.copy(hexColor = hex_code) }
		try {
			val color_int = hex_code.toColorInt()
			val color = Color(color_int).copy(alpha = 1f)
			card_state.update { it.copy(isHexCodeValid = true, aucard = it.aucard.copy(color = color)) }
		}
		catch (_: Exception) {
			card_state.update { it.copy(isHexCodeValid = false) }
		}
	}

	private fun changePopup(popup: OpenPopup) {
		val new_popup = if (popup == cardState.value.openPopup) OpenPopup.NONE else popup
		card_state.update { it.copy(openPopup = new_popup) }
	}

	private fun changeTextFontSize(size: Int) {
		card_state.update { it.copy(aucard = cardState.value.aucard.copy(titleFontSize = size)) }
	}

	private fun changeDescFontSize(size: Int) {
		card_state.update { it.copy(aucard = cardState.value.aucard.copy(descriptionFontSize = size)) }
	}

	private fun changeLayout(layout: CardLayout) {
		if (layout == CardLayout.TWO_HALVES) {
			card_state.update { it.copy(aucard = cardState.value.aucard.copy(
				layout = layout,
				titleFontSize = 57,
				descriptionFontSize = 57
			)) }
		} else {
			card_state.update { it.copy(aucard = cardState.value.aucard.copy(
				layout = layout,
				titleFontSize = 57,
				descriptionFontSize = 24
			)) }
		}
	}

	private fun changeImage(uri: Uri?) {
		val is_editing = uri != null
		with (cardState.value.aucard) {
			card_state.update { it.copy(
				isEditingImage = is_editing,
				aucard = copy(
					imagePath = uri,
					imageRotation = 0f,
					imageOffset = Offset.Zero,
					imageScale = 1f
			)) }
		}
	}

	private var realRotation = 0f

	fun transformImage(
		scale: Float,
		offset: Offset,
		rotation: Float
	) {
		with (cardState.value.aucard) {

			// Snap rotation to right angles
			var totalRotation = imageRotation + rotation

			realRotation += rotation

			Log.i(TAG, "scale is $imageScale, offset is $imageOffset, rotation is $imageRotation")

			if (realRotation in -5f..5f) totalRotation = 0f
			if (realRotation in 85f..90f) totalRotation = 90f
			if (realRotation in 175f..185f) totalRotation = 180f
			if (realRotation in 265f..275f) totalRotation = 270f

			if (realRotation > 360f || realRotation < -360f) {
				totalRotation = 0f
				realRotation = 0f
			}

			if (realRotation in -95f..-85f) totalRotation = -90f
			if (realRotation in -185f..-175f) totalRotation = -180f
			if (realRotation in -275f..-265f) totalRotation = -270f

			card_state.update {
				it.copy(
					aucard = copy(
						imageScale = imageScale * scale,
						imageOffset = imageOffset + offset * imageScale,
						imageRotation = totalRotation
					)
				)
			}
		}
	}

	fun selectImage(value: Boolean) {
		card_state.update { it.copy(isEditingImage = value) }
	}

	private fun changeOpacity(opacity: Float) {
		card_state.update { it.copy(aucard = cardState.value.aucard.copy(textBackgroundOpacity = opacity)) }
	}

	fun onAction(action: CardAction) {
		when (action) {
			is CardAction.Saved -> {
				saveAucard(
					aucard = action.aucard,
					context = action.context
				)
			}
			is CardAction.LayoutChanged -> {
				changeLayout(action.layout)
			}
			is CardAction.PopupChanged -> {
				changePopup(action.openPopup)
			}
			is CardAction.ColorSelected -> {
				updateColor(action.color)
			}
			is CardAction.DescSizeChanged -> {
				changeDescFontSize(action.size)
			}
			is CardAction.HexCodeChanged -> {
				updateHexCode(action.hex)
			}
			is CardAction.TextSizeChanged -> {
				changeTextFontSize(action.size)
			}
			is CardAction.ImageUriChanged -> {
				changeImage(action.uri)
			}
			is CardAction.TextBackgroundChanged -> {
				changeOpacity(action.opacity)
			}
		}
	}

	fun hasPermission(context: Context): Boolean {
		return android.provider.Settings.System.canWrite(context) && card_state.value.isMaxBrightness
	}

	override fun onCleared() {
		super.onCleared()
		soundPlayer.stop()
		soundPlayer.release()
		Log.i(TAG, "Cleared the viewmodel and player")
	}
}

enum class OpenPopup {
	NONE,
	PALETTE,
	FONT_SIZE,
	LAYOUT,
	IMAGE
}