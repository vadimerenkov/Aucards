package vadimerenkov.aucards.screens

import android.content.pm.ActivityInfo
import android.provider.Settings
import android.graphics.Color.toArgb
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.VectorConverter
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.colorspace.ColorSpace
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
//import vadimerenkov.aucards.CreateStarters
import vadimerenkov.aucards.R
import vadimerenkov.aucards.ViewModelFactory
import vadimerenkov.aucards.data.Aucard
import vadimerenkov.aucards.ui.CardState
import vadimerenkov.aucards.ui.CardViewModel

private const val TAG = "CardFullscreen"

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CardFullscreen(
	onBackClicked: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: CardViewModel = viewModel(factory = ViewModelFactory.Factory)
) {
	val state by viewModel.cardState.collectAsState()
	val keyboardController = LocalSoftwareKeyboardController.current
	val activity = LocalActivity.current

	val context = LocalContext.current
	fun hasPermission(): Boolean {
		return Settings.System.canWrite(context) && state.isMaxBrightness
	}

	val current_brightness = Settings.System.getInt(
		context.contentResolver,
		Settings.System.SCREEN_BRIGHTNESS
	)

	DisposableEffect(Unit) {
		onDispose {
			if (hasPermission()) {
				Settings.System.putInt(
					activity?.contentResolver,
					Settings.System.SCREEN_BRIGHTNESS,
					current_brightness
				)
			}
		}
	}

	if (state.isLandscapeMode == true) {
		activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
		Log.i(TAG, "Requested landscape mode, setting is ${state.isLandscapeMode}.")
	}
	else {
		activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
		Log.i(TAG, "Reset back to user preferences, setting is ${state.isLandscapeMode}.")
	}

	if (hasPermission()) {
		Settings.System.putInt(
			activity?.contentResolver,
			Settings.System.SCREEN_BRIGHTNESS,
			255
		)
		Log.i(TAG, "Brightness set to maximum")
	} else {
		Log.i(TAG, "Permission not granted to change brightness")
	}

	Surface(
		color = state.aucard.color,
		modifier = modifier
			.fillMaxSize()
			.clickable(
				enabled = if (state.isEditable) WindowInsets.isImeVisible else true,
				onClick = {
					keyboardController?.hide()
					if (!state.isEditable) {
						onBackClicked()
					}
				}
			)
	) {
		if (state.isEditable) {
			EditScreen(
				state = state,
				onTextChange = { viewModel.UpdateState(state.aucard.copy(text = it)) },
				onDescriptionChange = { viewModel.UpdateState(state.aucard.copy(description = it)) },
				onSaveClicked = {
					viewModel.SaveAucard(it)
					onBackClicked()
				},
				onCancelClicked = onBackClicked,
				onColorChange = { viewModel.UpdateState(state.aucard.copy(color = it)) },
				requestKeyboardClose = {
					keyboardController?.hide()
				}
			)
		}
		else {
			ViewScreen(state)
		}
	}
}

