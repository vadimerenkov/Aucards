package vadimerenkov.aucards.screens.fullscreencard.toolbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
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
			.padding(16.dp)
	) {
		Row(
			horizontalArrangement = Arrangement.SpaceEvenly,
			modifier = Modifier
				.fillMaxWidth()
		) {
			OutlinedButton(
				onClick = {
					launchImagePicker()
				}
			) {
				Icon(
					painter = painterResource(R.drawable.replace),
					contentDescription = null
				)
				Spacer(modifier = Modifier.width(4.dp))
				Text(
					text = "Change image"
				)
			}
			OutlinedButton(
				onClick = {
					onAction(CardAction.ImageUriChanged(null))
				}
			) {
				Icon(
					imageVector = Icons.Default.Clear,
					contentDescription = null
				)
				Spacer(modifier = Modifier.width(4.dp))
				Text(
					text = "Remove image"
				)
			}
		}
		Spacer(modifier = Modifier.height(16.dp))
		Text(
			text = "Text background opacity:",
			color = MaterialTheme.colorScheme.onBackground
		)
		Spacer(modifier = Modifier.height(8.dp))
		Slider(
			value = sliderValue,
			onValueChange = {
				onAction(CardAction.TextBackgroundChanged(it))
			},
			colors = SliderDefaults.colors(
				inactiveTrackColor = MaterialTheme.colorScheme.onPrimary
			)
		)
	}
}