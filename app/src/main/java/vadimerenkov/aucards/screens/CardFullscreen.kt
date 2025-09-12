package vadimerenkov.aucards.screens

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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.Dispatchers
import vadimerenkov.aucards.R
import vadimerenkov.aucards.ViewModelFactory
import vadimerenkov.aucards.data.CardLayout
import vadimerenkov.aucards.ui.CardViewModel
import vadimerenkov.aucards.ui.ContentType
import vadimerenkov.aucards.ui.SharedContentStateKey
import vadimerenkov.aucards.ui.Target
import vadimerenkov.aucards.ui.calculateContentColor

private const val TAG = "CardFullscreen"

@UnstableApi
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
	val soundPlayer = remember { ExoPlayer.Builder(context).build() }
	var soundPlaying by remember { mutableStateOf(false) }

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

	LaunchedEffect(state.isPlaySoundEnabled) {
		if (state.isPlaySoundEnabled && state.ringtoneUri != null) {
			val sound = MediaItem.fromUri(state.ringtoneUri!!)
			soundPlayer.addListener(
				object : Player.Listener {
					override fun onIsPlayingChanged(isPlaying: Boolean) {
						super.onIsPlayingChanged(isPlaying)
						soundPlaying = isPlaying
					}
				}
			)
			soundPlayer.setMediaItem(sound)
			soundPlayer.prepare()
			soundPlayer.play()
		}
	}

	DisposableEffect(Unit) {
		onDispose {
			controller?.show(WindowInsetsCompat.Type.systemBars())
			soundPlayer.stop()
			soundPlayer.release()
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
		when (state.aucard.layout) {
			CardLayout.TITLE_SUBTITLE -> {
				Column(
					verticalArrangement = Arrangement.Center,
					horizontalAlignment = Alignment.CenterHorizontally,
					modifier = Modifier
						.align(Alignment.Center)
						.padding(24.dp)
				) {
					Text(
						text = state.aucard.text,
						color = contentColor,
						fontSize = state.aucard.titleFontSize.sp,
						style = MaterialTheme.typography.displayLarge.copy(
							hyphens = Hyphens.Auto,
							lineBreak = LineBreak.Heading,
							lineHeight = (state.aucard.titleFontSize + 8).sp
						),
						textAlign = TextAlign.Center,
						modifier = Modifier
							.sharedBounds(
								sharedContentState = textContentState,
								animatedVisibilityScope = scope
							)
					)
					if (state.aucard.description != null) {
						var lines by remember { mutableIntStateOf(0) }
						Spacer(modifier = Modifier.height(24.dp))
						Text(
							text = state.aucard.description!!,
							color = contentColor,
							fontSize = state.aucard.descriptionFontSize.sp,
							style = MaterialTheme.typography.titleLarge.copy(
								hyphens = Hyphens.Auto,
								lineBreak = LineBreak.Paragraph,
								lineHeight = (state.aucard.descriptionFontSize + 8).sp
							),
							textAlign = if (lines > 2) TextAlign.Start else TextAlign.Center,
							onTextLayout = {
								lines = it.lineCount
							}
						)
					}
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
						Text(
							text = state.aucard.text,
							color = contentColor,
							fontSize = state.aucard.titleFontSize.sp,
							style = MaterialTheme.typography.displayLarge.copy(
								hyphens = Hyphens.Auto,
								lineBreak = LineBreak.Heading,
								lineHeight = (state.aucard.titleFontSize + 8).sp
							),
							textAlign = TextAlign.Center,
							modifier = Modifier
								.sharedBounds(
									sharedContentState = textContentState,
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
						if (state.aucard.description != null) {
							Text(
								text = state.aucard.description!!,
								color = contentColor,
								fontSize = state.aucard.descriptionFontSize.sp,
								style = MaterialTheme.typography.titleLarge.copy(
									hyphens = Hyphens.Auto,
									lineBreak = LineBreak.Heading,
									lineHeight = (state.aucard.descriptionFontSize + 8).sp
								),
								textAlign = TextAlign.Center
							)
						}
					}
				}
			}
		}
		if (state.isPlaySoundEnabled && state.ringtoneUri != null) {
			Box(
				contentAlignment = Alignment.Center,
				modifier = Modifier
					.align(Alignment.BottomEnd)
					.padding(vertical = 60.dp, horizontal = 30.dp)
					.clip(CircleShape)
					.clickable(
						onClickLabel = "Play sound"
					) {
						if (soundPlaying) {
							soundPlayer.pause()
						} else {
							soundPlayer.seekTo(0)
							soundPlayer.play()
						}
					}
					.size(100.dp)

			) {
				AnimatedContent(
					targetState = soundPlaying,
					transitionSpec = {
						scaleIn() + fadeIn() togetherWith scaleOut() + fadeOut()
					}
				) { isPlaying ->
					Icon(
						painter = if (isPlaying) {
							painterResource(R.drawable.pause)
						} else {
							painterResource(R.drawable.play)
						},
						contentDescription = if (isPlaying) {
							stringResource(R.string.pause)
						} else {
							stringResource(R.string.play)
						},
						tint = contentColor.copy(alpha = 0.8f),
						modifier = Modifier
							.fillMaxSize(0.7f)

					)
				}
			}
		}
	}
}
