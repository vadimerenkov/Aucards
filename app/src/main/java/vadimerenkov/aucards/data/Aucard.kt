package vadimerenkov.aucards.data

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Aucard(
	@PrimaryKey(autoGenerate = true) val id: Int = 0,
	val text: String,
	val title: String? = null,
	val description: String? = null,
	val color: Color = Color.White
)
