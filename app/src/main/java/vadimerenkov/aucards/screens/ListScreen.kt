@file:OptIn( ExperimentalAnimationApi::class,
	ExperimentalSharedTransitionApi::class
)

package vadimerenkov.aucards.screens

import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.lerp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import sh.calvin.reorderable.ReorderableCollectionItemScope
import vadimerenkov.aucards.R
import vadimerenkov.aucards.ViewModelFactory
import vadimerenkov.aucards.data.Aucard
import vadimerenkov.aucards.ui.AucardsTopBar
import vadimerenkov.aucards.ui.ContentType
import vadimerenkov.aucards.ui.ListViewModel
import vadimerenkov.aucards.ui.SharedContentStateKey
import vadimerenkov.aucards.ui.Target
import vadimerenkov.aucards.ui.calculateContentColor

private const val TAG = "ListScreen"

@Composable
fun ListScreen(
	onCardClicked: (Int) -> Unit,
	onCardEditClicked: (Int) -> Unit,
	onAddButtonClicked: (Int) -> Unit,
	onSettingsClicked: () -> Unit,
	animatedVisibilityScope: AnimatedVisibilityScope,
	sharedTransitionScope: SharedTransitionScope,
	modifier: Modifier = Modifier,
	viewModel: ListViewModel = viewModel(factory = ViewModelFactory.Factory())
) {
	val listState by viewModel.listState.collectAsState()
	var deleteConfirmationOpen by remember { mutableStateOf(false) }

	// Set screen orientation back to user-specified.
	val activity = LocalActivity.current
	activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER

	// Exit selection mode on back pressed
	BackHandler(listState.isSelectMode) {
		viewModel.exitSelectMode()
	}

	if (deleteConfirmationOpen) {
		AlertDialog(
			onDismissRequest = { deleteConfirmationOpen = false },
			text = { Text(pluralStringResource(R.plurals.delete_confirmation, listState.selectedList.size, listState.selectedList.size)) },
			dismissButton = {
				TextButton(
					onClick = { deleteConfirmationOpen = false }
				) {
					Text(stringResource(R.string.cancel))
				}
			},
			confirmButton = {
				Button(
					onClick = {
						viewModel.deleteSelected()
						deleteConfirmationOpen = false
					}
				) {
					Text(stringResource(R.string.delete_button))
				}
			})
	}

	Scaffold(
		topBar = {
			AucardsTopBar(
				selectedNumber = listState.selectedList.size,
				onDeleteClick = {
					deleteConfirmationOpen = true
				},
				onEditClick = {
					onCardEditClicked(listState.selectedList[0])
					viewModel.exitSelectMode()
				},
				onSettingsClick = {
					onSettingsClicked()
					viewModel.exitSelectMode()
				},
				isSelectMode = listState.isSelectMode,
				isEditEnabled = listState.selectedList.size == 1,
				isDeleteEnabled = listState.selectedList.isNotEmpty(),
				currentPage = listState.currentPage
			)
		},
		bottomBar = {
			NavigationBar(
				containerColor = MaterialTheme.colorScheme.primaryContainer,
				contentColor = MaterialTheme.colorScheme.onPrimaryContainer
			) {
				NavigationBarItem(
					selected = listState.currentPage == 0,
					onClick = { viewModel.turnPage(0) },
					icon = {
						Icon(
							painterResource(R.drawable.grid),
							contentDescription = null
						)
					},
					label = {
						Text(text = stringResource(R.string.all_cards))
					},
					colors = NavigationBarItemDefaults.colors(
						indicatorColor = MaterialTheme.colorScheme.onPrimaryContainer,
						selectedIconColor = MaterialTheme.colorScheme.onPrimary
					)
				)
				NavigationBarItem(
					selected = listState.currentPage == 1,
					onClick = { viewModel.turnPage(1) },
					icon = {
						Icon(
							imageVector = Icons.Outlined.Star,
							contentDescription = null
						)
					},
					label = {
						Text(text = stringResource(R.string.favourites))
					},
					colors = NavigationBarItemDefaults.colors(
						indicatorColor = MaterialTheme.colorScheme.onPrimaryContainer,
						selectedIconColor = MaterialTheme.colorScheme.onPrimary
					)
				)
			}
		},
		floatingActionButton = {
			with(sharedTransitionScope) {
				val contentState = rememberSharedContentState(
					SharedContentStateKey(
						0,
						ContentType.CARD,
						Target.EDIT
					)
				)
				FloatingActionButton(
					onClick = {
						onAddButtonClicked(listState.list.size + 1)
						viewModel.exitSelectMode()
					},
					shape = MaterialTheme.shapes.medium,
					modifier = Modifier
						.padding(16.dp)
						.sharedBounds(
							sharedContentState = contentState,
							animatedVisibilityScope = animatedVisibilityScope
						)
				) {
					Icon(
						imageVector = Icons.Default.Add,
						contentDescription = stringResource(R.string.add_card)
					)
				}

			}
		}
	) { innerPadding ->
		Box(
			modifier = modifier
				.fillMaxSize()
				.padding(innerPadding)
		) {
			val pager_state = rememberPagerState { 2 }

			LaunchedEffect(listState.currentPage) {
				pager_state.animateScrollToPage(listState.currentPage)
			}

			LaunchedEffect(pager_state.currentPage) {
				viewModel.turnPage(pager_state.currentPage)
			}

			HorizontalPager(
				state = pager_state,
				modifier = modifier
					.fillMaxSize(),
			) { page ->
				when (page) {
					0 -> {
						AnimatedContent(targetState = listState.isLoading) {
							if (it) {
								CircularProgressIndicator(
									modifier = Modifier
										.fillMaxSize()
										.wrapContentSize()
										.align(Alignment.Center)
								)
							} else if (listState.list.isEmpty()) {
								Box(
									contentAlignment = Alignment.Center,
									modifier = Modifier
										.fillMaxSize()
								) {
									Text(
										text = stringResource(R.string.empty_list_prompt),
										style = MaterialTheme.typography.bodyLarge,
										textAlign = TextAlign.Center,
										color = Color.Gray
									)
								}
							} else {
								SimpleReorderableLazyVerticalGridScreen(
									items = listState.list,
									selectedList = listState.selectedList,
									sharedTransitionScope = sharedTransitionScope,
									animatedVisibilityScope = animatedVisibilityScope,
									onCardClick = onCardClicked,
									onFavourited = {
										viewModel.markAsFavourite(it)
									},
									onLongPress = {
										viewModel.enterSelectMode(it)
									},
									isSelectMode = listState.isSelectMode,
									onSelect = { id ->
										viewModel.selectId(id)
									},
									onDeselect = { id ->
										viewModel.deselectId(id)
									},
									onDragStopped = { cards ->
										viewModel.saveAllCards(cards)
									}
								)
							}
						}
					}
					1 -> {
						SimpleReorderableLazyVerticalGridScreen(
							items = listState.favouritesList,
							selectedList = listState.selectedList,
							sharedTransitionScope = sharedTransitionScope,
							animatedVisibilityScope = animatedVisibilityScope,
							onCardClick = onCardClicked,
							onFavourited = {
								viewModel.markAsFavourite(it)
							},
							onLongPress = {
								viewModel.enterSelectMode(it)
							},
							isSelectMode = listState.isSelectMode,
							onSelect = { id ->
								viewModel.selectId(id)
							},
							onDeselect = { id ->
								viewModel.deselectId(id)
							},
							onDragStopped = { cards ->
								viewModel.saveAllCards(cards)
							}
						)
					}
				}
			}



			/*

			HorizontalPager(
				state = pager_state,
				modifier = Modifier
					.fillMaxSize()
			) { page ->
				when (page) {
					0 -> {
						if (listState.isLoading) {
							Box(
								contentAlignment = Alignment.Center,
								modifier = Modifier
									.fillMaxSize()
							) {
								CircularProgressIndicator()
							}
						}
						else if (listState.list.isEmpty()) {
							Box(
								contentAlignment = Alignment.Center,
								modifier = Modifier
									.fillMaxSize()
							) {
								Text(
									text = stringResource(R.string.empty_list_prompt),
									style = MaterialTheme.typography.bodyLarge,
									textAlign = TextAlign.Center,
									color = Color.Gray
								)
							}
						}
						else {
							//SimpleReorderableLazyVerticalGridScreen()

							GridOfCards(
								list = listState.list,
								letter = "A",
								onSelect = {
									viewModel.SelectId(it)
								},
								onDeselect = {
									viewModel.DeselectId(it)
								},
								onCardClicked = {
									onCardClicked(it)
								},
								onSelectModeEntered = {
									viewModel.EnterSelectMode(it)
								},
								onFavourited = {
									viewModel.MarkAsFavourite(it)
								},
								animatedVisibilityScope = animatedVisibilityScope,
								isSelectMode = listState.isSelectMode,
								selectedList = listState.selectedList,
								sharedTransitionScope = sharedTransitionScope
							)


						}
					}
					1 -> {
						if (listState.favouritesList.isEmpty()) {
							Box(
								contentAlignment = Alignment.Center,
								modifier = Modifier
									.fillMaxSize()
							) {
								Text(
									text = stringResource(R.string.add_to_fav_prompt),
									style = MaterialTheme.typography.bodyLarge,
									textAlign = TextAlign.Center,
									color = Color.Gray
								)
							}
						} else {
							GridOfCards(
								list = listState.favouritesList,
								letter = "F",
								onSelect = {
									viewModel.SelectId(it)
								},
								onDeselect = {
									viewModel.DeselectId(it)
								},
								onCardClicked = {
									onCardClicked(it)
								},
								onSelectModeEntered = {
									viewModel.EnterSelectMode(it)
								},
								onFavourited = {
									viewModel.MarkAsFavourite(it)
								},
								animatedVisibilityScope = animatedVisibilityScope,
								isSelectMode = listState.isSelectMode,
								selectedList = listState.selectedList,
								sharedTransitionScope = sharedTransitionScope
							)
						}
					}
				}


			}

			 */
		}
	}
}

