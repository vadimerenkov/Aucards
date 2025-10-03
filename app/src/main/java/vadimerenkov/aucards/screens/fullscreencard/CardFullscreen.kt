package vadimerenkov.aucards.screens.fullscreencard

import android.content.pm.ActivityInfo
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import vadimerenkov.aucards.R
import vadimerenkov.aucards.ViewModelFactory
import vadimerenkov.aucards.data.CardLayout
import vadimerenkov.aucards.screens.fullscreencard.layouts.DisplayText
import vadimerenkov.aucards.screens.fullscreencard.layouts.TitleSubtitleLayout
import vadimerenkov.aucards.screens.fullscreencard.layouts.TwoHalvesLayout
import vadimerenkov.aucards.screens.fullscreencard.toolbar.ActionButton
import vadimerenkov.aucards.ui.ContentType
import vadimerenkov.aucards.ui.SharedContentStateKey
import vadimerenkov.aucards.ui.Target
import vadimerenkov.aucards.ui.calculateContentColor

private const val TAG = "CardFullscreen"

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.CardFullscreen(
	onBackClicked: () -> Unit,
	scope: AnimatedVisibilityScope,
	id: Int,
	modifier: Modifier = Modifier,
	viewModel: CardViewModel = viewModel(factory = ViewModelFactory.Factory(id = id))
) {
	val state by viewModel.cardState.collectAsStateWithLifecycle(
		context = Dispatchers.Main.immediate
	)
	val activity = LocalActivity.current
	val context = LocalContext.current

	var controller: WindowInsetsControllerCompat? by remember { mutableStateOf(null) }
	var current_brightness by remember { mutableIntStateOf(0) }


	LaunchedEffect(viewModel.hasPermission(context)) {
		if (viewModel.hasPermission(context)) {
			current_brightness = Settings.System.getInt(
				context.contentResolver,
				Settings.System.SCREEN_BRIGHTNESS
			)

			Settings.System.putInt(
				activity?.contentResolver,
				Settings.System.SCREEN_BRIGHTNESS,
				255
			)
		}
	}

	LaunchedEffect(state.isLandscapeMode) {
		val window = activity?.window
		window?.let {
			controller = WindowCompat.getInsetsController(it, it.decorView)
			controller?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
		}
		controller?.hide(WindowInsetsCompat.Type.systemBars())
		if (state.isLandscapeMode == true) {
			activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
			Log.i(TAG, "Requested landscape mode, setting is ${state.isLandscapeMode}.")
		} else {
			activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
			Log.i(TAG, "Reset back to user preferences, setting is ${state.isLandscapeMode}.")
		}
	}

	DisposableEffect(Unit) {
		onDispose {
			controller?.show(WindowInsetsCompat.Type.systemBars())
			if (viewModel.hasPermission(context)) {
				Settings.System.putInt(
					activity?.contentResolver,
					Settings.System.SCREEN_BRIGHTNESS,
					current_brightness
				)
			}
		}
	}

	val contentColor= calculateContentColor(state.aucard.color)

	val contentState = rememberSharedContentState(
		SharedContentStateKey(
			state.aucard.id,
			ContentType.CARD,
			target = Target.VIEW
		)
	)

	val textContentState = rememberSharedContentState(
		SharedContentStateKey(
			state.aucard.id,
			ContentType.TEXT,
			target = Target.VIEW
		)
	)

	Box(
		contentAlignment = Alignment.Center,
		modifier = modifier
			.sharedBounds(
				sharedContentState = contentState,
				animatedVisibilityScope = scope,
				enter = expandIn(),
				exit = shrinkOut(),
				resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
			)
			.fillMaxSize()
			.background(state.aucard.color)
			.clickable(
				onClick = {
					onBackClicked()
				}
			)

	) {
		val textBackColor = state.aucard.color.copy(alpha = state.aucard.textBackgroundOpacity)
		AsyncImage(
			model = state.aucard.imagePath,
			contentDescription = null,
			modifier = Modifier
				.graphicsLayer {
					with(state.aucard) {
						scaleX = imageScale
						scaleY = imageScale
						rotationZ = imageRotation
					}
				}
				.fillMaxWidth()
				.absoluteOffset {
					state.aucard.imageOffset.round()
				}
		)
		when (state.aucard.layout) {
			CardLayout.TITLE_SUBTITLE -> {
				TitleSubtitleLayout(
					displayText = {
						DisplayText(
							text = state.aucard.text,
							textSize = state.aucard.titleFontSize,
							color = contentColor,
							backgroundColor = textBackColor,
							modifier = Modifier
								.sharedBounds(
									sharedContentState = textContentState,
									animatedVisibilityScope = scope
								)
						)
					},
					descriptionText = {
						state.aucard.description?.let {
							DisplayText(
								text = it,
								textSize = state.aucard.descriptionFontSize,
								color = contentColor,
								backgroundColor = textBackColor,
								lineBreak = LineBreak.Paragraph
							)
						}
					}
				)
			}
			CardLayout.TWO_HALVES -> {
				TwoHalvesLayout(
					contentColor = contentColor,
					displayText = {
						DisplayText(
							text = state.aucard.text,
							textSize = state.aucard.titleFontSize,
							color = contentColor,
							backgroundColor = textBackColor,
							modifier = Modifier
								.sharedBounds(
									sharedContentState = textContentState,
									animatedVisibilityScope = scope
								)
						)
					},
					descriptionText = {
						state.aucard.description?.let {
							DisplayText(
								text = it,
								textSize = state.aucard.descriptionFontSize,
								color = contentColor,
								backgroundColor = textBackColor,
							)
						}
					}
				)
			}
		}
		if (state.isPlaySoundEnabled) {
			AnimatedContent(
				targetState = state.isSoundPlaying,
				transitionSpec = {
					scaleIn() + fadeIn() togetherWith scaleOut() + fadeOut()
				},
				modifier = Modifier
					.align(Alignment.BottomEnd)
					.padding(vertical = 60.dp, horizontal = 30.dp)
			) { isPlaying ->
				ActionButton(
					onClick = {
						if (state.isSoundPlaying) {
							viewModel.soundPlayer.pause()
						} else {
							viewModel.soundPlayer.seekTo(0)
							viewModel.soundPlayer.play()
						}
					},
					icon = if (isPlaying) {
						painterResource(R.drawable.pause)
					} else {
						painterResource(R.drawable.play)
					},
					contentDescription = if (isPlaying) {
						stringResource(R.string.pause)
					} else {
						stringResource(R.string.play)
					},
					iconSize = 72,
					tint = contentColor
				)
			}
		}
	}
}
