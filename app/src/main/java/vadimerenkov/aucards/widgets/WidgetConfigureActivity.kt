package vadimerenkov.aucards.widgets

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.flow.first
import vadimerenkov.aucards.AucardsApplication
import vadimerenkov.aucards.R
import vadimerenkov.aucards.data.Aucard
import vadimerenkov.aucards.ui.calculateContentColor
import vadimerenkov.aucards.ui.theme.AucardsTheme

class WidgetConfigureActivity : ComponentActivity() {
	@OptIn(ExperimentalSharedTransitionApi::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()

		GlanceAppWidgetManager(this)

		val widgetId = intent?.extras?.getInt(
			AppWidgetManager.EXTRA_APPWIDGET_ID,
			AppWidgetManager.INVALID_APPWIDGET_ID
		) ?: AppWidgetManager.INVALID_APPWIDGET_ID

		setResult(RESULT_CANCELED)

		val app = this.application as AucardsApplication
		val database = app.database.aucardDao()

		setContent {
			var items: List<Aucard> by remember { mutableStateOf(emptyList()) }

			LaunchedEffect(true) {
				items = database.getAllCards().first()
				println(items)
			}

			AucardsTheme {

				Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
					Column(
						verticalArrangement = Arrangement.spacedBy(16.dp),
						modifier = Modifier
							.padding(innerPadding)
					) {
						Text(
							text = stringResource(R.string.choose_card),
							fontSize = 20.sp,
							modifier = Modifier
								.padding(horizontal = 16.dp)
						)
						LazyVerticalGrid(
							columns = GridCells.Adaptive(minSize = 150.dp),
							contentPadding = PaddingValues(12.dp),
							verticalArrangement = Arrangement.spacedBy(12.dp),
							horizontalArrangement = Arrangement.spacedBy(12.dp),
						) {
							items(
								items = items,
								key = { it.id }
							) { card ->
								ElevatedCard(
									onClick = {
										CardWidget().updateAll(this)
										val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
										setResult(RESULT_OK, resultValue)
										finish()
									},
									colors = CardDefaults.cardColors(
										containerColor = card.color
									),
									modifier = Modifier
										.heightIn(max = 100.dp)
								) {
									Text(
										text = card.text,
										color = calculateContentColor(card.color),
										modifier = Modifier
											.fillMaxSize()
											.wrapContentSize()
									)
								}
							}
						}
					}
				}
			}
		}
	}
}