package vadimerenkov.aucards.widgets

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import coil3.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.koin.core.context.GlobalContext.get
import vadimerenkov.aucards.MainActivity
import vadimerenkov.aucards.R
import vadimerenkov.aucards.data.Aucard
import vadimerenkov.aucards.data.AucardDao
import vadimerenkov.aucards.ui.calculateContentColor

class CardWidget: GlanceAppWidget() {

	override val stateDefinition = PreferencesGlanceStateDefinition

	override suspend fun provideGlance(
		context: Context,
		id: GlanceId
	) {
		val dao: AucardDao = get().get()
		provideContent {
			// Read the card id inside the composition so the widget recomposes once
			// WidgetConfigureActivity stores it, even if the session was already running.
			val cardId = currentState(CARD_ID_KEY)
			val state by produceState<CardState>(CardState.Loading, cardId) {
				if (cardId != null) {
					value = dao.getAucardByID(cardId)
						.map<Aucard, CardState> { CardState.Loaded(it) }
						.catch { emit(CardState.Missing) }
						.first()
				}
			}
			when (val current = state) {
				is CardState.Loaded -> WidgetCard(current.card, context)
				CardState.Missing -> MissingCardWidget(context)
				CardState.Loading -> Box(modifier = GlanceModifier.fillMaxSize()) {}
			}
		}
	}

	companion object {
		val CARD_ID_KEY = intPreferencesKey("card_id")
	}
}

private sealed interface CardState {
	data object Loading : CardState
	data object Missing : CardState
	data class Loaded(val card: Aucard) : CardState
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

@Composable
private fun MissingCardWidget(context: Context) {
	Box(
		contentAlignment = Alignment.Center,
		modifier = GlanceModifier
			.fillMaxSize()
			.background(Color.DarkGray)
			.padding(16.dp)
	) {
		Text(
			text = context.getString(R.string.widget_card_not_found),
			style = TextStyle(
				color = ColorProvider(Color.White)
			)
		)
	}
}
