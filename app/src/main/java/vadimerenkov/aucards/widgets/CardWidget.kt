package vadimerenkov.aucards.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import kotlinx.coroutines.flow.first
import org.koin.compose.koinInject
import vadimerenkov.aucards.data.Aucard
import vadimerenkov.aucards.data.AucardDao
import vadimerenkov.aucards.ui.calculateContentColor

class CardWidget: GlanceAppWidget() {

	var cardId: Int = 0

	override suspend fun provideGlance(
		context: Context,
		id: GlanceId
	) {
		provideContent {
			WidgetCard(cardId)
		}
	}
}

@Composable
private fun WidgetCard(
	cardId: Int
) {
	val dao = koinInject<AucardDao>()
	var card by remember { mutableStateOf(Aucard(text = "")) }

	LaunchedEffect(cardId) {
		card = dao.getAucardByID(cardId).first()
	}

	Box(
		contentAlignment = Alignment.Center,
		modifier = GlanceModifier
			.fillMaxSize()
			.background(card.color)
	) {
		Text(
			text = card.text,
			style = TextStyle(
				color = ColorProvider(calculateContentColor(card.color))
			)
		)
	}
}