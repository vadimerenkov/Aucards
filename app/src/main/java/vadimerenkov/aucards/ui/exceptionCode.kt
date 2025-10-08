package vadimerenkov.aucards.ui

import android.database.sqlite.SQLiteException
import java.io.FileNotFoundException
import java.io.IOException
import java.util.zip.ZipException

fun Exception.code(): Int {
	return when (this) {
		is FileNotFoundException -> 1
		is IllegalArgumentException -> 2
		is ZipException -> 3
		is IOException -> 4
		is SQLiteException -> 5
		else -> 0
	}
}