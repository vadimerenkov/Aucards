package vadimerenkov.aucards.screens.fullscreencard.toolbar

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

private const val BUTTON_SIZE = 64

@Composable
fun ActionButton(
	onClick: () -> Unit,
	icon: ImageVector,
	modifier: Modifier = Modifier,
	enabled: Boolean = true,
	contentDescription: String? = null,
	tint: Color = MaterialTheme.colorScheme.primary
) {
	Box(
		contentAlignment = Alignment.Center,
		modifier = modifier
			.clip(CircleShape)
			.clickable(
				enabled = enabled,
				onClickLabel = contentDescription
			) {
				onClick()
			}
			.size(BUTTON_SIZE.dp)
	) {
		Icon(
			imageVector = icon,
			contentDescription = null,
			tint = tint,
			modifier = Modifier
				.fillMaxSize(0.7f)
		)
	}
}

@Composable
fun ActionButton(
	onClick: () -> Unit,
	icon: Painter,
	modifier: Modifier = Modifier,
	selected: Boolean = false,
	enabled: Boolean = true,
	contentDescription: String? = null,
	iconSize: Int = BUTTON_SIZE,
	tint: Color = MaterialTheme.colorScheme.secondary
) {
	val strokeWidth by animateFloatAsState(if (selected) 10f else 0f)
	Box(
		contentAlignment = Alignment.Center,
		modifier = modifier
			.drawBehind {
				if (selected) {
					drawLine(
						color = tint,
						start = Offset(0f, size.height),
						end = Offset(size.width, size.height),
						strokeWidth = strokeWidth
					)
				}
			}
			.clip(CircleShape)
			.clickable(
				enabled = enabled,
				onClickLabel = contentDescription
			) {
				onClick()
			}
			.size(iconSize.dp)
	) {
		Icon(
			painter = icon,
			contentDescription = null,
			tint = tint,
			modifier = Modifier
				.fillMaxSize(0.7f)
		)
	}
}