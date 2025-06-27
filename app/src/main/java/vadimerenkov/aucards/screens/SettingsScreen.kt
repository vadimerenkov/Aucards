@file:OptIn(ExperimentalMaterial3Api::class)

package vadimerenkov.aucards.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
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
import androidx.lifecycle.viewmodel.compose.viewModel
import vadimerenkov.aucards.R
import vadimerenkov.aucards.ViewModelFactory
import vadimerenkov.aucards.settings.Language
import vadimerenkov.aucards.settings.Theme
import vadimerenkov.aucards.ui.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
	onBackClicked: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: SettingsViewModel = viewModel(factory = ViewModelFactory.Factory)
) {
	val context = LocalContext.current
	val name = context.packageName
	val info = context.packageManager.getPackageInfo(name, 0)
	val v = info?.versionName ?: "unknown"

	/* Could not make it work; throws ActivityNotFoundException. Will try to
	fix that in the future.

	val permissionLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.RequestPermission(),
		onResult = { result ->

		}
	)
	 */

	val state by viewModel.settingsState.collectAsState()

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
					/* see comment above.

				CheckboxSetting(
					description = stringResource(R.string.brightness),
					onCheckedChange = {
						//activity?.requestPermissions(arrayOf(Manifest.permission.WRITE_SETTINGS), 0)
						permissionLauncher.launch(Manifest.permission.WRITE_SETTINGS)
						//viewModel.saveBrightnessSetting(it)
					},
					isChecked = state.isMaxBrightness
				)

				 */
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
						description = stringResource(R.string.theme),
						onOptionChosen = { viewModel.saveThemeSetting(it) },
						chosenOption = stringResource(state.theme.uiText)
					)
					DropdownSetting(
						options = Language.entries.map { it.uiText },
						description = stringResource(R.string.language),
						onOptionChosen = { viewModel.saveLanguageSetting(it) },
						chosenOption = stringResource(state.language.uiText)
					)
					state.language.translator?.let { it ->
						Text(
							text = stringResource(it),
							color = Color.Blue,
							style = MaterialTheme.typography.bodyMedium
						)
					}
				}

				Column(
					horizontalAlignment = Alignment.CenterHorizontally,
					modifier = Modifier
						.padding(12.dp)
						.navigationBarsPadding()
						.fillMaxWidth()
				) {
					val handler = LocalUriHandler.current
					val link = stringResource(R.string.source_code_link)

					Text(
						text = stringResource(R.string.about, v),
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
				}
			}
		}
	}
}

@Composable
private fun SettingsRow(
	modifier: Modifier = Modifier,
	content: @Composable (RowScope.() -> Unit)
) {
	Row(
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically,
		modifier = modifier
			.fillMaxWidth()
			.padding(bottom = 8.dp)
	) {
		content()
	}
}

@Composable
private fun DropdownSetting(
	options: List<Int>,
	description: String,
	chosenOption: String,
	onOptionChosen: (Int) -> Unit
) {
	var expanded by remember { mutableStateOf(false) }

	SettingsRow {
		Text(
			text = description,
			style = MaterialTheme.typography.bodyLarge,
			modifier = Modifier
				.weight(0.5f)
		)
		ExposedDropdownMenuBox(
			expanded = expanded,
			onExpandedChange = { expanded = it },
			modifier = Modifier
				.weight(0.5f)
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
					DropdownMenuItem(
						text = { Text(stringResource(option)) },
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
	SettingsRow(
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