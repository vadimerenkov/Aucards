@file:OptIn(ExperimentalMaterial3Api::class)

package vadimerenkov.aucards.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import vadimerenkov.aucards.BuildConfig
import vadimerenkov.aucards.R
import vadimerenkov.aucards.ViewModelFactory
import vadimerenkov.aucards.settings.Language
import vadimerenkov.aucards.settings.Theme
import vadimerenkov.aucards.ui.SettingsViewModel

private const val TAG = "SettingsScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
	onBackClicked: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: SettingsViewModel = viewModel(factory = ViewModelFactory.Factory())
) {
	val context = LocalContext.current
	val version = BuildConfig.VERSION_NAME
	val lifecycleOwner = LocalLifecycleOwner.current
	fun hasPermission(context: Context): Boolean {
		return Settings.System.canWrite(context)
	}

	val state by viewModel.settingsState.collectAsState()
	var showBrightnessContext by remember { mutableStateOf(false) }

	if (!hasPermission(context)) {
		DisposableEffect(Unit) {
			val observer = LifecycleEventObserver { _, event ->
				if (event == Lifecycle.Event.ON_RESUME) {
					if (hasPermission(context)) {
						viewModel.saveBrightnessSetting(true)
						showBrightnessContext = false
					} else {
						showBrightnessContext = true
						viewModel.saveBrightnessSetting(false)
					}
				}
			}
			lifecycleOwner.lifecycle.addObserver(observer)
			onDispose {
				lifecycleOwner.lifecycle.removeObserver(observer)
			}
		}
	}
	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text(stringResource(R.string.settings)) },
				navigationIcon = {
					IconButton(
						onClick = onBackClicked,
					) {
						Icon(
							imageVector = Icons.AutoMirrored.Filled.ArrowBack,
							contentDescription = stringResource(R.string.go_back)
						)
					}
				},
				colors = TopAppBarDefaults.topAppBarColors(
					containerColor = MaterialTheme.colorScheme.primaryContainer
				)
			)
		}
	) { innerPadding ->
		val scroll = rememberScrollState()
		Box(
			contentAlignment = Alignment.TopStart,
			modifier = modifier
				.padding(innerPadding)
				.padding(16.dp)
				.fillMaxSize()
		) {
			Column(
				verticalArrangement = Arrangement.SpaceBetween,
				modifier = Modifier
					.widthIn(max = 500.dp)
					.align(Alignment.Center)
					.fillMaxSize()
					.verticalScroll(scroll)
			) {
				Column {
					CheckboxSetting(
						description = stringResource(R.string.brightness),
						onCheckedChange = {
							if (!hasPermission(context)) {
								val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
									data = "package:${context.packageName}".toUri()
									addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
								}
								context.startActivity(intent)
							}
							else{
								viewModel.saveBrightnessSetting(it)
							}
						},
						isChecked = state.isMaxBrightness
					)
					AnimatedVisibility(showBrightnessContext) {
						Text(
							text = stringResource(R.string.brightness_permission),
							color = Color.Blue,
							style = MaterialTheme.typography.bodyMedium
						)
					}
					HorizontalDivider(
						modifier = Modifier
							.padding(vertical = 8.dp)
					)
					CheckboxSetting(
						description = stringResource(R.string.landscape),
						onCheckedChange = { viewModel.saveLandscapeSetting(it) },
						isChecked = state.isLandscapeMode
					)
					HorizontalDivider(
						modifier = Modifier
							.padding(vertical = 8.dp)
					)
					DropdownSetting(
						options = Theme.entries.map { it.uiText },
						icon = R.drawable.theme_mode,
						description = stringResource(R.string.theme),
						onOptionChosen = { viewModel.saveThemeSetting(it) },
						chosenOption = stringResource(state.theme.uiText)
					)
					DropdownSetting(
						options = Language.entries.map { it.uiText }.sorted(),
						icon = R.drawable.language,
						description = stringResource(R.string.language),
						onOptionChosen = { viewModel.saveLanguageSetting(it) },
						chosenOption = stringResource(state.language.uiText)
					)
					state.language.translator?.let { it ->
						Spacer(modifier = Modifier.height(8.dp))
						Text(
							text = stringResource(it),
							color = Color.Blue,
							style = MaterialTheme.typography.bodyMedium,
							modifier = Modifier
								.align(Alignment.End)
						)
					}
				}

				Column(
					horizontalAlignment = Alignment.CenterHorizontally,
					modifier = Modifier
						.padding(12.dp)
						.fillMaxWidth()
				) {
					val handler = LocalUriHandler.current
					val link = stringResource(R.string.source_code_link)

					Text(
						text = stringResource(R.string.about, version),
						style = MaterialTheme.typography.bodyLarge,
						textAlign = TextAlign.Center,
						modifier = Modifier
							.padding(8.dp)
					)
					TextButton(
						onClick = {
							handler.openUri(link)
						}
					) {
						Row {

							Text(
								text = stringResource(R.string.source_code),
								style = MaterialTheme.typography.bodyLarge,
								textDecoration = TextDecoration.Underline,
								modifier = Modifier
									.padding(end = 4.dp)
							)
							Icon(
								painter = painterResource(R.drawable.open_in_new),
								contentDescription = null
							)
						}
					}
					Spacer(modifier = Modifier.height(48.dp))
					Text(
						text = "logo: Olga Prilutskaia",
						style = MaterialTheme.typography.bodyLarge,
						textAlign = TextAlign.Center,
						modifier = Modifier
							.padding(8.dp)
					)
				}
			}
		}
	}
}

