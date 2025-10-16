package vadimerenkov.aucards.screens.settings

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate.getApplicationLocales
import androidx.appcompat.app.AppCompatDelegate.setApplicationLocales
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.intl.Locale
import androidx.core.net.toUri
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vadimerenkov.aucards.R
import vadimerenkov.aucards.data.Aucard
import vadimerenkov.aucards.data.AucardsDatabase
import vadimerenkov.aucards.data.CardLayout
import vadimerenkov.aucards.ui.SnackbarController
import vadimerenkov.aucards.ui.code
import vadimerenkov.aucards.ui.getPluralString
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

private const val TAG = "SettingsViewModel"

class SettingsViewModel(
	private val settings: Settings,
	private val database: AucardsDatabase
): ViewModel() {
	private val state = MutableStateFlow(SettingsState())

	private val settings_observer = settings.settingsFlow
		.onStart {
			val isEmpty = database.aucardDao().getAllCards().first().isEmpty()
			state.update { it.copy(isDbEmpty = isEmpty) }
		}
		.onEach {
			val landscape = settings.landscape.first() ?: false
			val brightness = settings.brightness.first() ?: false
			val playSound = settings.playSound.first() ?: false
			val ringtoneUri = settings.soundUri.first()?.toUri()
			val materialYou = settings.materialYou.first() ?: false
			val theme = readThemeSetting()
			val language = readLanguageSetting()
			state.update { it.copy(
				theme = theme,
				isMaxBrightness = brightness,
				isLandscapeMode = landscape,
				language = language,
				playSound = playSound,
				ringtoneUri = ringtoneUri,
				materialYou = materialYou
			) }
		}
		.launchIn(viewModelScope)

	val settingsState = state
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5000L),
			initialValue = state.value
		)

	private fun readLanguageSetting(): Language {
		val locales = getApplicationLocales()
			.toLanguageTags()

		Log.i(TAG, "Loaded $locales as saved locale.")

		val lang = Language.entries.find { it.code == locales }

		if (lang != null) {
			return lang
		}

		Log.w(TAG, "Could not load language settings.")
		val current_locale = Locale.current
		val default_language = current_locale.toLanguageTag()

		Log.i(TAG, "Current locale is $current_locale, defaulting to $default_language.")

		Language.entries.forEach { it ->
			if (it.code.contains(default_language.take(2))) {
				return it
			}
		}

		Log.e(TAG, "$default_language is not supported. Defaulting to English.")

		return Language.ENGLISH
	}

	private suspend fun readThemeSetting(): Theme {
		val theme = settings.themeSetting.first()

		Theme.entries.forEach { it ->
			if (theme == it.name) {
				return it
			}
		}

		Log.w(TAG, "Could not load theme settings; defaulting to Device.")
		return Theme.DEVICE
	}

	fun saveThemeSetting(themeValue: Int) {

		val theme = Theme.entries.find { it.ordinal == themeValue } ?: Theme.DEVICE

		viewModelScope.launch {
			settings.saveStringSettings(
				key = THEME_STRING,
				value = theme.name
			)
		}

	}

	fun saveLanguageSetting(lang_string: String, context: Context) {

		val language = Language.entries.find { lang ->
			context.getString(lang.uiText) == lang_string
		} ?: Language.ENGLISH

		val locale = LocaleListCompat.forLanguageTags(language.code)
		Log.d(TAG, "Saved $locale as new language.")

		setApplicationLocales(locale)
		state.update { it.copy(language = language) }
	}

	fun saveLandscapeSetting(landscape: Boolean) {
		viewModelScope.launch {
			settings.saveBoolSettings(LANDSCAPE_STRING, landscape)
		}
	}

	fun saveBrightnessSetting(brightness: Boolean) {
		viewModelScope.launch {
			settings.saveBoolSettings(
				key = BRIGHTNESS_STRING,
				value = brightness
			)
		}
	}

	fun saveMaterialSetting(isDynamic: Boolean) {
		viewModelScope.launch {
			settings.saveBoolSettings(
				key = MATERIAL_STRING,
				value = isDynamic
			)
		}
	}

	fun saveSoundSetting(playSound: Boolean) {
		viewModelScope.launch {
			settings.saveBoolSettings(
				key = SOUND_STRING,
				value = playSound
			)
		}
	}

	fun saveSoundUri(uri: Uri) {
		viewModelScope.launch {
			settings.saveStringSettings(
				key = RINGTONE_URI,
				value = uri.toString()
			)
		}
	}

	fun hasPermission(context: Context): Boolean {
		return android.provider.Settings.System.canWrite(context)
	}

	fun exportDatabase(uri: Uri, context: Context) {
		viewModelScope.launch {
			try {
				val size = database.aucardDao().getAllCards().first().size
				val temp = File.createTempFile("aucards_export", ".db", context.cacheDir)
				val folder = File(context.filesDir, "images")
				val outputStream = context.contentResolver.openOutputStream(uri)

				val db = database.openHelper.writableDatabase
				db.execSQL("VACUUM INTO '${temp.absolutePath}'")
				val inputStream = FileInputStream(temp)

				ZipOutputStream(outputStream).use { zip ->
					val dbEntry = ZipEntry("database")
					zip.putNextEntry(dbEntry)
					inputStream.copyTo(zip)
					zip.closeEntry()
					folder.listFiles()?.forEach { file ->
						FileInputStream(file).use { input ->
							val entry = ZipEntry(file.name)
							zip.putNextEntry(entry)
							input.copyTo(zip)
							zip.closeEntry()
						}
					}
				}
				val message = context.getPluralString(R.plurals.export_success, size, size)
				SnackbarController.send(message)
				temp.delete()

			} catch (e: Exception) {
				if (e is CancellationException) throw e
				Log.e(TAG, "Error exporting the db: $e")
				val message = context.getString(R.string.export_error, e.code())
				SnackbarController.send(message)
			}
		}
	}

	fun importDatabase(uri: Uri, context: Context) {
		viewModelScope.launch {
			try {
				var current_index = database.aucardDao().getAllCards().first().size
				val dbFile = File(context.cacheDir, "database.db")
				val images = File(context.filesDir, "images")
				if (!images.exists()) {
					images.mkdirs()
				}
				val type = context.contentResolver.getType(uri)
				val file = context.contentResolver.openInputStream(uri)

				when (type) {
					"application/zip" -> {
						ZipInputStream(file).use { zip ->
							var entry = zip.nextEntry
							do {
								if (entry.name == "database") {
									FileOutputStream(dbFile).use { output ->
										zip.copyTo(output)
									}
								} else {
									val image = File(images, entry.name)
									FileOutputStream(image).use { imagesOutput ->
										zip.copyTo(imagesOutput)
									}
								}
								zip.closeEntry()
								entry = zip.nextEntry
							} while (entry != null)
						}
					}

					"application/octet-stream" -> {
						file?.use { input ->
							FileOutputStream(dbFile).use { output ->
								input.copyTo(output)
							}
						}
					}

					else -> {
						throw IllegalArgumentException("Not a database nor a zip archive.")
					}
				}

				val cards = mutableListOf<Aucard>()

				SQLiteDatabase
					.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)
					.use { database ->
						database.rawQuery("SELECT * FROM aucard", null)
							.use { cursor ->
								if (cursor.moveToFirst()) {
									do {
										val card =
											importCard(cursor).copy(index = current_index + 1)
										cards.add(card)
										current_index++

									} while (cursor.moveToNext())
								}
							}
					}

				database.aucardDao().saveAllCards(cards)
				val message = context.getPluralString(R.plurals.import_success, cards.size, cards.size)
				SnackbarController.send(message)
				dbFile.delete()

			} catch (e: Exception) {
				if (e is CancellationException) throw e
				Log.e(TAG, "Error importing the db: $e")
				val message = context.getString(R.string.import_error, e.code())
				SnackbarController.send(message)
			}
		}
	}
}

