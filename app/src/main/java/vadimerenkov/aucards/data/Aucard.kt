package vadimerenkov.aucards.data

import androidx.compose.ui.graphics.Color
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Aucard(
	@PrimaryKey(autoGenerate = true) val id: Int = 0,
	val text: String,
	@ColumnInfo(defaultValue = "false")
	val isFavourite: Boolean = false,
	val description: String? = null,
	val color: Color = Color.White,
	val backgroundImageUri: String? = null
)
