package vadimerenkov.aucards.screens.fullscreencard

import android.net.Uri
import androidx.compose.ui.graphics.Color
import vadimerenkov.aucards.data.Aucard
import vadimerenkov.aucards.data.CardLayout

sealed interface CardAction {
	data class PopupChanged(val openPopup: OpenPopup): CardAction
	data class Saved(val aucard: Aucard): CardAction
	data class LayoutChanged(val layout: CardLayout): CardAction
	data class ColorSelected(val color: Color): CardAction
	data class HexCodeChanged(val hex: String): CardAction
	data class TextSizeChanged(val size: Int): CardAction
	data class DescSizeChanged(val size: Int): CardAction
	data class ImageUriChanged(val uri: Uri?): CardAction
	data class TextBackgroundChanged(val opacity: Float): CardAction
}