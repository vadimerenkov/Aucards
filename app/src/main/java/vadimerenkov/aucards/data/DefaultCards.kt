package vadimerenkov.aucards.data

import androidx.compose.ui.graphics.Color

object DefaultCards {

	val defaultCards = listOf<Aucard>(
		Aucard(
			text = "I love you",
			color = Color.Red
		),
		Aucard(
			text = "No",
			color = Color.Yellow
		),
		Aucard(
			text = "Yes",
			color = Color.Green
		),
		Aucard(
			text = "I need some rest right now",
			title = "Rest",
			color = Color.Cyan
		),
		Aucard(
			text = "Can you hug me?"
		)
	)
}