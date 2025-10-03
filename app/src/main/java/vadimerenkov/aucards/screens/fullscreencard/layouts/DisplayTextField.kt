package vadimerenkov.aucards.screens.fullscreencard.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vadimerenkov.aucards.ui.theme.AucardsTheme

@Composable
fun DisplayTextField (
	placeholderText: String,
	onValueChange: (String) -> Unit,
	backgroundColor: Color,
	modifier: Modifier = Modifier,
	enabled: Boolean = true,
	textValue: String = "",
	fontSize: Int = 24,
	color: Color = Color.Black,
	interactionSource: MutableInteractionSource? = null,
	imeAction: ImeAction = ImeAction.Default
) {
	BasicTextField(
		value = textValue,
		onValueChange = onValueChange,
		interactionSource = interactionSource,
		enabled = enabled,
		textStyle = MaterialTheme.typography.displayLarge.copy(
			color = color,
			textAlign = TextAlign.Center,
			hyphens = Hyphens.Auto,
			fontSize = fontSize.sp,
			lineHeight = (fontSize + 8).sp
		),
		keyboardOptions = KeyboardOptions(
			imeAction = imeAction
		),
		cursorBrush = SolidColor(color.copy(alpha = 0.5f)),
		decorationBox = { field ->
			Box(
				contentAlignment = Alignment.TopCenter,
				modifier = Modifier
					.background(backgroundColor)
					.padding(8.dp)

			) {
				if (textValue.isEmpty()) {
					Text(
						text = placeholderText,
						fontSize = fontSize.sp,
						textAlign = TextAlign.Center,
						lineHeight = (fontSize + 8).sp,
						color = color.copy(alpha = 0.3f)
					)
				}
				field()
			}
		},
		modifier = modifier
	)
}

@Preview
@Composable
private fun DisplayTextFieldPreview() {
	AucardsTheme {
		DisplayTextField(
			placeholderText = "Your text...",
			onValueChange = {},
			backgroundColor = Color.White
		)
	}
}