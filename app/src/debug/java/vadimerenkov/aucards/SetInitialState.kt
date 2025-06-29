package vadimerenkov.aucards

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import vadimerenkov.aucards.data.AucardDao

@Composable
fun SetInitialState(
	scope: LifecycleCoroutineScope,
	dao: AucardDao
) {
	val start_list = CreateStarters()

	LaunchedEffect(true) {
		scope.launch {
			dao.getAllCards().first().forEach { card ->
				dao.deleteById(card.id)
			}
			start_list.forEach { card ->
				dao.saveAucard(card)
			}
		}
	}
}