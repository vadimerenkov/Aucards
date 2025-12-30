package vadimerenkov.aucards.widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import kotlinx.coroutines.MainScope

class WidgetReceiver: GlanceAppWidgetReceiver() {

	override val glanceAppWidget: GlanceAppWidget = CardWidget()

	val scope = MainScope()

	override fun onUpdate(
		context: Context,
		appWidgetManager: AppWidgetManager,
		appWidgetIds: IntArray
	) {
		super.onUpdate(context, appWidgetManager, appWidgetIds)
		val manager = GlanceAppWidgetManager(context)
		val glanceIds = appWidgetIds.map { id ->
			manager.getGlanceIdBy(id)
		}.forEach { glanceId ->

		}
	}
}