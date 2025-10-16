package vadimerenkov.aucards.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Source: https://medium.com/@mittalkshitij20/adding-a-custom-scrollbar-to-a-column-in-jetpack-compose-9996c26f498f
@Composable
fun Modifier.verticalScrollbar(
	scrollState: ScrollState,
	width: Dp = 6.dp,
	showScrollBarTrack: Boolean = true,
	scrollBarTrackColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
	scrollBarColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
	scrollBarCornerRadius: Float = 6f,
	verticalPadding: Dp = 0.dp,
	horizontalPadding: Dp = 0.dp
): Modifier {
	return drawWithContent {
		drawContent()

		val viewportHeight = this.size.height
		val totalContentHeight = scrollState.maxValue.toFloat() + viewportHeight
		val scrollValue = scrollState.value.toFloat()

		val scrollBarHeight =
			(viewportHeight / totalContentHeight) * viewportHeight - verticalPadding.toPx()
		val trackHeight = viewportHeight - verticalPadding.toPx()
		val scrollbarWidth = width.toPx() + horizontalPadding.toPx()
		val scrollBarStartOffset =
			(scrollValue / totalContentHeight) * viewportHeight + verticalPadding.toPx() / 2f

		if (scrollBarHeight != trackHeight) {
			if (showScrollBarTrack) {
				drawRoundRect(
					cornerRadius = CornerRadius(scrollBarCornerRadius),
					color = scrollBarTrackColor,
					topLeft = Offset(
						x = this.size.width - scrollbarWidth,
						y = verticalPadding.toPx() / 2f
					),
					size = Size(width.toPx(), trackHeight),
				)
			}

			drawRoundRect(
				cornerRadius = CornerRadius(scrollBarCornerRadius),
				color = scrollBarColor,
				topLeft = Offset(
					this.size.width - scrollbarWidth,
					scrollBarStartOffset
				),
				size = Size(width.toPx(), scrollBarHeight)
			)
		}
	}
}