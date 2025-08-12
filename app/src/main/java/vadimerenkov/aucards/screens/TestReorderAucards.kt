package vadimerenkov.aucards.screens

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState
import vadimerenkov.aucards.data.Aucard

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SimpleReorderableLazyVerticalGridScreen(
	items: List<Aucard>,
	selectedList: List<Int>,
	animatedVisibilityScope: AnimatedVisibilityScope,
	sharedTransitionScope: SharedTransitionScope,
	onCardClick: (Int) -> Unit,
	onFavourited: (Int) -> Unit,
	onLongPress: (Int) -> Unit,
	isSelectMode: Boolean,
	onSelect: (Int) -> Unit,
	onDeselect: (Int) -> Unit,
	onDragStopped: (List<Aucard>) -> Unit
) {

	var list by remember { mutableStateOf(items) }
	list = items
	val lazyGridState = rememberLazyGridState()
	val reorderableLazyGridState = rememberReorderableLazyGridState(lazyGridState) { from, to ->

		list = list.toMutableList().apply {
			add(to.index, removeAt(from.index))
		}
	}

	LazyVerticalGrid(
		columns = GridCells.Adaptive(minSize = 150.dp),
		modifier = Modifier.fillMaxSize(),
		state = lazyGridState,
		contentPadding = PaddingValues(8.dp),
		verticalArrangement = Arrangement.spacedBy(8.dp),
		horizontalArrangement = Arrangement.spacedBy(8.dp),
	) {
		itemsIndexed(list, key = { _, item -> item.id }) { index, item ->
			ReorderableItem(reorderableLazyGridState, item.id) {
				val interactionSource = remember { MutableInteractionSource() }
				val isSelected = selectedList.contains(item.id)

				AucardItem(
					aucard = item,
					onClick = { id ->
						if (isSelectMode) {
							if (isSelected) {
								onDeselect(id)
							} else {
								onSelect(id)
							}
						} else {
							onCardClick(id)
						}
					},
					onFavourited = {
						onFavourited(item.id)
					},
					onLongPress = {
						onLongPress(item.id)
					},
					interactionSource = interactionSource,
					dragScope = this@ReorderableItem,
					animatedVisibilityScope = animatedVisibilityScope,
					sharedTransitionScope = sharedTransitionScope,
					isSelectMode = isSelectMode,
					isSelected = isSelected,
					onDragStopped = {
						onDragStopped(list)
					}
				)
			}
		}
	}
}