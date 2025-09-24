package vadimerenkov.aucards.screens.fullscreencard.toolbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import vadimerenkov.aucards.R
import vadimerenkov.aucards.screens.fullscreencard.CardAction
import vadimerenkov.aucards.ui.theme.AucardsTheme
import kotlin.math.roundToInt

private const val MIN_SIZE = 20f
private const val MAX_SIZE = 100f

@Composable
fun FontSizePopup(
	onAction: (CardAction) -> Unit,
	textSizeValue: Float,
	descSizeValue: Float,
	modifier: Modifier = Modifier
) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = modifier
			.padding(16.dp)
	) {
		Text(
			text = stringResource(R.string.text_size),
			style = MaterialTheme.typography.titleLarge,
			color = MaterialTheme.colorScheme.onBackground
		)
		Slider(
			valueRange = MIN_SIZE..MAX_SIZE,
			value = textSizeValue,
			colors = SliderDefaults.colors(
				inactiveTrackColor = MaterialTheme.colorScheme.onPrimary
			),
			onValueChange = {
				onAction(CardAction.TextSizeChanged(it.roundToInt()))
			}
		)
		Slider(
			valueRange = MIN_SIZE..MAX_SIZE,
			value = descSizeValue,
			colors = SliderDefaults.colors(
				inactiveTrackColor = MaterialTheme.colorScheme.onPrimary
			),
			onValueChange = {
				onAction(CardAction.DescSizeChanged(it.roundToInt()))
			}
		)
	}
}

@PreviewLightDark
@Composable
private fun FontSizePreview() {
	AucardsTheme {
		Box(
			modifier = Modifier
				.background(MaterialTheme.colorScheme.primaryContainer)
		) {
			FontSizePopup(
				onAction = {},
				textSizeValue = 57f,
				descSizeValue = 24f
			)
		}
	}
}