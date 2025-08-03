package vadimerenkov.aucards.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import vadimerenkov.aucards.converters.Converters

@Database(
	entities = [Aucard::class],
	views = [FavAucard::class],
	exportSchema = true,
	version = 4,
	autoMigrations = [
		AutoMigration(2, 3,
			spec = AucardsDatabase.Migrations::class
		),
		AutoMigration(3, 4)
	]
)
@TypeConverters(Converters::class)
abstract class AucardsDatabase: RoomDatabase() {
	abstract fun aucardDao(): AucardDao

	@DeleteColumn(
		"Aucard",
		"title"
	)
	class Migrations: AutoMigrationSpec {

	}


	companion object {
		@Volatile
		private var Instance: AucardsDatabase? = null

		fun getDatabase(context: Context): AucardsDatabase {
			return Instance ?: synchronized(this) {
				Room.databaseBuilder(context, AucardsDatabase::class.java, "aucards_database")
					//.fallbackToDestructiveMigration(true)
					.build()
					.also { Instance = it }
			}
		}

	}
}