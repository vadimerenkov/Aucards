package vadimerenkov.aucards.screens.settings

import android.net.Uri

data class SettingsState(
	val theme: Theme = Theme.DEVICE,
	val isMaxBrightness: Boolean = false,
	val isLandscapeMode: Boolean = false,
	val playSound: Boolean = false,
	val voiceCard: Boolean = false,
	val materialYou: Boolean = false,
	val ringtoneUri: Uri? = null,
	val language: Language = Language.ENGLISH,
	val isDbEmpty: Boolean = true
)