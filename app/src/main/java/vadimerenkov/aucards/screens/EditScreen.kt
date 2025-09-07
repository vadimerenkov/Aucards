package vadimerenkov.aucards.screens

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import vadimerenkov.aucards.R
import vadimerenkov.aucards.ViewModelFactory
import vadimerenkov.aucards.ui.CardViewModel
import vadimerenkov.aucards.ui.ColorPickerPopup
import vadimerenkov.aucards.ui.ContentType
import vadimerenkov.aucards.ui.SharedContentStateKey
import vadimerenkov.aucards.ui.Target
import vadimerenkov.aucards.ui.calculateContentColor

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.EditScreen(
	isDarkTheme: Boolean,
	onBackClicked: () -> Unit,
	scope: AnimatedVisibilityScope,
	id: Int,
	index: Int?,
	modifier: Modifier = Modifier,
	viewModel: CardViewModel = viewModel(factory = ViewModelFactory.Factory(isDarkTheme, id, index))
) {
	val state by viewModel.cardState.collectAsStateWithLifecycle()

	var colorPaletteOpen by remember { mutableStateOf(false) }
	val focusRequester = remember { FocusRequester() }
	val contentColor by animateColorAsState(calculateContentColor(state.aucard.color))
	val keyboardController = LocalSoftwareKeyboardController.current

	LaunchedEffect(true) {
		focusRequester.requestFocus()
	}

	val contentState = rememberSharedContentState(
		SharedContentStateKey(
			state.aucard.id,
			ContentType.CARD,
			Target.EDIT
		)
	)

	val textState = rememberSharedContentState(
		SharedContentStateKey(
			state.aucard.id,
			ContentType.TEXT,
			Target.EDIT
		)
	)

	with(scope) {
		val source = remember { MutableInteractionSource() }
		Box(
			modifier = modifier
				.fillMaxSize()
				.background(state.aucard.color)
				.clickable(
					interactionSource = source,
					indication = null
				) {
					keyboardController?.hide()
				}
				.sharedBounds(
					contentState,
					scope
				)
		) {
			Column(
				verticalArrangement = Arrangement.Center,
				horizontalAlignment = Alignment.CenterHorizontally,
				modifier = Modifier
					.align(Alignment.Center)
					.scrollable(
						state = rememberScrollState(),
						orientation = Orientation.Vertical
					)
			) {
				TextField(
					value = state.aucard.text,
					onValueChange = { viewModel.updateText(it) },
					placeholder = {
						Text(
							text = stringResource(R.string.your_text),
							style = MaterialTheme.typography.displayLarge,
							textAlign = TextAlign.Center,
							color = contentColor.copy(alpha = 0.3f),
							modifier = Modifier
								.align(Alignment.CenterHorizontally)
								.fillMaxWidth()
						)
					},
					keyboardOptions = KeyboardOptions(
						imeAction = ImeAction.Done
					),
					textStyle = MaterialTheme.typography.displayLarge.copy(
						textAlign = TextAlign.Center,
						hyphens = Hyphens.Auto
					),
					colors = TextFieldDefaults.colors(
						focusedTextColor = contentColor,
						focusedContainerColor = Color.Transparent,
						unfocusedTextColor = contentColor,
						unfocusedContainerColor = Color.Transparent,
						focusedIndicatorColor = Color.Transparent,
						unfocusedIndicatorColor = Color.Transparent
					),
					modifier = Modifier
						.focusRequester(focusRequester)
						.testTag("TextField")
						.sharedBounds(
							sharedContentState = textState,
							animatedVisibilityScope = scope
						)
				)
				TextField(
					value = state.aucard.description ?: "",
					onValueChange = { viewModel.updateDescription(it) },
					textStyle = MaterialTheme.typography.titleLarge.copy(
						textAlign = TextAlign.Center,
						hyphens = Hyphens.Auto
					),
					colors = TextFieldDefaults.colors(
						focusedTextColor = contentColor,
						focusedContainerColor = Color.Transparent,
						unfocusedTextColor = contentColor,
						unfocusedContainerColor = Color.Transparent,
						focusedIndicatorColor = Color.Transparent,
						unfocusedIndicatorColor = Color.Transparent
					),
					placeholder = {
						Text(
							text = stringResource(R.string.description),
							style = MaterialTheme.typography.titleLarge,
							textAlign = TextAlign.Center,
							color = contentColor.copy(alpha = 0.3f),
							modifier = Modifier
								.align(Alignment.CenterHorizontally)
								.fillMaxWidth()
						)
					},
					modifier = Modifier
						.fillMaxWidth()
				)
			}
			Column(
				modifier = Modifier
					.align(Alignment.BottomCenter)
					.clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
					.background(
						color = Color.Black.copy(alpha = 0.4f)
					)
					.padding(horizontal = 24.dp)
			) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier
						.padding(top = 16.dp)

				) {
					Spacer(modifier = Modifier.weight(1f))
					Box(
						contentAlignment = Alignment.Center,
						modifier = Modifier
							.clip(CircleShape)
							.size(70.dp)
							.clickable {
								keyboardController?.hide()
								colorPaletteOpen = !colorPaletteOpen
							}
						) {
							Icon(
								painter = painterResource(R.drawable.palette),
								tint = Color.White,
								contentDescription = stringResource(R.string.choose_color),
								modifier = Modifier
									.fillMaxSize(0.7f)
							)
						}

						ColorPickerPopup(
							isOpen = colorPaletteOpen,
							onDismissRequest = { colorPaletteOpen = false },
							selectedColor = state.aucard.color,
							onColorSelected = { viewModel.updateColor(it) },
							selectedHexCode = state.hexColor,
							onHexCodeChanged = { viewModel.updateHexCode(it) },
							isHexCodeValid = state.isHexCodeValid,
							contentColor = contentColor,
							offset = DpOffset(-(50).dp, 0.dp),
							tabRowSize = DpSize(250.dp, 50.dp)
						)
					}

				Row(
					horizontalArrangement = Arrangement.SpaceBetween,
					modifier = Modifier
						.fillMaxWidth()
						.navigationBarsPadding()
						.padding(bottom = 16.dp)
				) {
					Box(
						contentAlignment = Alignment.Center,
						modifier = Modifier
							.clip(CircleShape)
							.clickable {
								onBackClicked()
							}
							.size(70.dp)
					) {
						Icon(
							imageVector = Icons.Default.Close,
							contentDescription = "Cancel",
							tint = Color.White,
							modifier = Modifier
								.fillMaxSize(0.7f)
						)
					}
					Box(
						contentAlignment = Alignment.Center,
						modifier = Modifier
							.clip(CircleShape)
							.clickable(enabled = state.isValid) {
								viewModel.saveAucard(state.aucard)
								onBackClicked()
							}
							.size(70.dp)
					) {
						Icon(
							imageVector = Icons.Default.Done,
							contentDescription = stringResource(R.string.save),
							tint = if (state.isValid) Color.White else Color.White.copy(alpha = 0.3f),
							modifier = Modifier
								.fillMaxSize(0.7f)
						)
					}
				}
			}
		}
	}
}