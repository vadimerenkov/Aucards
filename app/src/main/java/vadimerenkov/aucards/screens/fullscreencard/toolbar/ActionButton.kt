package vadimerenkov.aucards.screens.fullscreencard.toolbar

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

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
			.size(70.dp)
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
	tint: Color = MaterialTheme.colorScheme.primary
) {
	val animatedColor by animateColorAsState(
		if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.Transparent
	)
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
			.size(70.dp)
			.background(animatedColor)
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