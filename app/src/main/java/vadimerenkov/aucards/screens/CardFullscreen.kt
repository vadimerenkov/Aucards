@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalSharedTransitionApi::class)

package vadimerenkov.aucards.screens

//import vadimerenkov.aucards.CreateStarters
import android.content.pm.ActivityInfo
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import kotlinx.coroutines.Dispatchers
import vadimerenkov.aucards.R
import vadimerenkov.aucards.ViewModelFactory
import vadimerenkov.aucards.data.Aucard
import vadimerenkov.aucards.ui.CardState
import vadimerenkov.aucards.ui.CardViewModel
import vadimerenkov.aucards.ui.ContentType
import vadimerenkov.aucards.ui.Palette
import vadimerenkov.aucards.ui.SharedContentStateKey
import vadimerenkov.aucards.ui.calculateContentColor

private const val TAG = "CardFullscreen"

@Composable
fun SharedTransitionScope.CardFullscreen(
	onBackClicked: () -> Unit,
	isDarkTheme: Boolean,
	scope: AnimatedVisibilityScope,
	modifier: Modifier = Modifier,
	viewModel: CardViewModel = viewModel(factory = ViewModelFactory.Factory(isDarkTheme))
) {
	val state by viewModel.cardState.collectAsStateWithLifecycle(
		context = Dispatchers.Main.immediate
	)
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

	LaunchedEffect(state.isLandscapeMode) {
		if (state.isLandscapeMode == true) {
			activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
			Log.i(TAG, "Requested landscape mode, setting is ${state.isLandscapeMode}.")
		} else {
			activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
			Log.i(TAG, "Reset back to user preferences, setting is ${state.isLandscapeMode}.")
		}
	}

	if (hasPermission()) {
		Settings.System.putInt(
			activity?.contentResolver,
			Settings.System.SCREEN_BRIGHTNESS,
			255
		)
	}

	val contentColor by animateColorAsState(
		calculateContentColor(state.aucard.color)
	)

	val contentState = rememberSharedContentState(
		SharedContentStateKey(
			state.aucard.id,
			ContentType.CARD
		)
	)

	val textContentState = rememberSharedContentState(
		SharedContentStateKey(
			state.aucard.id,
			ContentType.TEXT
		)
	)

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
			.sharedBounds(
				sharedContentState = contentState,
				animatedVisibilityScope = scope,
				enter = scaleIn(),
				exit = scaleOut(),
				resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
			)
	) {
		if (state.isEditable) {
			EditScreen(
				state = state,
				onTextChange = { viewModel.updateText(it) },
				onDescriptionChange = { viewModel.updateDescription(it) },
				onSaveClicked = {
					viewModel.saveAucard(it)
					onBackClicked()
				},
				onCancelClicked = onBackClicked,
				onColorChange = { viewModel.updateColor(it) },
				onHexChange = { viewModel.updateHexCode(it) },
				requestKeyboardClose = {
					keyboardController?.hide()
				},
				scope = scope,
				contentColor = contentColor,
				textContentState = textContentState
			)
		}
		else {
			ViewScreen(
				state = state,
				scope = scope,
				contentColor = contentColor,
				textContentState = textContentState
			)
		}
	}
}

