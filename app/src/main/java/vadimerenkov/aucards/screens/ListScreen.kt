@file:OptIn( ExperimentalAnimationApi::class,
	ExperimentalSharedTransitionApi::class
)

package vadimerenkov.aucards.screens

import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.lerp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import vadimerenkov.aucards.R
import vadimerenkov.aucards.ViewModelFactory
import vadimerenkov.aucards.data.Aucard
import vadimerenkov.aucards.ui.AucardsTopBar
import vadimerenkov.aucards.ui.ListViewModel

@Composable
fun SharedTransitionScope.ListScreen(
	onCardClicked: (Int) -> Unit,
	onCardEditClicked: (Int) -> Unit,
	onAddButtonClicked: () -> Unit,
	onSettingsClicked: () -> Unit,
	scope: AnimatedVisibilityScope,
	modifier: Modifier = Modifier,
	viewModel: ListViewModel = viewModel(factory = ViewModelFactory.Factory)
) {
	val listState by viewModel.listState.collectAsState()
	var deleteConfirmationOpen by remember { mutableStateOf(false) }

	// Set screen orientation back to user-specified.
	val activity = LocalActivity.current
	activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER

	// Exit selection mode on back pressed
	BackHandler(listState.isSelectMode) {
		viewModel.ExitSelectMode()
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
						viewModel.DeleteSelected()
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
					viewModel.ExitSelectMode()
				},
				onSettingsClick = {
					onSettingsClicked()
					viewModel.ExitSelectMode()
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
					onClick = { viewModel.TurnPage(0) },
					icon = {
						Icon(
							painterResource(R.drawable.grid),
							contentDescription = stringResource(R.string.all_cards),
							tint = if (listState.currentPage == 0) {
								MaterialTheme.colorScheme.onPrimary
							} else {
								MaterialTheme.colorScheme.onPrimaryContainer
							}
						)
					},
					colors = NavigationBarItemDefaults.colors(
						indicatorColor = MaterialTheme.colorScheme.onPrimaryContainer
					),
					modifier = Modifier
						.weight(0.5f)
				)
				NavigationBarItem(
					selected = listState.currentPage == 1,
					onClick = { viewModel.TurnPage(1) },
					icon = {
						Icon(
							imageVector = Icons.Outlined.Star,
							contentDescription = stringResource(R.string.favourites),
							tint = if (listState.currentPage == 1) {
								MaterialTheme.colorScheme.onPrimary
							} else {
								MaterialTheme.colorScheme.onPrimaryContainer
							}
						)
					},
					colors = NavigationBarItemDefaults.colors(
						indicatorColor = MaterialTheme.colorScheme.onPrimaryContainer
					),
					modifier = Modifier
						.weight(0.5f)
				)
			}
		},
		floatingActionButton = {
			FloatingActionButton(
				onClick = {
					onAddButtonClicked()
					viewModel.ExitSelectMode()
				},
				shape = MaterialTheme.shapes.medium,
				modifier = Modifier
					.padding(6.dp)
					.navigationBarsPadding()
					.sharedBounds(
						sharedContentState = rememberSharedContentState(0),
						animatedVisibilityScope = scope
					)
			) {
				Icon(
					imageVector = Icons.Default.Add,
					contentDescription = stringResource(R.string.add_card)
				)
			}
		},
		modifier = modifier
	) { innerPadding ->
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(innerPadding)
		) {
			val pager_state = rememberPagerState { 2 }

			LaunchedEffect(listState.currentPage) {
				pager_state.animateScrollToPage(listState.currentPage)
			}

			LaunchedEffect(pager_state.currentPage) {
				viewModel.TurnPage(pager_state.currentPage)
			}

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
							GridOfCards(
								list = listState.list,
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
								scope = scope,
								isSelectMode = listState.isSelectMode,
								selectedList = listState.selectedList
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
								scope = scope,
								isSelectMode = listState.isSelectMode,
								selectedList = listState.selectedList
							)
						}
					}
				}
			}
		}
	}
}

@Composable
fun SharedTransitionScope.GridOfCards(
	list: List<Aucard>,
	onSelect: (Int) -> Unit,
	onDeselect: (Int) -> Unit,
	onCardClicked: (Int) -> Unit,
	onSelectModeEntered: (Int) -> Unit,
	onFavourited: (Int) -> Unit,
	isSelectMode: Boolean,
	selectedList: List<Int>,
	scope: AnimatedVisibilityScope,
	modifier: Modifier = Modifier

) {
	Box(
		modifier = modifier
			.fillMaxSize()
	) {
		LazyVerticalGrid(
			columns = GridCells.Adaptive(150.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			modifier = Modifier
				.padding(6.dp)
		) {
			items(
				items = list,
				key = { it.id }
			) { card ->
				val isSelected = selectedList.contains(card.id)
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
					isSelectMode = isSelectMode,
					isSelected = isSelected,
					onFavourited = { onFavourited(card.id) },
					modifier = Modifier
						.padding(6.dp)
						.animateItem()
						.sharedBounds(
							sharedContentState = rememberSharedContentState(card.id),
							animatedVisibilityScope = scope
						)
				)
			}
		}
	}
}

@Composable
fun AucardItem(
	aucard: Aucard,
	onClick: (Int) -> Unit,
	onLongPress: () -> Unit,
	onFavourited: () -> Unit,
	modifier: Modifier = Modifier,
	isSelectMode: Boolean = false,
	isSelected: Boolean = false,
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

	ElevatedCard(
		colors = CardDefaults.cardColors(
			containerColor = aucard.color
		),
		elevation = CardDefaults.cardElevation(6.dp),
		modifier = modifier
			.heightIn(max = 100.dp)
			.combinedClickable(
				enabled = true,
				onClick = { onClick(aucard.id) },
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
			androidx.compose.animation.AnimatedVisibility(
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
			androidx.compose.animation.AnimatedVisibility(
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
						//.padding(8.dp)
						.minimumInteractiveComponentSize()
						.clickable(
							onClick = onFavourited,
							onClickLabel = stringResource(R.string.mark_as_favourite)
						)
				) {
					Icon(
						painter = painterResource(R.drawable.star_outlined),
						contentDescription = null,
						tint = Color.White,
						modifier = Modifier
							.size(32.dp)
					)
					Icon(
						painter = painterResource(R.drawable.star_outlined),
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
					color = Color.Black,
					textAlign = TextAlign.Center,
					modifier = Modifier
						.padding(4.dp)
				)
			}
		}
	}
}