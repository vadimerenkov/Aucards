package vadimerenkov.aucards.data

import androidx.compose.ui.graphics.Color
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Aucard(
	@PrimaryKey(autoGenerate = true) val id: Int = 0,
	val text: String,
	@ColumnInfo(defaultValue = "false") val isFavourite: Boolean = false,
	val description: String? = null,
	val color: Color = Color.White,
	@ColumnInfo(defaultValue = "57") val titleFontSize: Int = 57,
	@ColumnInfo(defaultValue = "24") val descriptionFontSize: Int = 24,
	@ColumnInfo(defaultValue = "TITLE_SUBTITLE") val layout: CardLayout = CardLayout.TITLE_SUBTITLE,
	@ColumnInfo(defaultValue = "0") var index: Int = 0
)

enum class CardLayout {
	TITLE_SUBTITLE,
	TWO_HALVES
}