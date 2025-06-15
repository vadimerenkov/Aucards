package vadimerenkov.aucards.data

import androidx.compose.ui.graphics.Color
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class AucardDaoTest {

	private lateinit var database: AucardsDatabase
	private lateinit var dao: AucardDao

	val testAucard1 = Aucard(
		id = 1,
		text = "test1"
	)
	val testAucard2 = Aucard(
		id = 2,
		text = "test2",
		description = "test_description"
	)
	val testAucard3 = Aucard(
		id = 3,
		text = "test3",
		description = "test_description_2",
		color = Color.Yellow
	)

	@Before
	fun SetupDatabase() {
		database = Room.inMemoryDatabaseBuilder(
			context = ApplicationProvider.getApplicationContext(),
			klass = AucardsDatabase::class.java
		)
			.allowMainThreadQueries()
			.build()
		dao = database.aucardDao()
	}

	@After
	fun CloseDatabase() {
		database.close()
	}

	@Test
	fun InsertNewCard() = runTest {
		dao.saveAucard(testAucard1)
		val card = dao.getAucardByID(testAucard1.id).first()
		assertThat(card).isEqualTo(testAucard1)
	}

	@Test
	fun UpdateCard() = runTest {
		dao.saveAucard(testAucard2)

		val updated_card = testAucard2.copy(description = "another test description")
		dao.saveAucard(updated_card)

		val card = dao.getAucardByID(testAucard2.id).first()
		assertThat(card).isEqualTo(updated_card)
	}

	@Test
	fun QueryItems() = runTest {
		dao.saveAucard(testAucard1)
		dao.saveAucard(testAucard2)
		dao.saveAucard(testAucard3)

		val all_cards = dao.getAllCards().first()
		assertThat(all_cards).containsExactly(testAucard1, testAucard2, testAucard3)
	}

	@Test
	fun DeleteItem() = runTest {
		dao.saveAucard(testAucard1)
		dao.deleteById(testAucard1.id)

		val all_cards = dao.getAllCards().first()
		assertThat(all_cards).doesNotContain(testAucard1)
	}
}