@Composable
private fun EditScreen(
	state: CardState,
	onTextChange: (String) -> Unit,
	onDescriptionChange: (String) -> Unit,
	onColorChange: (Color) -> Unit,
	onSaveClicked: (Aucard) -> Unit,
	onCancelClicked: () -> Unit,
	requestKeyboardClose: () -> Unit,
	modifier: Modifier = Modifier
) {
	val colorController = rememberColorPickerController()
	var colorPaletteOpen by remember { mutableStateOf(false) }
	val focusRequester = remember { FocusRequester() }
	var hex_color by remember { mutableStateOf("") }

	val hsv by remember { mutableStateOf(FloatArray(3)) }

	val textColor = if (hsv[0] in (210f..280f) && hsv[1] in (0.75f..1f)) {
		Color.White
	} else Color.Black

	LaunchedEffect(true) {
		focusRequester.requestFocus()
	}

	Box(
		modifier = modifier
			.fillMaxSize()
	) {

		Column(
			verticalArrangement = Arrangement.Center,
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = Modifier
				.align(Alignment.Center)
		) {
			TextField(
				value = state.aucard.text,
				onValueChange = onTextChange,
				placeholder = { Text(stringResource(R.string.your_text)) },
				colors = TextFieldDefaults.colors(
					unfocusedContainerColor = Color.Transparent,
					focusedContainerColor = Color.Transparent,
					focusedTextColor = textColor,
					unfocusedTextColor = textColor

				),
				keyboardOptions = KeyboardOptions(
					imeAction = ImeAction.Next
				),
				modifier = Modifier
					.focusRequester(focusRequester)
					.testTag("TextField")
			)
			TextField(
				value = state.aucard.description ?: "",
				onValueChange = onDescriptionChange,
				placeholder = { Text(stringResource(R.string.description)) },
				colors = TextFieldDefaults.colors(
					unfocusedContainerColor = Color.Transparent,
					focusedContainerColor = Color.Transparent,
					focusedTextColor = textColor,
					unfocusedTextColor = textColor
				),
				keyboardOptions = KeyboardOptions(
					imeAction = ImeAction.Done
				)
			)
			IconButton(
				onClick = {
					requestKeyboardClose()
					colorPaletteOpen = !colorPaletteOpen
				},
				modifier = Modifier
					.align(Alignment.End)
					.padding(top = 8.dp)
					.clip(CircleShape)
					.size(48.dp)
					.background(Color.White)

			) {
				Icon(
					painter = painterResource(R.drawable.palette),
					contentDescription = stringResource(R.string.choose_color),
					modifier = Modifier
						.size(48.dp)
				)

				DropdownMenu(
					expanded = colorPaletteOpen,
					onDismissRequest = { colorPaletteOpen = false },
					offset = DpOffset((-60).dp, 300.dp)
				) {
					HsvColorPicker(
						controller = colorController,
						onColorChanged = {
							onColorChange(it.color)
							hex_color = it.hexCode
							android.graphics.Color.colorToHSV(it.color.toArgb(), hsv)
						},
						initialColor = state.aucard.color,
						modifier = Modifier
							.padding(8.dp)
							.size(200.dp)
					)
					Text(
						text = hex_color,
						textAlign = TextAlign.Center,
						modifier = Modifier
							.fillMaxWidth()
					)
				}
			}
		}
		Row(
			horizontalArrangement = Arrangement.SpaceBetween,
			modifier = Modifier
				.align(Alignment.BottomCenter)
				.fillMaxWidth()
				//.padding(8.dp)
				//.windowInsetsPadding(WindowInsets(8.dp))
		) {
			IconButton(
				onClick = onCancelClicked,
				modifier = Modifier
					.navigationBarsPadding()
					.size(120.dp)

			) {
				Icon(
					imageVector = Icons.Default.Close,
					contentDescription = "Cancel",
					//modifier = Modifier

				)
			}
			IconButton(
				enabled = state.isValid,
				onClick = { onSaveClicked(state.aucard) },
				modifier = Modifier
					.navigationBarsPadding()
					.size(120.dp)

			) {
				Icon(
					imageVector = Icons.Default.Done,
					contentDescription = stringResource(R.string.save),
					//modifier = Modifier

				)
			}
		}
	}
}

@Composable
private fun ViewScreen(
	state: CardState,
	modifier: Modifier = Modifier
) {
	Box(
		modifier = modifier
			.padding(16.dp)
			.fillMaxSize()
	) {
		Column(
			verticalArrangement = Arrangement.Center,
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = Modifier
				.align(Alignment.Center)
		) {
			Text(
				text = state.aucard.text,
				style = MaterialTheme.typography.displayLarge,
				textAlign = TextAlign.Center
			)
			if (!state.aucard.description.isNullOrBlank()) {
				Text(
					text = state.aucard.description,
					textAlign = TextAlign.Justify,
					modifier = Modifier
						.padding(8.dp)
				)
			}
		}
	}
}

@Preview(device = "spec:parent=pixel_5,orientation=landscape")
@Composable
private fun PreviewEditScreen() {
	EditScreen(
		state = CardState(),
		onSaveClicked = {},
		onTextChange = {},
		onDescriptionChange = {},
		onColorChange = {},
		onCancelClicked = {},
		requestKeyboardClose = {}
	)
}

@Preview(device = "spec:parent=pixel_5,orientation=landscape")
@Composable
private fun PreviewViewScreen() {
	//ViewScreen(CardState(CreateStarters()[4]))
}

