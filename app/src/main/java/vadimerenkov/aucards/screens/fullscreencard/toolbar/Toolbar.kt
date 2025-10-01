package vadimerenkov.aucards.screens.fullscreencard.toolbar

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vadimerenkov.aucards.R
import vadimerenkov.aucards.data.Aucard
import vadimerenkov.aucards.screens.fullscreencard.CardAction
import vadimerenkov.aucards.screens.fullscreencard.CardState
import vadimerenkov.aucards.screens.fullscreencard.OpenPopup
import vadimerenkov.aucards.ui.theme.AucardsTheme

private const val TAG = "Toolbar"

@Composable
fun Toolbar(
	state: CardState,
	contentColor: Color,
	clickStealer: MutableInteractionSource,
	onAction: (CardAction) -> Unit,
	onBackClicked: () -> Unit,
	modifier: Modifier = Modifier,
	isWideScreen: Boolean = false
) {
	val imagePicker =
		rememberLauncherForActivityResult(PickVisualMedia()) {
			if (it != null) {
				onAction(CardAction.ImageUriChanged(it))
			}
		}

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = modifier
			.dropShadow(
				shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
				shadow = Shadow(
					radius = 20.dp,
					color = Color.Gray
				)
			)
			.clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
			.background(
				color = MaterialTheme.colorScheme.primaryContainer
			)
			.clickable(
				interactionSource = clickStealer,
				indication = null
			) {

			}
			.navigationBarsPadding()

	) {
		AnimatedContent(
			targetState = state.openPopup,
			transitionSpec = {
				fadeIn() togetherWith fadeOut()
			}
		) { it ->

			when (it) {
				OpenPopup.NONE -> {
					Spacer(modifier = Modifier.height(16.dp))
				}
				OpenPopup.PALETTE -> {
					ColorPickerPopup(
						onAction = onAction,
						selectedColor = state.aucard.color,
						selectedHexCode = state.hexColor,
						isHexCodeValid = state.isHexCodeValid,
						contentColor = contentColor,
						isWideScreen = isWideScreen
					)
				}
				OpenPopup.FONT_SIZE -> {
					FontSizePopup(
						onAction = onAction,
						textSizeValue = state.aucard.titleFontSize.toFloat(),
						descSizeValue = state.aucard.descriptionFontSize.toFloat(),
						modifier = Modifier
							.widthIn(max = 600.dp)
					)
				}
				OpenPopup.LAYOUT -> {
					LayoutPopup(
						onAction = onAction,
						selectedLayout = state.aucard.layout,
						modifier = Modifier
							.widthIn(max = 600.dp)
					)
				}
				OpenPopup.IMAGE -> {
					Column {
						Row {
							IconButton(
								onClick = {
									imagePicker.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
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
							value = state.aucard.textBackgroundOpacity,
							onValueChange = {
								onAction(CardAction.TextBackgroundChanged(it))
							}
						)
					}
				}
			}
		}

		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.padding(horizontal = 16.dp)
		) {
			Spacer(modifier = Modifier.weight(1f))
			ActionButton(
				icon = painterResource(R.drawable.picture),
				selected = state.openPopup == OpenPopup.IMAGE,
				contentDescription = "Choose image",
				onClick = {
					onAction(CardAction.PopupChanged(OpenPopup.IMAGE))
					if (state.aucard.imagePath == null) {
						imagePicker.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
					}
				}
			)
			ActionButton(
				icon = painterResource(R.drawable.format),
				selected = state.openPopup == OpenPopup.LAYOUT,
				contentDescription = stringResource(R.string.layout),
				onClick = {
					onAction(CardAction.PopupChanged(OpenPopup.LAYOUT))
				}
			)
			ActionButton(
				icon = painterResource(R.drawable.font_size),
				selected = state.openPopup == OpenPopup.FONT_SIZE,
				contentDescription = stringResource(R.string.text_size),
				onClick = {
					onAction(CardAction.PopupChanged(OpenPopup.FONT_SIZE))
				}
			)
			ActionButton(
				icon = painterResource(R.drawable.palette),
				selected = state.openPopup == OpenPopup.PALETTE,
				contentDescription = stringResource(R.string.choose_color),
				onClick = {
					onAction(CardAction.PopupChanged(OpenPopup.PALETTE))
				}
			)
		}

		Row(
			horizontalArrangement = Arrangement.SpaceBetween,
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp)
		) {
			ActionButton(
				icon = Icons.Default.Close,
				onClick = {
					onBackClicked()
				},
				contentDescription = stringResource(R.string.cancel)
			)
			ActionButton(
				icon = Icons.Default.Done,
				enabled = state.isValid,
				contentDescription = stringResource(R.string.save),
				tint = if (state.isValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
				onClick = {
					onAction(CardAction.Saved(state.aucard))
					onBackClicked()
				}
			)
		}
	}
}

@Preview
@Composable
private fun ToolbarPreview() {
	AucardsTheme {
		Toolbar(
			state = CardState(
				aucard = Aucard(text = ""),
				openPopup = OpenPopup.IMAGE
			),
			onAction = {},
			onBackClicked = {},
			contentColor = Color.Black,
			clickStealer = remember { MutableInteractionSource() }
		)
	}
}