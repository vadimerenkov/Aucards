package vadimerenkov.aucards.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.Text
import vadimerenkov.aucards.R

class CardWidget: GlanceAppWidget() {

	override suspend fun provideGlance(
		context: Context,
		id: GlanceId
	) {
		provideContent {
			WidgetCard(context)
		}
	}

	@Composable
	private fun WidgetCard(
		context: Context
	) {
		Box(
			contentAlignment = Alignment.Center,
			modifier = GlanceModifier
				.fillMaxSize()
				.background(Color.Cyan)
		) {
			Text(
				text = context.getString(R.string.choose_card)
			)
		}
	}
}