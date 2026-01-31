@file:OptIn( ExperimentalAnimationApi::class,
	ExperimentalSharedTransitionApi::class
)

package vadimerenkov.aucards.screens.list

import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import vadimerenkov.aucards.R
import vadimerenkov.aucards.ui.ContentType
import vadimerenkov.aucards.ui.SharedContentStateKey
import vadimerenkov.aucards.ui.Target
import vadimerenkov.aucards.ui.applyIf

private const val TAG = "ListScreen"

@Composable
fun ListScreen(
	viewModel: ListViewModel,
	onCardClicked: (Int) -> Unit,
	onCardEditClicked: (Int) -> Unit,
	onAddButtonClicked: (Int) -> Unit,
	onSettingsClicked: () -> Unit,
	animatedVisibilityScope: AnimatedVisibilityScope,
	sharedTransitionScope: SharedTransitionScope,
	isWideScreen: Boolean,
	snackbar: SnackbarHostState,
	modifier: Modifier = Modifier
) {
	val listState by viewModel.listState.collectAsStateWithLifecycle()
	val context = LocalContext.current
	var deleteConfirmationOpen by remember { mutableStateOf(false) }
	var chooseCategoriesDialogOpen by remember { mutableStateOf(false) }
	var newCategoryDialogOpen by remember { mutableStateOf(false) }

	// Set screen orientation back to user-specified.
	val activity = LocalActivity.current
	activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER

	// Exit selection mode on back pressed
	BackHandler(listState.isSelectMode || listState.selectedCategory != null) {
		when {
			listState.isSelectMode -> {
				viewModel.exitSelectMode()
			}
			listState.selectedCategory != null -> {
				viewModel.selectCategory(null)
			}
		}

	}

	if (deleteConfirmationOpen) {
		AlertDialog(
			onDismissRequest = { deleteConfirmationOpen = false },
			text = {
				Text(
					text = pluralStringResource(R.plurals.delete_confirmation, listState.selectedList.size, listState.selectedList.size)
				)
		    },
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
						viewModel.deleteSelected(context)
						deleteConfirmationOpen = false
					},
					shape = MaterialTheme.shapes.medium
				) {
					Text(stringResource(R.string.delete_button))
				}
			},
			shape = MaterialTheme.shapes.medium
		)
	}

	if (chooseCategoriesDialogOpen) {
		Dialog(
			onDismissRequest = { chooseCategoriesDialogOpen = false }
		) {
			var categories by remember { mutableStateOf( listState.categories.associateWith { category ->
				listState.selectedCards.first().categories.contains(category.id)
			})}
			Column(
				modifier = Modifier
					.clip(MaterialTheme.shapes.medium)
					.background(MaterialTheme.colorScheme.background)
					.padding(16.dp)
			) {
				Text(
					text = stringResource(R.string.choose_categories),
					fontSize = 18.sp,
					modifier = Modifier
						.padding(bottom = 16.dp)
				)
				categories.forEach { (category, checked) ->
					Row(
						verticalAlignment = Alignment.CenterVertically,
						modifier = Modifier
							.fillMaxWidth()
							.clickable {
								categories = categories.toMutableMap().apply {
									replace(category, !checked)
								}.toMap()
							}
					) {
						Checkbox(
							checked = checked,
							onCheckedChange = {
								categories = categories.toMutableMap().apply {
									replace(category, it)
								}.toMap()
							}
						)
						Text(
							text = category.name
						)
					}
				}
				TextButton(
					onClick = {
						newCategoryDialogOpen = true
						chooseCategoriesDialogOpen = false
					}
				) {
					Icon(
						imageVector = Icons.Default.Add,
						contentDescription = null
					)
					Text(
						text = stringResource(R.string.new_category)
					)
				}
				Button(
					shape = MaterialTheme.shapes.medium,
					onClick = {
						categories
							.filter { it.value }
							.map { it.key.id }
							.let {
								viewModel.updateCategories(it)
							}
						chooseCategoriesDialogOpen = false
					},
					modifier = Modifier
						.align(Alignment.End)
						.padding(top = 16.dp)
				) {
					Text(
						text = stringResource(R.string.save)
					)
				}
			}
		}
	}
	val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
	val scope = rememberCoroutineScope()

	ModalNavigationDrawer(
		drawerState = drawerState,
		drawerContent = {
			CategoriesDrawer(
				listState = listState,
				onCategoryClick = {
					viewModel.selectCategory(it)
					scope.launch {
						drawerState.close()
					}
				},
				onSettingsClick = {
					onSettingsClicked()
					scope.launch {
						drawerState.close()
					}
				},
				onNewCategoryClick = {
					viewModel.createNewCategory()
				},
				onNewCategoryNameChange = viewModel::enterNewCategoryName,
				onDeleteCategory = viewModel::deleteCategory,
				onRenameCategory = viewModel::renameCategory,
				onCategoriesReorder = {
					this.launch {
						viewModel.saveCategories(it)
					}
				},
				newCategoryDialogOpen = newCategoryDialogOpen,
				onDismissNewCategory = {
					newCategoryDialogOpen = false
				},
				onAddCategoryClick = {
					newCategoryDialogOpen = true
				}
			)
		}
	) {

	Row {
		if (isWideScreen) {
			NavigationRail(
				containerColor = MaterialTheme.colorScheme.primaryContainer,
				contentColor = MaterialTheme.colorScheme.onPrimaryContainer
			) {
				NavigationRailItem(
					selected = false,
					onClick = {
						scope.launch {
							drawerState.open()
						}
					},
					icon = {
						Icon(
							imageVector = Icons.Default.Menu,
							contentDescription = null
						)
					},
					label = {
						Text(
							text = stringResource(R.string.categories),
							modifier = Modifier
								.padding(horizontal = 8.dp)
						)
					},
					modifier = Modifier
						.displayCutoutPadding()
				)
				NavigationRailItem(
					selected = listState.currentPage == 0,
					onClick = { viewModel.turnPage(0) },
					icon = {
						Icon(
							painterResource(R.drawable.grid),
							contentDescription = null
						)
					},
					label = {
						Text(
							text = stringResource(R.string.all_cards),
							modifier = Modifier
								.padding(horizontal = 8.dp)
						)
					},
					colors = NavigationRailItemDefaults.colors(
						indicatorColor = MaterialTheme.colorScheme.onPrimaryContainer,
						selectedIconColor = MaterialTheme.colorScheme.onPrimary
					),
					modifier = Modifier
						.displayCutoutPadding()
				)
				NavigationRailItem(
					selected = listState.currentPage == 1,
					onClick = { viewModel.turnPage(1) },
					icon = {
						Icon(
							imageVector = Icons.Outlined.Star,
							contentDescription = null
						)
					},
					label = {
						Text(
							text = stringResource(R.string.favourites),
							modifier = Modifier
								.padding(horizontal = 8.dp)
						)
					},
					colors = NavigationRailItemDefaults.colors(
						indicatorColor = MaterialTheme.colorScheme.onPrimaryContainer,
						selectedIconColor = MaterialTheme.colorScheme.onPrimary
					),
					modifier = Modifier
						.displayCutoutPadding()
				)
				Spacer(modifier = Modifier.weight(1f))
				NavigationRailItem(
					selected = false,
					onClick = { onSettingsClicked() },
					icon = {
						Icon(
							imageVector = Icons.Filled.Settings,
							contentDescription = null
						)
					},
					label = {
						Text(
							text = stringResource(R.string.settings),
							modifier = Modifier
								.padding(horizontal = 8.dp)
						)
					},
					colors = NavigationRailItemDefaults.colors(
						indicatorColor = MaterialTheme.colorScheme.onPrimaryContainer,
						selectedIconColor = MaterialTheme.colorScheme.onPrimary
					),
					modifier = Modifier
						.displayCutoutPadding()
				)
			}
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
						onDrawerClick = {
							scope.launch {
								drawerState.open()
							}
						},
						onCategoryClick = {
							chooseCategoriesDialogOpen = true
						},
						onDuplicateClick = {
							viewModel.duplicateSelected()
							viewModel.exitSelectMode()
						},
						isSelectMode = listState.isSelectMode,
						isEditEnabled = listState.selectedList.size == 1,
						isDeleteEnabled = listState.selectedList.isNotEmpty(),
						currentPage = listState.currentPage,
						isShowingSettingsButton = !isWideScreen,
						titleText = listState.selectedCategory?.name
					)
				},
				snackbarHost = {
					SnackbarHost(snackbar)
				},
				bottomBar = {
					if (!isWideScreen) {
						NavigationBar(
							containerColor = MaterialTheme.colorScheme.primaryContainer,
							contentColor = MaterialTheme.colorScheme.onPrimaryContainer
						) {
							NavigationBarItem(
								selected = listState.currentPage == 0,
								onClick = { viewModel.turnPage(0) },
								icon = {
									Icon(
										painter = painterResource(R.drawable.grid),
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
										imageVector = Icons.Default.Favorite,
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
								.applyIf(
									condition = isWideScreen,
									modifier = {
										navigationBarsPadding()
									}
								)
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

				val pager_state = rememberPagerState(
					initialPage = listState.currentPage
				) { 2 }

				LaunchedEffect(listState.currentPage) {
					pager_state.animateScrollToPage(listState.currentPage)
				}

				LaunchedEffect(pager_state.currentPage) {
					viewModel.turnPage(pager_state.currentPage)
				}

				HorizontalPager(
					state = pager_state,
					modifier = modifier
						.fillMaxSize()
						.padding(innerPadding),
				) { page ->
					AnimatedContent(targetState = listState.isLoading) {loading ->
						when {
							loading -> {
								CircularProgressIndicator(
									modifier = Modifier
										.fillMaxSize()
										.wrapContentSize()
								)
							}
							else -> {
								val lazyGridState = rememberLazyGridState()
								GridOfCards(
									items = if (page == 0) {
										if (listState.selectedCategory == null) {
											listState.list
										} else {
											listState.list.filter { it.categories.contains(listState.selectedCategory!!.id) }
										}
									} else {
										listState.favouritesList
									},
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
									},
									lazyGridState = lazyGridState
								)
								if (page == 0 && listState.list.isEmpty()) {
									PromptText(
										text = stringResource(R.string.empty_list_prompt)
									)
								} else if (listState.selectedCategory != null
									&& listState.list.count { it.categories.contains(listState.selectedCategory!!.id) } == 0
									) {
									PromptText(
										text = stringResource(R.string.no_cards_in_category, listState.selectedCategory!!.name)
									)
								}
								if (page == 1 && listState.favouritesList.isEmpty()) {
									PromptText(
										text = stringResource(R.string.add_to_fav_prompt)
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

@Composable
private fun PromptText(
	text: String
) {
	Box(
		contentAlignment = Alignment.Center,
		modifier = Modifier
			.fillMaxSize()
	) {
		Text(
			text = text,
			style = MaterialTheme.typography.bodyLarge,
			textAlign = TextAlign.Center,
			color = Color.Gray
		)
	}
}