private fun importCard(cursor: Cursor): Aucard {
	with(cursor) {
		// Initial db columns
		val text = getColumnIndex("text")
		val color = getColumnIndex("color")
		val desc = getColumnIndex("description")
		val fav = getColumnIndex("isFavourite")

		// Columns after layout update
		val textSize = getColumnIndex("titleFontSize")
		val descSize = getColumnIndex("descriptionFontSize")
		val layout = getColumnIndex("layout")

		// Columns after image update
		val imagePath = getColumnIndex("imagePath")
		val imageScale = getColumnIndex("imageScale")
		val imageRotation = getColumnIndex("imageRotation")
		val imageOffset = getColumnIndex("imageOffset")
		val textBackgroundOpacity = getColumnIndex("textBackgroundOpacity")

		val text_value = getString(text)
		val color_value = getInt(color)
		val desc_value = getString(desc)
		val fav_value = getInt(fav)

		val textSizeValue = if (textSize == -1) 57 else getInt(textSize)
		val descSizeValue = if (descSize == -1) 24 else getInt(descSize)
		val layoutValue = if (layout == -1) CardLayout.TITLE_SUBTITLE.name else getString(layout)

		val path = if (imagePath == -1) null else getString(imagePath)
		val scale = if (imageScale == -1) 1f else getFloat(imageScale)
		val rotation = if (imageRotation == -1) 0f else getFloat(imageRotation)
		val offset = if (imageOffset == -1) Offset.Zero else Offset(getLong(imageOffset))
		val opacity = if (textBackgroundOpacity == -1) 0.5f else getFloat(textBackgroundOpacity)

		val safeValueLayout = try {
			CardLayout.valueOf(layoutValue)
		} catch (e: IllegalArgumentException) {
			Log.e(TAG, "Invalid layout value: $e")
			CardLayout.TITLE_SUBTITLE
		}

		val card = Aucard(
			text = text_value,
			color = Color(color_value).copy(alpha = 1f),
			description = desc_value,
			isFavourite = fav_value.toBoolean(),

			titleFontSize = textSizeValue,
			descriptionFontSize = descSizeValue,
			layout = safeValueLayout,

			imagePath = path?.toUri(),
			imageScale = scale,
			imageRotation = rotation,
			imageOffset = offset,
			textBackgroundOpacity = opacity
		)

		return card
	}
}

data class SettingsState(
	val theme: Theme = Theme.DEVICE,
	val isMaxBrightness: Boolean = false,
	val isLandscapeMode: Boolean = false,
	val playSound: Boolean = false,
	val materialYou: Boolean = false,
	val ringtoneUri: Uri? = null,
	val language: Language = Language.ENGLISH,
	val isDbEmpty: Boolean = true
)

class InvalidSettingsException(setting: Any): Exception() {
	override val message: String = "Tried to save invalid settings: $setting"
}

fun Int.toBoolean(): Boolean {
	return this != 0
}