package vadimerenkov.aucards.screens.fullscreencard.toolbar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import vadimerenkov.aucards.R
import vadimerenkov.aucards.data.CardLayout
import vadimerenkov.aucards.screens.fullscreencard.CardAction
import vadimerenkov.aucards.ui.applyIf
import vadimerenkov.aucards.ui.theme.AucardsTheme

@Composable
fun LayoutPopup(
	onAction: (CardAction) -> Unit,
	selectedLayout: CardLayout,
	modifier: Modifier = Modifier
) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = modifier
			.padding(16.dp)
	) {
		Text(
			text = stringResource(R.string.layout),
			style = MaterialTheme.typography.titleLarge,
			color = MaterialTheme.colorScheme.onBackground
		)
		CardLayout.entries.forEach { layout ->
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier
					.clickable {
						onAction(CardAction.LayoutChanged(layout))
					}
					.fillMaxWidth()
			) {
				RadioButton(
					selected = selectedLayout == layout,
					onClick = {
						onAction(CardAction.LayoutChanged(layout))
					}
				)
				Icon(
					painter = painterResource(layout.icon),
					contentDescription = null,
					tint = MaterialTheme.colorScheme.primary,
					modifier = Modifier
						.applyIf(layout == CardLayout.TWO_HALVES) {
							rotate(90f)
						}
				)
				Spacer(modifier = Modifier.width(8.dp))
				Text(
					text = stringResource(layout.description),
					style = MaterialTheme.typography.titleLarge,
					color = MaterialTheme.colorScheme.onBackground
				)
			}
		}
	}
}

@PreviewLightDark
@Composable
private fun LayoutPreview() {
	AucardsTheme {
		Box(
			modifier = Modifier
				.background(MaterialTheme.colorScheme.primaryContainer)
		) {
			LayoutPopup(
				onAction = {},
				selectedLayout = CardLayout.TITLE_SUBTITLE
			)
		}
	}
}