@Composable
private fun DropdownSetting(
	options: List<Int>,
	description: String,
	chosenOption: String,
	onOptionChosen: (Int) -> Unit,
	modifier: Modifier = Modifier,
	@DrawableRes icon: Int? = null
) {
	var expanded by remember { mutableStateOf(false) }

	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = modifier
			.fillMaxWidth()
	) {
		Row(
			modifier = Modifier
				.weight(1f)
		) {
			icon?.let {
				Icon(
					painter = painterResource(it),
					contentDescription = null,
					tint = MaterialTheme.colorScheme.primary
				)
				Spacer(modifier = Modifier.width(6.dp))
			}
			Text(
				text = description,
				style = MaterialTheme.typography.bodyLarge
			)
		}
		ExposedDropdownMenuBox(
			expanded = expanded,
			onExpandedChange = { expanded = it },
			modifier = Modifier
				.weight(1f)
		) {
			TextField(
				value = chosenOption,
				onValueChange = {},
				readOnly = true,
				modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
				trailingIcon = { Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null) }
			)
			ExposedDropdownMenu(
				expanded = expanded,
				onDismissRequest = { expanded = !expanded }
			) {
				options.forEach { option ->
					val text = stringResource(option)

					DropdownMenuItem(
						text = { Text(text) },
						trailingIcon = {
							if (text == chosenOption) {
								Icon(
									imageVector = Icons.Default.Check,
									contentDescription = null
								)
							} else null
						},
						onClick = {
							onOptionChosen(option)
							expanded = false
						}
					)
				}
			}
		}
	}
}

@Composable
private fun CheckboxSetting(
	description: String,
	onCheckedChange: (Boolean) -> Unit,
	modifier: Modifier = Modifier,
	isChecked: Boolean = false
) {
	Row(
		modifier = modifier
			.clickable(onClick = {
				onCheckedChange(!isChecked)
			})
	) {
		Text(
			text = description,
			style = MaterialTheme.typography.bodyLarge,
			modifier = Modifier
				.weight(1f, false)
		)
		Checkbox(
			checked = isChecked,
			onCheckedChange = onCheckedChange,
			modifier = Modifier

		)
	}
}

@Preview()
@Composable
private fun SettingsPreview() {
	SettingsScreen(
		onBackClicked = {  }
	)
}