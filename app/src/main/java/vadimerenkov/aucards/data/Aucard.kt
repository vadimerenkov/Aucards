package vadimerenkov.aucards.data

import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import vadimerenkov.aucards.R

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
	val imagePath: Uri? = null,
	@ColumnInfo(defaultValue = "1.0") val imageScale: Float = 1f,
	@ColumnInfo(defaultValue = "0") val imageOffset: Offset = Offset.Zero,
	@ColumnInfo(defaultValue = "0.0") val imageRotation: Float = 0f,
	@ColumnInfo(defaultValue = "0.5") val textBackgroundOpacity: Float = 0.5f,
	@ColumnInfo(defaultValue = "0") var index: Int = 0
)

enum class CardLayout(
	@DrawableRes val icon: Int,
	@StringRes val description: Int
) {
	TITLE_SUBTITLE(R.drawable.title_layout, R.string.title_subtitle),
	TWO_HALVES(R.drawable.split, R.string.two_halves)
}