package vadimerenkov.aucards.screens.fullscreencard.toolbar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import vadimerenkov.aucards.R
import vadimerenkov.aucards.screens.fullscreencard.CardAction

@Composable
fun ImagePopup(
	onAction: (CardAction) -> Unit,
	sliderValue: Float,
	launchImagePicker: () -> Unit,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier
	) {
		Row {
			IconButton(
				onClick = {
					launchImagePicker()
				}
			) {
				Icon(
					painter = painterResource(R.drawable.replace),
					contentDescription = null
				)
			}
			IconButton(
				onClick = {
					onAction(CardAction.ImageUriChanged(null))
				}
			) {
				Icon(
					imageVector = Icons.Default.Clear,
					contentDescription = null
				)
			}
		}
		Slider(
			value = sliderValue,
			onValueChange = {
				onAction(CardAction.TextBackgroundChanged(it))
			}
		)
	}
}