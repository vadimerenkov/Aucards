package vadimerenkov.aucards.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import vadimerenkov.aucards.converters.Converters

@Database(
	entities = [Aucard::class],
	exportSchema = true,
	version = 2
)
@TypeConverters(Converters::class)
abstract class AucardsDatabase: RoomDatabase() {
	abstract fun aucardDao(): AucardDao

	companion object {
		@Volatile
		private var Instance: AucardsDatabase? = null

		fun getDatabase(context: Context): AucardsDatabase {
			return Instance ?: synchronized(this) {
				Room.databaseBuilder(context, AucardsDatabase::class.java, "aucards_database")
					.fallbackToDestructiveMigration(true)
					.build()
					.also { Instance = it }
			}
		}

	}
}