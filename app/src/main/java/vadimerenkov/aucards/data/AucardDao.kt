package vadimerenkov.aucards.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AucardDao {
	@Upsert
	suspend fun saveAucard(aucard: Aucard)

	@Upsert
	suspend fun saveAllCards(cards: List<Aucard>)

	@Query("SELECT * FROM aucard ORDER BY `index`")
	fun getAllCards(): Flow<List<Aucard>>

	@Query("SELECT * FROM aucard WHERE id = :id")
	fun getAucardByID(id: Int): Flow<Aucard>

	@Query("DELETE FROM aucard WHERE id = :id")
	suspend fun deleteById(id: Int)

	@Query("SELECT * FROM aucard WHERE isFavourite = 1 ORDER BY `index`")
	fun getFavouriteCards(): Flow<List<Aucard>>


	@Upsert
	suspend fun saveCategory(category: CardCategory)

	@Delete
	suspend fun deleteCategory(category: CardCategory)

	@Query("SELECT * FROM cardcategory ORDER BY `index`")
	fun getAllCategories(): Flow<List<CardCategory>>
}