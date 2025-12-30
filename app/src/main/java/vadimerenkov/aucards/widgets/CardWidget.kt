package vadimerenkov.aucards.widgets

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import coil3.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.koin.core.context.GlobalContext.get
import vadimerenkov.aucards.MainActivity
import vadimerenkov.aucards.data.Aucard
import vadimerenkov.aucards.data.AucardDao
import vadimerenkov.aucards.ui.calculateContentColor

class CardWidget: GlanceAppWidget() {

	var cardId: Int by mutableStateOf(0)

	override suspend fun provideGlance(
		context: Context,
		id: GlanceId
	) {
		val dao: AucardDao = get().get()
		val card = dao.getAucardByID(cardId).first()
		provideContent {
			WidgetCard(card, context)
		}
	}
}

@Composable
private fun WidgetCard(
	card: Aucard,
	context: Context
) {
	val activityIntent = Intent(context, MainActivity::class.java).apply {
		data = "vadimerenkov://aucards/${card.id}".toUri()
	}
	var bitmap: Bitmap? by remember { mutableStateOf(null) }

	LaunchedEffect(card.imagePath) {
		withContext(Dispatchers.IO) {
			bitmap = loadBitmap(card.imagePath, context)
		}
	}

	Box(
		contentAlignment = Alignment.Center,
		modifier = GlanceModifier
			.fillMaxSize()
			.background(card.color)
			.clickable(
				onClick = actionStartActivity(activityIntent)
			)
	) {
		bitmap?.let {
			Image(
				provider = ImageProvider(it),
				contentDescription = null,
				contentScale = ContentScale.Crop
			)
		}
		Text(
			text = card.text,
			style = TextStyle(
				color = ColorProvider(calculateContentColor(card.color))
			),
			modifier = GlanceModifier
				.background(card.color.copy(alpha = 0.6f))
				.padding(16.dp)
		)
	}
}