@Composable
private fun SharedTransitionScope.EditScreen(
	state: CardState,
	onTextChange: (String) -> Unit,
	onDescriptionChange: (String) -> Unit,
	onColorChange: (Color) -> Unit,
	onHexChange: (String) -> Unit,
	onSaveClicked: (Aucard) -> Unit,
	onCancelClicked: () -> Unit,
	requestKeyboardClose: () -> Unit,
	scope: AnimatedVisibilityScope,
	contentColor: Color,
	textContentState: SharedTransitionScope.SharedContentState,
	modifier: Modifier = Modifier
) {
	val colorController = rememberColorPickerController()
	var colorPaletteOpen by remember { mutableStateOf(false) }
	val focusRequester = remember { FocusRequester() }

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
					imeAction = ImeAction.Next
				),
				textStyle = MaterialTheme.typography.displayLarge.copy(
					textAlign = TextAlign.Center
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
						sharedContentState = textContentState,
						animatedVisibilityScope = scope
					)
			)
			TextField(
				value = state.aucard.description ?: "",
				onValueChange = onDescriptionChange,
				textStyle = MaterialTheme.typography.titleLarge.copy(
					textAlign = TextAlign.Center
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
				keyboardOptions = KeyboardOptions(
					imeAction = ImeAction.Done
				),
				modifier = Modifier
					.fillMaxWidth()
			)
			Row(
				verticalAlignment = Alignment.CenterVertically
			) {
				Spacer(modifier = Modifier.weight(1f))

				Box(

				) {
					IconButton(
						onClick = {
							requestKeyboardClose()
							colorPaletteOpen = !colorPaletteOpen
						},
						modifier = Modifier
							.padding(vertical = 8.dp, horizontal = 24.dp)
							.size(48.dp)
					) {
						Icon(
							painter = painterResource(R.drawable.palette),
							tint = contentColor,
							contentDescription = stringResource(R.string.choose_color),
							modifier = Modifier
								.size(48.dp)
						)
					}

					DropdownMenu(
						offset = DpOffset((-50).dp, 0.dp),
						onDismissRequest = { colorPaletteOpen = false },
						properties = PopupProperties(
							//clippingEnabled = false
						),
						expanded = colorPaletteOpen
					) {
						var selectedTab by remember { mutableIntStateOf(0) }
						TabRow(
							selectedTabIndex = selectedTab,
							modifier = Modifier
								.width(300.dp)
								.height(50.dp)
						) {
							Tab(
								selected = selectedTab == 0,
								onClick = { selectedTab = 0 }
							) {
								Text(stringResource(R.string.palette))
							}
							Tab(
								selected = selectedTab == 1,
								onClick = { selectedTab = 1 }
							) {
								Text(stringResource(R.string.custom_color))
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
												contentDescription = color.toString(),
												tint = contentColor
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
									onHexChange(it.hexCode)
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
								wheelColor = contentColor,
								modifier = Modifier
									.padding(horizontal = 16.dp)
									.fillMaxWidth()
									.height(24.dp)
							)
							OutlinedTextField(
								value = state.hexColor.uppercase(),
								isError = !state.isHexCodeValid,
								singleLine = true,
								textStyle = LocalTextStyle.current.copy(
									textAlign = TextAlign.Center
								),
								onValueChange = onHexChange,
								modifier = Modifier
									.padding(8.dp)
									.width(120.dp)
									.align(Alignment.CenterHorizontally)
							)
						}
					}
				}
			}
		}
		Row(
			horizontalArrangement = Arrangement.SpaceBetween,
			modifier = Modifier
				.align(Alignment.BottomCenter)
				.fillMaxWidth()
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
					tint = contentColor
				)
			}
			IconButton(
				enabled = state.isValid,
				onClick = { onSaveClicked(state.aucard) },
				colors = IconButtonDefaults.iconButtonColors(
					contentColor = contentColor,
					disabledContentColor = contentColor.copy(alpha = 0.3f)
				),
				modifier = Modifier
					.navigationBarsPadding()
					.size(120.dp)
			) {
				Icon(
					imageVector = Icons.Default.Done,
					contentDescription = stringResource(R.string.save)
				)
			}
		}
	}
}

@Composable
private fun SharedTransitionScope.ViewScreen(
	state: CardState,
	scope: AnimatedVisibilityScope,
	contentColor: Color,
	textContentState: SharedTransitionScope.SharedContentState,
	modifier: Modifier = Modifier
) {
	val window = LocalActivity.current?.window

	LaunchedEffect(true) {
		if (window != null) {
			val controller = WindowCompat.getInsetsController(window, window.decorView)
			controller.hide(WindowInsetsCompat.Type.systemBars())
		}
		Log.i(TAG, "Window is $window, trying to hide bars")
	}

	DisposableEffect(true) {
		onDispose {
			if (window != null) {
				val controller = WindowCompat.getInsetsController(window, window.decorView)
				controller.show(WindowInsetsCompat.Type.systemBars())
			}
		}
	}

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
				color = contentColor,
				style = MaterialTheme.typography.displayLarge,
				textAlign = TextAlign.Center,
				modifier = Modifier
					.sharedBounds(
						sharedContentState = textContentState,
						animatedVisibilityScope = scope
					)
			)
			state.aucard.description?.let {
				Text(
					text = it,
					color = contentColor,
					style = MaterialTheme.typography.titleLarge,
					textAlign = TextAlign.Justify,
					modifier = Modifier
						.padding(8.dp)
				)
			}
		}
	}
}