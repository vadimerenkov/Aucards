package vadimerenkov.aucards

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import vadimerenkov.aucards.data.Aucard

@Composable
fun CreateStarters(): List<Aucard> {
	return listOf(
		Aucard(
			text = stringResource(R.string.yes),
			color = Color.Green
		),
		Aucard(
			text = stringResource(R.string.no),
			color = Color(-50334)
		),
		Aucard(
			text = stringResource(R.string.help),
			color = Color(-25612)
		),
		Aucard(
			text = stringResource(R.string.hug),
			color = Color.Cyan
		),
		Aucard(
			text = stringResource(R.string.alone),
			description = "If you don't mind",
			color = Color.Yellow
		)
	)
}