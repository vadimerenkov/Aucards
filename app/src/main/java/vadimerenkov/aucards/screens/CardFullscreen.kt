package vadimerenkov.aucards.screens

import android.R.attr.contentDescription
import android.content.pm.ActivityInfo
import android.provider.Settings
import android.graphics.Color.toArgb
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.VectorConverter
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.hsv
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
//import vadimerenkov.aucards.CreateStarters
import vadimerenkov.aucards.R
import vadimerenkov.aucards.ViewModelFactory
import vadimerenkov.aucards.data.Aucard
import vadimerenkov.aucards.ui.CardState
import vadimerenkov.aucards.ui.CardViewModel
import vadimerenkov.aucards.ui.Palette
import vadimerenkov.aucards.ui.calculateContentColor
import vadimerenkov.aucards.ui.theme.onBackgroundDarkHighContrast

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

	//var contentColor by remember { mutableStateOf(calculateContentColor(state.aucard.color)) }

	LaunchedEffect(true) {
		focusRequester.requestFocus()
	}

	LaunchedEffect(state.aucard.color) {
		Log.i(TAG, "Background color is ${state.aucard.color}")
		//contentColor = calculateContentColor(state.aucard.color)
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
					.size(48.dp)

			) {
				Icon(
					painter = painterResource(R.drawable.palette),
					tint = calculateContentColor(state.aucard.color),
					contentDescription = stringResource(R.string.choose_color),
					modifier = Modifier
						.size(48.dp)
				)

				DropdownMenu(
					offset = DpOffset((-50).dp, 350.dp),
					onDismissRequest = { colorPaletteOpen = false },
					expanded = colorPaletteOpen
				) {
					var selectedTab by remember { mutableStateOf(0) }
					TabRow(
						selectedTabIndex = selectedTab,
						modifier = Modifier
							.width(250.dp)
							.height(40.dp)
					) {
						Tab(
							selected = selectedTab == 0,
							onClick = { selectedTab = 0 }
						) {
							Text("Palette")
						}
						Tab(
							selected = selectedTab == 1,
							onClick = { selectedTab = 1 }
						) {
							Text("Wheel")
						}
					}

					if (selectedTab == 0) {
						FlowRow(
							maxItemsInEachRow = 4,
							modifier = Modifier
								.padding(8.dp)
								.align(Alignment.CenterHorizontally)
						) {
							Palette.colors.forEach { color ->
								Box(
									modifier = Modifier
										.padding(4.dp)
										.clip(MaterialTheme.shapes.medium)
										.background(color)
										.minimumInteractiveComponentSize()
										.clickable(
											onClick = { onColorChange(color) }
										)
								) {
									if (color == state.aucard.color) {
										Icon(
											imageVector = Icons.Default.Done,
											contentDescription = null,
											tint = Color.Black
										)
									}
								}
							}
						}
					}
					if (selectedTab == 1) {
						HsvColorPicker(
							controller = colorController,
							onColorChanged = {
								onColorChange(it.color)
								hex_color = it.hexCode
							},
							initialColor = state.aucard.color,
							modifier = Modifier
								.padding(8.dp)
								.size(200.dp)
								.align(Alignment.CenterHorizontally)
						)
						BrightnessSlider(
							controller = colorController,
							initialColor = state.aucard.color,
							wheelRadius = 8.dp,
							modifier = Modifier
								.padding(horizontal = 16.dp)
								.fillMaxWidth()
								.height(24.dp)
						)
						Text(
							text = hex_color,
							textAlign = TextAlign.Center,
							modifier = Modifier
								.padding(8.dp)
								.fillMaxWidth()
						)
					}
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
					tint = calculateContentColor(state.aucard.color)
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
					tint = calculateContentColor(state.aucard.color)
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
				color = calculateContentColor(state.aucard.color),
				style = MaterialTheme.typography.displayLarge,
				textAlign = TextAlign.Center
			)
			state.aucard.description?.let {
				Text(
					text = it,
					color = calculateContentColor(state.aucard.color),
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

