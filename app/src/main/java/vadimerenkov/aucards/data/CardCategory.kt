package vadimerenkov.aucards.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CardCategory(
	@PrimaryKey(autoGenerate = true) val id: Int = 0,
	val name: String = "",
	val index: Int = 0
)
