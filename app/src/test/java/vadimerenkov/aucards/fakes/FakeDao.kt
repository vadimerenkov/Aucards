package vadimerenkov.aucards.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import vadimerenkov.aucards.data.Aucard
import vadimerenkov.aucards.data.AucardDao

class FakeDao : AucardDao {
	private val list = mutableListOf<Aucard>()

	override suspend fun saveAucard(aucard: Aucard) {
		list.add(aucard)
	}

	override fun getAllCards(): Flow<List<Aucard>> {
		return flowOf(list)
	}

	override fun getAucardByID(id: Int): Flow<Aucard> {
		val card = list.find { it ->
			it.id == id
		}
		if (card != null) {
			return flowOf(card)
		}
		else {
			throw NoSuchElementException()
		}
	}

	override suspend fun deleteById(id: Int) {
		val card = list.find { it ->
			it.id == id
		}
		list.remove(card)
	}
}