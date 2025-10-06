package vadimerenkov.aucards.screens.settings

import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.media.RingtoneManager.ACTION_RINGTONE_PICKER
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import vadimerenkov.aucards.BuildConfig
import vadimerenkov.aucards.R
import vadimerenkov.aucards.ViewModelFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val TAG = "SettingsScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
	onBackClicked: (page: Int) -> Unit,
	isWideScreen: Boolean,
	modifier: Modifier = Modifier,
	viewModel: SettingsViewModel = viewModel(factory = ViewModelFactory.Factory())
) {
	val context = LocalContext.current
	val version = if (BuildConfig.DEBUG) BuildConfig.VERSION_NAME + "-debug" else BuildConfig.VERSION_NAME
	val lifecycleOwner = LocalLifecycleOwner.current

	val state by viewModel.settingsState.collectAsState()
	var showBrightnessContext by remember { mutableStateOf(false) }
	var didWeClickOnBrightness by remember { mutableStateOf(false) }
	var ringtoneName by remember { mutableStateOf("None") }

	LaunchedEffect(state.ringtoneUri) {
		if (state.ringtoneUri != null) {
			val ringtone = RingtoneManager.getRingtone(context, state.ringtoneUri)
			ringtoneName = ringtone.getTitle(context)
		}
	}

	DisposableEffect(viewModel.hasPermission(context)) {
		val observer = LifecycleEventObserver { _, event ->
			if (event == Lifecycle.Event.ON_RESUME) {
				if (viewModel.hasPermission(context)) {
					viewModel.saveBrightnessSetting(true)
					showBrightnessContext = false
				} else {
					if (didWeClickOnBrightness) {
						showBrightnessContext = true
						viewModel.saveBrightnessSetting(false)
					}
				}
			}
		}
		lifecycleOwner.lifecycle.addObserver(observer)
		onDispose {
			lifecycleOwner.lifecycle.removeObserver(observer)
		}
	}

	Row {
		if (isWideScreen) {
			NavigationRail(
				containerColor = MaterialTheme.colorScheme.primaryContainer,
				contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
			) {
				NavigationRailItem(
					selected = false,
					onClick = { onBackClicked(0) },
					icon = {
						Icon(
							painterResource(R.drawable.grid),
							contentDescription = null
						)
					},
					label = {
						Text(
							text = stringResource(R.string.all_cards),
							modifier = Modifier
								.padding(horizontal = 8.dp))
					},
					colors = NavigationRailItemDefaults.colors(
						indicatorColor = MaterialTheme.colorScheme.onPrimaryContainer,
						selectedIconColor = MaterialTheme.colorScheme.onPrimary
					),
					modifier = Modifier
						.displayCutoutPadding()
				)
				NavigationRailItem(
					selected = false,
					onClick = { onBackClicked(1) },
					icon = {
						Icon(
							imageVector = Icons.Outlined.Star,
							contentDescription = null
						)
					},
					label = {
						Text(
							text = stringResource(R.string.favourites),
							modifier = Modifier
								.padding(horizontal = 8.dp))
					},
					colors = NavigationRailItemDefaults.colors(
						indicatorColor = MaterialTheme.colorScheme.onPrimaryContainer,
						selectedIconColor = MaterialTheme.colorScheme.onPrimary
					),
					modifier = Modifier
						.displayCutoutPadding()
				)
				Spacer(modifier = Modifier.weight(1f))
				NavigationRailItem(
					selected = true,
					onClick = { },
					icon = {
						Icon(
							imageVector = Icons.Filled.Settings,
							contentDescription = null
						)
					},
					label = {
						Text(
							text = stringResource(R.string.settings),
							modifier = Modifier
								.padding(horizontal = 8.dp)
						)
					},
					colors = NavigationRailItemDefaults.colors(
						indicatorColor = MaterialTheme.colorScheme.onPrimaryContainer,
						selectedIconColor = MaterialTheme.colorScheme.onPrimary
					),
					modifier = Modifier
						.displayCutoutPadding()
				)
			}
		}
		Scaffold(
			topBar = {
				TopAppBar(
					title = { Text(stringResource(R.string.settings)) },
					navigationIcon = {
						if (!isWideScreen) {
							IconButton(
								onClick = { onBackClicked(0) },
							) {
								Icon(
									imageVector = Icons.AutoMirrored.Filled.ArrowBack,
									contentDescription = stringResource(R.string.go_back)
								)
							}
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
					.padding(horizontal = 16.dp)
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
					Column(
						horizontalAlignment = Alignment.CenterHorizontally
					) {
						val saveLauncher =
							rememberLauncherForActivityResult(
							ActivityResultContracts.CreateDocument("application/zip")
						) {
							if (it != null) {
								Log.d(TAG, "Path saved: $it")
								viewModel.exportDatabase(it, context)
							} else {
								Log.d(TAG, "Path not saved.")
							}
						}
						val loadLauncher =
							rememberLauncherForActivityResult(
							contract = object : ActivityResultContracts.GetContent() {
								override fun createIntent(context: Context, input: String): Intent {
									return super.createIntent(context, input)
										.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/zip", "application/octet-stream"))
								}
							}
						) {
							if (it != null) {
								viewModel.importDatabase(it, context)
								onBackClicked(0)
							}
						}
						val pickSoundLauncher =
							rememberLauncherForActivityResult(
								contract = ActivityResultContracts.StartActivityForResult()
							) {
								val ringtone_uri = it
									.data
									?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
								ringtone_uri?.let {uri ->
									viewModel.saveSoundUri(uri)
								}
							}
						CheckboxSetting(
							title = stringResource(R.string.brightness),
							description = stringResource(R.string.brightness_permission),
							isDescVisible = showBrightnessContext,
							onCheckedChange = {
								if (!viewModel.hasPermission(context)) {
									val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
										data = "package:${context.packageName}".toUri()
										addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
									}
									context.startActivity(intent)
									didWeClickOnBrightness = true
								}
								else{
									viewModel.saveBrightnessSetting(it)
								}
							},
							isChecked = state.isMaxBrightness
						)
						HorizontalDivider()
						CheckboxSetting(
							title = stringResource(R.string.landscape),
							onCheckedChange = { viewModel.saveLandscapeSetting(it) },
							isChecked = state.isLandscapeMode
						)
						HorizontalDivider()
						CheckboxSetting(
							isChecked = state.playSound,
							title = stringResource(R.string.settings_play_sound) ,
							description = stringResource(R.string.play_sound_description),
							onCheckedChange = {
								viewModel.saveSoundSetting(it)
								if (state.ringtoneUri == null) {
									try {
										val uri = RingtoneManager.getActualDefaultRingtoneUri(
											context,
											RingtoneManager.TYPE_NOTIFICATION
										)
										viewModel.saveSoundUri(uri)
									} catch (e: Exception) {
										Log.e(TAG, "Error when getting default ringtone: $e")
									}
								}
							}
						)
						AnimatedVisibility(
							visible = state.playSound
						) {
							Row(
								verticalAlignment = Alignment.CenterVertically,
								modifier = Modifier
									.fillMaxWidth()
							) {
								Text(
									text = stringResource(R.string.ringtone)
								)
								TextButton(
									onClick = {
										val intent = Intent(ACTION_RINGTONE_PICKER)
										pickSoundLauncher.launch(intent)
									}
								) {
									Text(
										text = ringtoneName,
										style = MaterialTheme.typography.bodyLarge,
										textDecoration = TextDecoration.Underline
									)
									Spacer(modifier = Modifier.width(4.dp))
									Icon(
										painter = painterResource(R.drawable.open_in_new),
										contentDescription = stringResource(R.string.choose_ringtone)
									)
								}
							}
						}
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
							HorizontalDivider()
							CheckboxSetting(
								title = "Material You",
								isChecked = state.materialYou,
								onCheckedChange = {
									viewModel.saveMaterialSetting(it)
								}
							)
						}
						HorizontalDivider()
						DropdownSetting(
							options = Theme.entries.map { it.uiText },
							icon = R.drawable.theme_mode,
							description = stringResource(R.string.theme),
							onOptionChosen = { viewModel.saveThemeSetting(it) },
							chosenOption = stringResource(state.theme.uiText),
							modifier = Modifier
								.padding(top = 8.dp)
						)
						DropdownSetting(
							options = Language.entries.map { it.uiText }.sorted(),
							icon = R.drawable.language,
							description = stringResource(R.string.language),
							onOptionChosen = { viewModel.saveLanguageSetting(it) },
							chosenOption = stringResource(state.language.uiText)
						)
						state.language.translator?.let { it ->
							Text(
								text = stringResource(it),
								color = MaterialTheme.colorScheme.primary,
									style = MaterialTheme.typography.bodyMedium,
									modifier = Modifier
										.padding(top = 8.dp)
										.align(Alignment.End)
								)
							}
						Spacer(modifier = Modifier.padding(8.dp))
						OutlinedButton(
							onClick = {
								val formatter = DateTimeFormatter
									.ofPattern("yyyy-MM-dd.HH.mm.ss")
								val now = formatter.format(LocalDateTime.now())
								val version = BuildConfig.VERSION_NAME
								saveLauncher.launch("aucards-$version-exported-$now.zip")
							},
							enabled = !state.isDbEmpty,
							shape = MaterialTheme.shapes.medium
						) {
							Text(text = stringResource(R.string.export))
						}
						OutlinedButton(
							onClick = {
								loadLauncher.launch("application/zip")
							},
							shape = MaterialTheme.shapes.medium
						) {
							Text(text = stringResource(R.string.import_cards))
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
							text = stringResource(R.string.logo),
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
}