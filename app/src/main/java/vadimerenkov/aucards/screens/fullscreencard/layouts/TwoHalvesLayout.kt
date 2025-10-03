package vadimerenkov.aucards.screens.fullscreencard.layouts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vadimerenkov.aucards.ui.theme.AucardsTheme

@Composable
fun TwoHalvesLayout(
	contentColor: Color,
	displayText: @Composable () -> Unit,
	modifier: Modifier = Modifier,
	descriptionText: @Composable (() -> Unit)? = null
) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = modifier
			.fillMaxSize()
	) {
		Box(
			contentAlignment = Alignment.Center,
			modifier = Modifier
				.weight(1f)
		) {
			displayText()
		}
		HorizontalDivider(
			thickness = 8.dp,
			color = contentColor.copy(alpha = 0.5f)
		)
		Box(
			contentAlignment = Alignment.Center,
			modifier = Modifier
				.weight(1f)
		) {
			descriptionText?.invoke()
		}
	}
}

@Preview
@Composable
private fun TwoHalvesTextPreview() {
	AucardsTheme {
		TwoHalvesLayout(
			contentColor = Color.Black,
			displayText = {
				DisplayText(
					text = "Display text",
					textSize = 57,
					color = Color.Black,
					backgroundColor = Color.White
				)
			},
			descriptionText = {
				DisplayText(
					text = "Description text",
					textSize = 57,
					color = Color.Black,
					backgroundColor = Color.White
				)
			}
		)
	}
}

@Preview
@Composable
private fun TwoHalvesFieldPreview() {
	AucardsTheme {
		TwoHalvesLayout(
			contentColor = Color.Black,
			displayText = {
				DisplayTextField(
					placeholderText = "Your text...",
					onValueChange = {},
					fontSize = 57,
					backgroundColor = Color.White
				)
			},
			descriptionText = {
				DisplayTextField(
					placeholderText = "Additional text...",
					fontSize = 57,
					onValueChange = {},
					backgroundColor = Color.White
				)
			}
		)
	}
}