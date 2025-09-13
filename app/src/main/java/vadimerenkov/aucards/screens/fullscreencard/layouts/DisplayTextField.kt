package vadimerenkov.aucards.screens.fullscreencard.layouts

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import vadimerenkov.aucards.ui.theme.AucardsTheme

@Composable
fun DisplayTextField (
	placeholderText: String,
	onValueChange: (String) -> Unit,
	modifier: Modifier = Modifier,
	textValue: String = "",
	fontSize: Int = 24,
	color: Color = Color.Black,
	interactionSource: MutableInteractionSource? = null,
	imeAction: ImeAction = ImeAction.Default
) {
	TextField(
		value = textValue,
		interactionSource = interactionSource,
		onValueChange = onValueChange,
		placeholder = {
			Text(
				text = placeholderText,
				fontSize = fontSize.sp,
				textAlign = TextAlign.Center,
				lineHeight = (fontSize + 8).sp,
				color = color.copy(alpha = 0.3f),
				modifier = Modifier
					.fillMaxWidth()
			)
		},
		keyboardOptions = KeyboardOptions(
			imeAction = imeAction
		),
		textStyle = MaterialTheme.typography.displayLarge.copy(
			textAlign = TextAlign.Center,
			hyphens = Hyphens.Auto,
			fontSize = fontSize.sp,
			lineHeight = (fontSize + 8).sp
		),
		colors = TextFieldDefaults.colors(
			focusedTextColor = color,
			focusedContainerColor = Color.Transparent,
			unfocusedTextColor = color,
			unfocusedContainerColor = Color.Transparent,
			focusedIndicatorColor = Color.Transparent,
			unfocusedIndicatorColor = Color.Transparent
		),
		modifier = modifier
			.fillMaxWidth()
	)
}

@Preview
@Composable
private fun DisplayTextFieldPreview() {
	AucardsTheme {
		DisplayTextField(
			placeholderText = "Your text...",
			onValueChange = {}
		)
	}
}