package vadimerenkov.aucards.screens.fullscreencard.layouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vadimerenkov.aucards.ui.theme.AucardsTheme

@Composable
fun TitleSubtitleLayout(
	displayText: @Composable () -> Unit,
	modifier: Modifier = Modifier,
	descriptionText: @Composable (() -> Unit)? = null
) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
		modifier = modifier
			.padding(24.dp)
	) {
		displayText()
		descriptionText?.let {
			it()
		}
	}
}

@Preview
@Composable
private fun TitleSubtitleTextPreview() {
	AucardsTheme {
		Box(
			contentAlignment = Alignment.Center,
			modifier = Modifier.fillMaxSize()
		) {
			TitleSubtitleLayout(
				displayText = {
					DisplayText(
						text = "Display text",
						textSize = 57,
						color = Color.Black
					)
				},
				descriptionText = {
					DisplayText(
						text = "Description text",
						textSize = 24,
						color = Color.Black
					)
				}
			)
		}
	}
}

@Preview
@Composable
private fun TitleSubtitleFieldPreview() {
	AucardsTheme {
		Box(
			contentAlignment = Alignment.Center,
			modifier = Modifier.fillMaxSize()
		) {
			TitleSubtitleLayout(
				displayText = {
					DisplayTextField(
						placeholderText = "Your text...",
						onValueChange = {},
						fontSize = 57
					)
				},
				descriptionText = {
					DisplayTextField(
						placeholderText = "Additional text...",
						onValueChange = {}
					)
				}
			)
		}
	}
}