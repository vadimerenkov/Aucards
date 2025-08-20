package vadimerenkov.aucards.screens

import android.content.pm.ActivityInfo
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import vadimerenkov.aucards.ViewModelFactory
import vadimerenkov.aucards.ui.CardViewModel
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

	var controller: WindowInsetsControllerCompat? = null
	val window = LocalActivity.current?.window
	if (window != null) {
		controller = WindowCompat.getInsetsController(window, window.decorView)
		controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
	}

	fun hasPermission(): Boolean {
		return Settings.System.canWrite(context) && state.isMaxBrightness
	}

	val current_brightness = Settings.System.getInt(
		context.contentResolver,
		Settings.System.SCREEN_BRIGHTNESS
	)

	LaunchedEffect(state.isLandscapeMode) {
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
			if (hasPermission()) {
				Settings.System.putInt(
					activity?.contentResolver,
					Settings.System.SCREEN_BRIGHTNESS,
					current_brightness
				)
			}
		}
	}

	if (hasPermission()) {
		Settings.System.putInt(
			activity?.contentResolver,
			Settings.System.SCREEN_BRIGHTNESS,
			255
		)
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

	with(scope) {
		Box(
			modifier = modifier
				.fillMaxSize()
				.background(state.aucard.color)
				.clickable(
					onClick = {
						onBackClicked()
					}
				)
				.sharedBounds(
					sharedContentState = contentState,
					animatedVisibilityScope = scope,
					enter = expandIn(),
					exit = shrinkOut(),
					resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
				)
		) {
			Column(
				verticalArrangement = Arrangement.Center,
				horizontalAlignment = Alignment.CenterHorizontally,
				modifier = Modifier
					.align(Alignment.Center)
					.padding(16.dp)
			) {
				Text(
					text = state.aucard.text,
					color = contentColor,
					style = MaterialTheme.typography.displayLarge.copy(
						hyphens = Hyphens.Auto
					),
					textAlign = TextAlign.Center,
					modifier = Modifier
						.sharedBounds(
							sharedContentState = textContentState,
							animatedVisibilityScope = scope
						)
				)
				state.aucard.description?.let {
					AnimatedVisibility(!this@with.transition.isRunning) {
						Text(
							text = it,
							color = contentColor,
							style = MaterialTheme.typography.titleLarge.copy(
								hyphens = Hyphens.Auto
							),
							textAlign = TextAlign.Justify
						)
					}
				}
			}
		}
	}
}