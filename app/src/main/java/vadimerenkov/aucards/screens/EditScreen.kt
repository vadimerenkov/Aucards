package vadimerenkov.aucards.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import vadimerenkov.aucards.R
import vadimerenkov.aucards.ViewModelFactory
import vadimerenkov.aucards.data.CardLayout
import vadimerenkov.aucards.ui.ActionButton
import vadimerenkov.aucards.ui.CardViewModel
import vadimerenkov.aucards.ui.ColorPickerPopup
import vadimerenkov.aucards.ui.ContentType
import vadimerenkov.aucards.ui.OpenPopup
import vadimerenkov.aucards.ui.SharedContentStateKey
import vadimerenkov.aucards.ui.Target
import vadimerenkov.aucards.ui.applyIf
import vadimerenkov.aucards.ui.calculateContentColor
import kotlin.math.roundToInt

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
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

	val focusRequester = remember { FocusRequester() }
	val contentColor by animateColorAsState(calculateContentColor(state.aucard.color))
	val keyboardController = LocalSoftwareKeyboardController.current

	BackHandler(state.openPopup != OpenPopup.NONE) {
		viewModel.changePopup(OpenPopup.NONE)
	}

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
		val source2 = remember { MutableInteractionSource() }
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = modifier
				.fillMaxSize()
				.background(state.aucard.color)
				.clickable(
					interactionSource = source,
					indication = null
				) {
					keyboardController?.hide()
					viewModel.changePopup(OpenPopup.NONE)
				}
				.sharedBounds(
					contentState,
					scope
				)
		) {
			AnimatedContent(
				targetState = state.aucard.layout,
				modifier = Modifier
					.weight(1f)
					.statusBarsPadding()
			) {
				when (it) {
					CardLayout.TITLE_SUBTITLE -> {
						Column(
							verticalArrangement = Arrangement.Center,
							horizontalAlignment = Alignment.CenterHorizontally,
							modifier = Modifier
								.scrollable(
									state = rememberScrollState(),
									orientation = Orientation.Vertical
								)
						) {
							TextField(
								value = state.aucard.text,
								interactionSource = viewModel.titleInteractionSource,
								onValueChange = { viewModel.updateText(it) },
								placeholder = {
									Text(
										text = stringResource(R.string.your_text),
										fontSize = state.aucard.titleFontSize.sp,
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
									hyphens = Hyphens.Auto,
									fontSize = state.aucard.titleFontSize.sp,
									lineHeight = (state.aucard.titleFontSize + 8).sp
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
								interactionSource = viewModel.descriptionInteractionSource,
								textStyle = MaterialTheme.typography.titleLarge.copy(
									textAlign = TextAlign.Center,
									hyphens = Hyphens.Auto,
									fontSize = state.aucard.descriptionFontSize.sp,
									lineHeight = (state.aucard.descriptionFontSize + 8).sp
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
										fontSize = state.aucard.descriptionFontSize.sp,
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
					}

					CardLayout.TWO_HALVES -> {
						Column(
							modifier = Modifier
								.fillMaxSize()
						) {
							Box(
								contentAlignment = Alignment.Center,
								modifier = Modifier
									.weight(1f)
							) {
								TextField(
									value = state.aucard.text,
									interactionSource = viewModel.titleInteractionSource,
									onValueChange = { viewModel.updateText(it) },
									placeholder = {
										Text(
											text = stringResource(R.string.your_text),
											fontSize = state.aucard.titleFontSize.sp,
											textAlign = TextAlign.Center,
											lineHeight = (state.aucard.titleFontSize + 8).sp,
											color = contentColor.copy(alpha = 0.3f),
											modifier = Modifier
												.fillMaxWidth()
										)
									},
									keyboardOptions = KeyboardOptions(
										imeAction = ImeAction.Done
									),
									textStyle = MaterialTheme.typography.displayLarge.copy(
										textAlign = TextAlign.Center,
										hyphens = Hyphens.Auto,
										fontSize = state.aucard.titleFontSize.sp,
										lineHeight = (state.aucard.titleFontSize + 8).sp
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
								TextField(
									value = state.aucard.description ?: "",
									onValueChange = { viewModel.updateDescription(it) },
									interactionSource = viewModel.descriptionInteractionSource,
									textStyle = MaterialTheme.typography.titleLarge.copy(
										textAlign = TextAlign.Center,
										hyphens = Hyphens.Auto,
										fontSize = state.aucard.descriptionFontSize.sp,
										lineHeight = (state.aucard.descriptionFontSize + 8).sp
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
											fontSize = state.aucard.descriptionFontSize.sp,
											lineHeight = (state.aucard.descriptionFontSize + 8).sp,
											textAlign = TextAlign.Center,
											color = contentColor.copy(alpha = 0.3f),
											modifier = Modifier
												.fillMaxWidth()
										)
									},
									modifier = Modifier
										.fillMaxWidth()
								)
							}
						}
					}
				}
			}
			Column(
				modifier = Modifier
					.navigationBarsPadding()
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
						interactionSource = source2,
						indication = null
					) {

					}

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
								selectedColor = state.aucard.color,
								onColorSelected = { viewModel.updateColor(it) },
								selectedHexCode = state.hexColor,
								onHexCodeChanged = { viewModel.updateHexCode(it) },
								isHexCodeValid = state.isHexCodeValid,
								contentColor = contentColor
							)
						}
						OpenPopup.FONT_SIZE -> {
							Column(
								horizontalAlignment = Alignment.CenterHorizontally,
								modifier = Modifier
									.padding(16.dp)
							) {
								Text(
									text = "Text size",
									style = MaterialTheme.typography.titleLarge,
									color = MaterialTheme.colorScheme.onBackground
								)
								Slider(
									valueRange = 12f..64f,
									value = state.aucard.titleFontSize.toFloat(),
									colors = SliderDefaults.colors(
										inactiveTrackColor = MaterialTheme.colorScheme.onPrimary
									),
									onValueChange = {
										viewModel.changeTextFontSize(it.roundToInt())
									}
								)
								Slider(
									valueRange = 12f..64f,
									value = state.aucard.descriptionFontSize.toFloat(),
									colors = SliderDefaults.colors(
										inactiveTrackColor = MaterialTheme.colorScheme.onPrimary
									),
									onValueChange = {
										viewModel.changeDescFontSize(it.roundToInt())
									}
								)
							}
						}
						OpenPopup.LAYOUT -> {
							Column(
								horizontalAlignment = Alignment.CenterHorizontally,
								modifier = Modifier
									.padding(16.dp)
							) {
								Text(
									text = "Layout",
									style = MaterialTheme.typography.titleLarge,
									color = MaterialTheme.colorScheme.onBackground
								)
								CardLayout.entries.forEach { layout ->
									Row(
										verticalAlignment = Alignment.CenterVertically,
										modifier = Modifier
											.clickable {
												viewModel.changeLayout(layout)
											}
											.fillMaxWidth()
									) {
										RadioButton(
											selected = state.aucard.layout == layout,
											onClick = {
												viewModel.changeLayout(layout)
											}
										)
										Icon(
											painter = painterResource(layout.icon),
											contentDescription = null,
											tint = MaterialTheme.colorScheme.primary,
											modifier = Modifier
												.applyIf(layout == CardLayout.TWO_HALVES) {
													rotate(90f)
												}
										)
										Spacer(modifier = Modifier.width(8.dp))
										Text(
											text = stringResource(layout.description),
											style = MaterialTheme.typography.titleLarge,
											color = MaterialTheme.colorScheme.onBackground
										)
									}
								}
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
						icon = painterResource(R.drawable.format),
						selected = state.openPopup == OpenPopup.LAYOUT,
						contentDescription = "Layout",
						onClick = {
							viewModel.changePopup(OpenPopup.LAYOUT)
						}
					)
					ActionButton(
						icon = painterResource(R.drawable.font_size),
						selected = state.openPopup == OpenPopup.FONT_SIZE,
						contentDescription = "Choose font size",
						onClick = {
							viewModel.changePopup(OpenPopup.FONT_SIZE)
						}
					)
					ActionButton(
						icon = painterResource(R.drawable.palette),
						selected = state.openPopup == OpenPopup.PALETTE,
						contentDescription = stringResource(R.string.choose_color),
						onClick = {
							viewModel.changePopup(OpenPopup.PALETTE)
						}
					)
				}

				Row(
					horizontalArrangement = Arrangement.SpaceBetween,
					modifier = Modifier
						.fillMaxWidth()
						.padding(bottom = 16.dp)
						.padding(horizontal = 16.dp)
				) {
					ActionButton(
						icon = Icons.Default.Close,
						onClick = onBackClicked,
						contentDescription = stringResource(R.string.cancel)
					)
					ActionButton(
						icon = Icons.Default.Done,
						enabled = state.isValid,
						contentDescription = stringResource(R.string.save),
						tint = if (state.isValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
						onClick = {
							viewModel.saveAucard(state.aucard)
							onBackClicked()
						}
					)
				}
			}
		}
	}
}