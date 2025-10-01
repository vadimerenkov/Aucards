package vadimerenkov.aucards.screens.fullscreencard

import vadimerenkov.aucards.data.Aucard

data class CardState(
	val aucard: Aucard,
	val openPopup: OpenPopup = OpenPopup.NONE,
	val isMaxBrightness: Boolean = false,
	val isLandscapeMode: Boolean? = null,
	val isPlaySoundEnabled: Boolean = false,
	val isSoundPlaying: Boolean = false,
	val hexColor: String = "",
	val isHexCodeValid: Boolean = true,
	val isEditingImage: Boolean = false
) {
	val isValid: Boolean
		get() = aucard.text.isNotBlank()
}