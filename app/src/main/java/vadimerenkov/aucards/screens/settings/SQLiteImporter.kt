package vadimerenkov.aucards.screens.settings

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.core.net.toUri
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import vadimerenkov.aucards.R
import vadimerenkov.aucards.data.Aucard
import vadimerenkov.aucards.data.AucardsDatabase
import vadimerenkov.aucards.data.CardCategory
import vadimerenkov.aucards.data.CardLayout
import vadimerenkov.aucards.ui.SnackbarController
import vadimerenkov.aucards.ui.code
import vadimerenkov.aucards.ui.getPluralString
import vadimerenkov.aucards.util.toBoolean
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream


class SQLiteImporter(
	private val context: Context,
	private val database: AucardsDatabase
) {
	private val TAG = "SQLiteImporter"

	suspend fun exportDatabase(uri: Uri) {
		withContext(Dispatchers.IO) {
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

	suspend fun importDatabase(uri: Uri) {
		withContext(Dispatchers.IO) {
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
				val categories = mutableListOf<CardCategory>()
				var index = database.aucardDao().getAllCategories().first().size

				SQLiteDatabase
					.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)
					.use { database ->
						database.rawQuery("SELECT * FROM aucard", null)
							.use { cursor ->
								if (cursor.moveToFirst()) {
									do {
										val card = importCard(cursor).copy(index = current_index + 1)
										cards.add(card)
										current_index++

									} while (cursor.moveToNext())
								}
							}
						database.rawQuery("SELECT * FROM cardcategory", null)
							.use { cursor ->
								if (cursor.moveToFirst()) {
									do {
										val name = cursor.getColumnIndex("name")
										val name_value = cursor.getString(name)
										val category = CardCategory(name = name_value, index = index + 1)
										index++
										categories.add(category)
									} while (cursor.moveToNext())
								}
							}
					}

				database.aucardDao().saveAllCards(cards)
				categories.forEach {
					database.aucardDao().saveCategory(it)
				}

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
}