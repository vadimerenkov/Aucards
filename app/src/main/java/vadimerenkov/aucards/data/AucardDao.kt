package vadimerenkov.aucards.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AucardDao {
	@Upsert
	suspend fun saveAucard(aucard: Aucard)

	@Query("SELECT * FROM aucard")
	fun getAllCards(): Flow<List<Aucard>>

	@Query("SELECT * FROM aucard WHERE id = :id")
	fun getAucardByID(id: Int): Flow<Aucard>

	@Query("DELETE FROM aucard WHERE id = :id")
	suspend fun deleteById(id: Int)
}