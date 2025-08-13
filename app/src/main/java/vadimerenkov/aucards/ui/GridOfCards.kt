@file:OptIn(ExperimentalSharedTransitionApi::class)

package vadimerenkov.aucards.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.lerp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState
import vadimerenkov.aucards.R
import vadimerenkov.aucards.data.Aucard

@Composable
fun GridOfCards(
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
						list.forEach { card ->
							card.index = list.indexOf(card)
						}
						onDragStopped(list)
					}
				)
			}
		}
	}
}

@Composable
private fun AucardItem(
	aucard: Aucard,
	onClick: (Int) -> Unit,
	onLongPress: () -> Unit,
	onFavourited: () -> Unit,
	onDragStopped: () -> Unit,
	animatedVisibilityScope: AnimatedVisibilityScope,
	dragScope: ReorderableCollectionItemScope,
	sharedTransitionScope: SharedTransitionScope,
	interactionSource: MutableInteractionSource,
	modifier: Modifier = Modifier,
	isSelectMode: Boolean = false,
	isSelected: Boolean = false
) {
	val color by animateColorAsState(
		targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.Transparent
	)
	val bg_color by animateColorAsState(
		targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else Color.Transparent
	)
	val textColor = remember { calculateContentColor(aucard.color) }
	val fav_color by animateColorAsState(
		targetValue = if (aucard.isFavourite) textColor else Color.Transparent
	)
	val border by animateDpAsState(
		targetValue = if (isSelected) 3.dp else 0.dp
	)
	val textSize by animateFloatAsState(
		if (isSelected) 1f else 0f
	)
	val elevation by animateDpAsState(
		targetValue = if (isSelected) 0.dp else 8.dp
	)


	with(sharedTransitionScope) {

		val contentState = rememberSharedContentState(
			SharedContentStateKey(
				aucard.id,
				ContentType.CARD,
				Target.VIEW
			)
		)

		val textContentState = rememberSharedContentState(
			SharedContentStateKey(
				aucard.id,
				ContentType.TEXT,
				Target.VIEW
			)
		)

		val editContentState = rememberSharedContentState(
			SharedContentStateKey(
				aucard.id,
				ContentType.CARD,
				Target.EDIT
			)
		)

		val editTextContentState = rememberSharedContentState(
			SharedContentStateKey(
				aucard.id,
				ContentType.TEXT,
				Target.EDIT
			)
		)

		ElevatedCard(
			colors = CardDefaults.cardColors(
				containerColor = aucard.color
			),
			elevation = CardDefaults.cardElevation(elevation),
			modifier = modifier
				.heightIn(max = 100.dp)
				.combinedClickable(
					enabled = true,
					onClick = {
						onClick(aucard.id)
					},
					onLongClick = {
						if (!isSelectMode) {
							onLongPress()
						}
					}
				)
				.border(
					border = BorderStroke(border, color),
					shape = MaterialTheme.shapes.medium
				)
				.sharedBounds(
					sharedContentState = if (!isSelectMode) contentState else editContentState,
					animatedVisibilityScope = animatedVisibilityScope,
					resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
				)
		) {
			Box {
				this@ElevatedCard.AnimatedVisibility(
					visible = isSelectMode,
					enter = scaleIn(),
					exit = scaleOut(),
					modifier = Modifier
						.align(Alignment.TopEnd)
						.zIndex(10f)
				) {
					Icon(
						imageVector = Icons.Default.CheckCircle,
						contentDescription = null,
						tint = color,
						modifier = Modifier
							.padding(8.dp)
							.border(
								width = 2.dp,
								color = Color.White,
								shape = CircleShape
							)
							.padding(2.dp)
							.border(
								width = 2.dp,
								color = Color.Black,
								shape = CircleShape
							)
							.background(
								color = bg_color,
								shape = CircleShape
							)
					)
				}
				this@ElevatedCard.AnimatedVisibility(
					visible = isSelectMode,
					enter = scaleIn(),
					exit = scaleOut(),
					modifier = Modifier
						.align(Alignment.BottomEnd)
						.zIndex(10f)
				) {
					Box(
						contentAlignment = Alignment.Center,
						modifier = Modifier
							.minimumInteractiveComponentSize()
							.clickable(
								onClick = onFavourited,
								onClickLabel = stringResource(R.string.mark_as_favourite)
							)
					) {
						Icon(
							imageVector = ImageVector.vectorResource(R.drawable.star_outlined),
							contentDescription = null,
							tint = Color.White,
							modifier = Modifier
								.size(32.dp)
						)
						Icon(
							imageVector = ImageVector.vectorResource(R.drawable.star_outlined),
							contentDescription = null,
							tint = Color.Black,
							modifier = Modifier
								.size(24.dp)
						)
						Icon(
							imageVector = Icons.Default.Star,
							contentDescription = null,
							tint = fav_color,
							modifier = Modifier
								.size(24.dp)
						)

					}
				}
				this@ElevatedCard.AnimatedVisibility(
					visible = isSelectMode,
					enter = scaleIn(
						transformOrigin = TransformOrigin(0.4f, 0.5f)
					),
					exit = scaleOut(
						transformOrigin = TransformOrigin(0.4f, 0.5f)
					),
					modifier = Modifier
						.align(Alignment.TopStart)
						.zIndex(10f)
				) {
					IconButton(
						onClick = {},
						modifier = with(dragScope) {
							Modifier
								.draggableHandle(
									onDragStopped = onDragStopped,
									interactionSource = interactionSource
								)
						}
					) {
						Icon(
							imageVector = ImageVector.vectorResource(R.drawable.drag),
							contentDescription = null,
							tint = textColor,
							modifier = Modifier
								.offset(x = (-6).dp)
						)
					}
				}

				Column(
					verticalArrangement = Arrangement.Center,
					horizontalAlignment = Alignment.CenterHorizontally,
					modifier = Modifier
						.fillMaxSize()
				) {
					Text(
						text = aucard.text,
						style = lerp(
							start = MaterialTheme.typography.titleLarge,
							stop = MaterialTheme.typography.titleSmall,
							fraction = textSize
						),
						color = textColor,
						textAlign = TextAlign.Center,
						modifier = Modifier
							.padding(4.dp)
							.sharedBounds(
								sharedContentState = if (!isSelectMode) textContentState else editTextContentState,
								animatedVisibilityScope = animatedVisibilityScope
							)
					)
				}
			}
		}
	}
}