@Composable
private fun GridOfCards(
	list: List<Aucard>,
	letter: String,
	onSelect: (Int) -> Unit,
	onDeselect: (Int) -> Unit,
	onCardClicked: (Int) -> Unit,
	onSelectModeEntered: (Int) -> Unit,
	onFavourited: (Int) -> Unit,
	isSelectMode: Boolean,
	selectedList: List<Int>,
	animatedVisibilityScope: AnimatedVisibilityScope,
	sharedTransitionScope: SharedTransitionScope,
	modifier: Modifier = Modifier
) {
	/*
	var visibleList by remember { mutableStateOf(list) }
	val state = rememberLazyGridState()
	val reorderableState =
		rememberReorderableLazyGridState(state) { from, to ->
			Log.d(TAG, "AAAAA")
			visibleList = visibleList.toMutableList().apply {
				add(to.index, removeAt(from.index))
			}
		}

	LazyVerticalGrid(
		columns = GridCells.Adaptive(150.dp),
		horizontalArrangement = Arrangement.SpaceBetween,
		state = state,
		modifier = modifier
			.padding(8.dp)
			.fillMaxSize()
	) {
		itemsIndexed(
			items = visibleList,
			key = { index, card ->
				card.id }
		) { index, card ->
			val isSelected = selectedList.contains(card.id)
			ReorderableItem(
				state = reorderableState,
				key = { card.id }
			) {isDragging ->
				val interactionSource = remember { MutableInteractionSource() }
				AucardItem(
					aucard = card,
					onClick = { id ->
						if (isSelectMode) {
							if (isSelected) {
								onDeselect(id)
							} else {
								onSelect(id)
							}
						} else {
							onCardClicked(id)
						}
					},
					onLongPress = {
						onSelectModeEntered(card.id)
					},
					onFavourited = { onFavourited(card.id) },
					//animatedVisibilityScope = animatedVisibilityScope,
					dragScope = this,
					modifier = Modifier
						.padding(6.dp),
						//.animateItem(),
					isSelectMode = isSelectMode,
					isSelected = isSelected,
					interactionSource = interactionSource,
					//sharedTransitionScope = sharedTransitionScope,
					onDrag = {

					}
				)
			}
		}
	}

	 */
}

@Composable
fun AucardItem(
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
	val fav_color by animateColorAsState(
		targetValue = if (aucard.isFavourite) Color.Black else Color.Transparent
	)
	val border by animateDpAsState(
		targetValue = if (isSelected) 3.dp else 0.dp
	)
	val textSize by animateFloatAsState(
		if (isSelected) 1f else 0f
	)
	val textColor = remember { calculateContentColor(aucard.color) }

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
		elevation = CardDefaults.cardElevation(6.dp),
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
				enter = scaleIn(),
				exit = scaleOut(),
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
						imageVector = Icons.Default.Menu,
						contentDescription = null
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