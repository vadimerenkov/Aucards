package vadimerenkov.aucards.data

import androidx.compose.ui.graphics.Color
import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Aucard(
	@PrimaryKey(autoGenerate = true) val id: Int = 0,
	val text: String,
	@ColumnInfo(defaultValue = "false")
	val isFavourite: Boolean = false,
	val description: String? = null,
	val color: Color = Color.White
)

@DatabaseView("SELECT * FROM aucard WHERE isFavourite = 1")
data class FavAucard(
	val id: Int,
	val text: String,
	val isFavourite: Boolean,
	val description: String?,
	val color: Color
)

fun FavAucard.toAucard(): Aucard {
	return Aucard(
		id = id,
		text = text,
		isFavourite = isFavourite,
		description = description,
		color = color
	)
}