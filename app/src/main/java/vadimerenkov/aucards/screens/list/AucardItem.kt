package vadimerenkov.aucards.screens.list

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
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
import vadimerenkov.aucards.R
import vadimerenkov.aucards.data.Aucard
import vadimerenkov.aucards.ui.ContentType
import vadimerenkov.aucards.ui.SharedContentStateKey
import vadimerenkov.aucards.ui.Target
import vadimerenkov.aucards.ui.calculateContentColor

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun AucardItem(
	aucard: Aucard,
	onClick: (Int) -> Unit,
	onLongPress: () -> Unit,
	onFavourited: (Boolean) -> Unit,
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
		targetValue = if (isSelected) Color.White else Color.Transparent
	)
	val overlayColor by animateColorAsState(
		targetValue = if (isSelectMode) Color.Black.copy(alpha = 0.3f) else Color.Transparent
	)
	val favColor by animateColorAsState(
		targetValue = if (aucard.isFavourite) Color.Red else Color.White
	)
	val textColor = calculateContentColor(aucard.color)
	val border by animateDpAsState(
		targetValue = if (isSelected) 4.dp else 0.dp
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
				.sharedBounds(
					sharedContentState = if (!isSelectMode) contentState else editContentState,
					animatedVisibilityScope = animatedVisibilityScope,
					resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
				)
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

		) {
			Box {
				this@ElevatedCard.AnimatedVisibility(
					visible = true
				) {
					Box(
						modifier = Modifier
							.fillMaxSize()
							.background(
								brush = Brush.horizontalGradient(
									listOf(
										Color.Transparent, overlayColor
									)
								)
							)
					)
				}
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
							.padding(12.dp)
							.border(
								width = 3.dp,
								color = Color.White,
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
					IconToggleButton(
						checked = aucard.isFavourite,
						onCheckedChange = onFavourited
					)  {
						Icon(
							imageVector = if (aucard.isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
							contentDescription = stringResource(R.string.mark_as_favourite),
							tint = favColor
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
							tint = textColor.copy(alpha = 0.5f),
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
							start = MaterialTheme.typography.titleMedium,
							stop = MaterialTheme.typography.titleSmall,
							fraction = textSize
						),
						color = textColor,
						textAlign = TextAlign.Center,
						modifier = Modifier
							.padding(8.dp)
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