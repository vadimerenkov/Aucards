package vadimerenkov.aucards.screens.fullscreencard.layouts

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import vadimerenkov.aucards.ui.theme.AucardsTheme

@Composable
fun DisplayText(
	text: String,
	textSize: Int,
	color: Color,
	modifier: Modifier = Modifier,
	lineBreak: LineBreak = LineBreak.Heading
) {
	var lines by remember { mutableIntStateOf(0) }
	Text(
		text = text,
		color = color,
		fontSize = textSize.sp,
		style = MaterialTheme.typography.titleLarge.copy(
			hyphens = Hyphens.Auto,
			lineBreak = lineBreak,
			lineHeight = (textSize + 8).sp
		),
		textAlign = if (lines > 2 && lineBreak == LineBreak.Paragraph) TextAlign.Start else TextAlign.Center,
		onTextLayout = {
			lines = it.lineCount
		},
		modifier = modifier
	)
}

@Preview
@Composable
private fun DisplayTextPreview() {
	AucardsTheme {
		DisplayText(
			text = "Display text",
			textSize = 48,
			color = Color.Black
		)
	}
}