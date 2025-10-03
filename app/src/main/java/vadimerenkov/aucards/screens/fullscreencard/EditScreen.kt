package vadimerenkov.aucards.screens.fullscreencard

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import vadimerenkov.aucards.R
import vadimerenkov.aucards.ViewModelFactory
import vadimerenkov.aucards.data.CardLayout
import vadimerenkov.aucards.screens.fullscreencard.layouts.DisplayTextField
import vadimerenkov.aucards.screens.fullscreencard.layouts.TitleSubtitleLayout
import vadimerenkov.aucards.screens.fullscreencard.layouts.TwoHalvesLayout
import vadimerenkov.aucards.screens.fullscreencard.toolbar.Toolbar
import vadimerenkov.aucards.ui.ContentType
import vadimerenkov.aucards.ui.SharedContentStateKey
import vadimerenkov.aucards.ui.Target
import vadimerenkov.aucards.ui.calculateContentColor

private const val TAG = "EditScreen"

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.EditScreen(
	isDarkTheme: Boolean,
	onBackClicked: () -> Unit,
	scope: AnimatedVisibilityScope,
	id: Int,
	index: Int?,
	isWideScreen: Boolean,
	modifier: Modifier = Modifier,
	viewModel: CardViewModel = viewModel(factory = ViewModelFactory.Factory(isDarkTheme, id, index))
) {
	val state by viewModel.cardState.collectAsStateWithLifecycle()

	val focusRequester = remember { FocusRequester() }
	val contentColor by animateColorAsState(calculateContentColor(state.aucard.color))
	val keyboardController = LocalSoftwareKeyboardController.current

	BackHandler(
		enabled = state.openPopup != OpenPopup.NONE
				|| state.isEditingImage
	) {
		viewModel.onAction(CardAction.PopupChanged(OpenPopup.NONE))
		viewModel.selectImage(false)
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

	val source = remember { MutableInteractionSource() }
	val source2 = remember { MutableInteractionSource() }

	Box(
		contentAlignment = Alignment.Center,
		modifier = modifier
			.fillMaxSize()
			.background(state.aucard.color)
			.clickable(
				interactionSource = source,
				indication = null
			) {
				keyboardController?.hide()
				focusRequester.freeFocus()
				viewModel.onAction(CardAction.PopupChanged(OpenPopup.NONE))
				viewModel.selectImage(false)
			}
			.sharedBounds(
				contentState,
				scope
			)
	) {
		val imageState = rememberTransformableState { zoom, pan, spin ->
			if (state.isEditingImage) {
				viewModel.transformImage(zoom, pan, spin)
			}
		}
		state.aucard.imagePath?.let {
			AsyncImage(
				model = it,
				contentDescription = null,
				modifier = Modifier
					.graphicsLayer {
						with(state.aucard) {
							scaleX = imageScale
							scaleY = imageScale
							rotationZ = imageRotation
							translationX = imageOffset.x
							translationY = imageOffset.y
						}
					}
					.zIndex(
						if (state.isEditingImage) 1f else 0f
					)
					.fillMaxWidth()
					.transformable(imageState)
					.combinedClickable(
						interactionSource = null,
						indication = null,
						onLongClick = {
							viewModel.selectImage(true)
							viewModel.onAction(CardAction.PopupChanged(OpenPopup.IMAGE))
							keyboardController?.hide()
						},
						onClick = {
							viewModel.selectImage(false)
							if (state.openPopup != OpenPopup.IMAGE) {
								viewModel.onAction(CardAction.PopupChanged(OpenPopup.NONE))
							}
							keyboardController?.hide()
						}
					)
					.border(
						width = 2.dp,
						color = if (state.isEditingImage) Color.Black else Color.Transparent
					)
					.padding(2.dp)
					.border(
						width = 2.dp,
						color = if (state.isEditingImage) Color.White else Color.Transparent
					)
					.alpha(
						if (state.isEditingImage) 0.7f else 1f
					)
			)
		}
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = modifier
				.fillMaxSize()
		) {
			AnimatedContent(
				contentAlignment = Alignment.Center,
				targetState = state.aucard.layout,
				modifier = Modifier
					.weight(1f)
					.statusBarsPadding()
			) { layout ->
				val textBackColor = state.aucard.color.copy(
					alpha = state.aucard.textBackgroundOpacity
				)
				when (layout) {
					CardLayout.TITLE_SUBTITLE -> {
						TitleSubtitleLayout(
							displayText = {
								DisplayTextField(
									textValue = state.aucard.text,
									onValueChange = {
										viewModel.updateText(it)
									},
									placeholderText = stringResource(R.string.your_text),
									enabled = !state.isEditingImage,
									fontSize = state.aucard.titleFontSize,
									color = contentColor,
									interactionSource = viewModel.titleInteractionSource,
									imeAction = ImeAction.Done,
									backgroundColor = textBackColor,
									modifier = Modifier
										.focusRequester(focusRequester)
										.testTag("TextField")
										.sharedBounds(
											sharedContentState = textState,
											animatedVisibilityScope = scope
										)
								)
							},
							descriptionText = {
								DisplayTextField(
									textValue = state.aucard.description ?: "",
									fontSize = state.aucard.descriptionFontSize,
									enabled = !state.isEditingImage,
									onValueChange = {
										viewModel.updateDescription(it)
									},
									placeholderText = stringResource(R.string.description),
									color = contentColor,
									backgroundColor = textBackColor,
									interactionSource = viewModel.descriptionInteractionSource
								)
							}
						)
					}

					CardLayout.TWO_HALVES -> {
						TwoHalvesLayout(
							contentColor = contentColor,
							displayText = {
								DisplayTextField(
									textValue = state.aucard.text,
									onValueChange = {
										viewModel.updateText(it)
									},
									enabled = !state.isEditingImage,
									placeholderText = stringResource(R.string.your_text),
									fontSize = state.aucard.titleFontSize,
									color = contentColor,
									interactionSource = viewModel.titleInteractionSource,
									imeAction = ImeAction.Done,
									backgroundColor = textBackColor,
									modifier = Modifier
										.focusRequester(focusRequester)
										.testTag("TextField")
										.sharedBounds(
											sharedContentState = textState,
											animatedVisibilityScope = scope
										)
								)
							},
							descriptionText = {
								DisplayTextField(
									textValue = state.aucard.description ?: "",
									fontSize = state.aucard.descriptionFontSize,
									enabled = !state.isEditingImage,
									onValueChange = {
										viewModel.updateDescription(it)
									},
									placeholderText = stringResource(R.string.description),
									color = contentColor,
									backgroundColor = textBackColor,
									interactionSource = viewModel.descriptionInteractionSource
								)
							}
						)
					}
				}
			}
		}
		Toolbar(
			state = state,
			contentColor = contentColor,
			clickStealer = source2,
			onAction = viewModel::onAction,
			onBackClicked = onBackClicked,
			isWideScreen = isWideScreen,
			modifier = Modifier
				.align(Alignment.BottomCenter)
				.alpha(if (state.isEditingImage) 0.6f else 1f)
		)
	}
}