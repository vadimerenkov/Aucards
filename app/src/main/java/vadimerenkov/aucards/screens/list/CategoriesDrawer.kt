package vadimerenkov.aucards.screens.list

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.CoroutineScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import vadimerenkov.aucards.R
import vadimerenkov.aucards.data.CardCategory

@Composable
fun CategoriesDrawer(
	listState: ListState,
	onCategoryClick: (CardCategory?) -> Unit,
	onSettingsClick: () -> Unit,
	onCategoriesReorder: CoroutineScope.(List<CardCategory>) -> Unit,
	onNewCategoryNameChange: (String) -> Unit,
	onNewCategoryClick: () -> Unit,
	onDeleteCategory: (CardCategory) -> Unit,
	onRenameCategory: (CardCategory, String) -> Unit
) {
	var newCategoryDialogOpen by remember { mutableStateOf(false) }
	var deleteConfirmationOpen by remember { mutableStateOf(false) }
	var renameDialogOpen by remember { mutableStateOf(false) }

	var selectedCategory: CardCategory? by remember { mutableStateOf(null) }
	var textFieldName by remember { mutableStateOf("") }

	if (newCategoryDialogOpen) {
		Dialog(
			onDismissRequest = { newCategoryDialogOpen = false }
		) {
			Column(
				verticalArrangement = Arrangement.spacedBy(16.dp),
				modifier = Modifier
					.clip(MaterialTheme.shapes.medium)
					.background(MaterialTheme.colorScheme.background)
					.padding(16.dp)
			) {
				Text(
					text = stringResource(R.string.new_category),
					fontSize = 18.sp
				)
				TextField(
					value = listState.newCategoryName,
					onValueChange = onNewCategoryNameChange,
					placeholder = {
						Text(
							text = stringResource(R.string.category_name)
						)
					}
				)
				Button(
					shape = MaterialTheme.shapes.medium,
					onClick = {
						onNewCategoryClick()
						newCategoryDialogOpen = false
					},
					modifier = Modifier
						.align(Alignment.End)
				) {
					Text(
						text = stringResource(R.string.save)
					)
				}
			}
		}
	}

	if (deleteConfirmationOpen) {
		Dialog(
			onDismissRequest = { deleteConfirmationOpen = false }
		) {
			Column(
				verticalArrangement = Arrangement.spacedBy(16.dp),
				modifier = Modifier
					.clip(MaterialTheme.shapes.medium)
					.background(MaterialTheme.colorScheme.background)
					.padding(16.dp)
			) {
				Text(
					text = stringResource(R.string.delete_category_confirmation, selectedCategory?.name ?: "NULL")
				)
				Row(
					horizontalArrangement = Arrangement.SpaceBetween,
					modifier = Modifier
						.fillMaxWidth()
				) {
					TextButton(
						onClick = {
							deleteConfirmationOpen = false
							selectedCategory = null
						}
					) {
						Text(
							text = stringResource(R.string.cancel)
						)
					}
					Button(
						shape = MaterialTheme.shapes.medium,
						onClick = {
							selectedCategory?.let {
								onDeleteCategory(it)
								selectedCategory = null
								deleteConfirmationOpen = false
							}
						}
					) {
						Text(
							text = stringResource(R.string.delete_button)
						)
					}
				}
			}
		}
	}

	if (renameDialogOpen) {
		Dialog(
			onDismissRequest = { renameDialogOpen = false }
		) {
			Column(
				verticalArrangement = Arrangement.spacedBy(16.dp),
				modifier = Modifier
					.clip(MaterialTheme.shapes.medium)
					.background(MaterialTheme.colorScheme.background)
					.padding(16.dp)
			) {
				Text(
					text = stringResource(R.string.rename)
				)
				TextField(
					value = textFieldName,
					onValueChange = {
						textFieldName = it
					}
				)
				Button(
					shape = MaterialTheme.shapes.medium,
					onClick = {
						selectedCategory?.let {
							onRenameCategory(it, textFieldName)
						}
						textFieldName = ""
						selectedCategory = null
						renameDialogOpen = false
					},
					modifier = Modifier
						.align(Alignment.End)
				) {
					Text(
						text = stringResource(R.string.save)
					)
				}
			}
		}
	}

	ModalDrawerSheet() {
		NavigationDrawerItem(
			label = {
				val string = stringResource(R.string.all)
				Text(
					text = "$string (${listState.list.size})",
					fontSize = 16.sp
				)
			},
			onClick = {
				onCategoryClick(null)
			},
			selected = listState.selectedCategory == null
		)
		val lazyListState = rememberLazyListState()
		var categories by remember { mutableStateOf(listState.categories) }
		categories = listState.categories
		val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
			Log.i("Reordering", "Reordering")
			categories = categories.toMutableList().apply {
				add(to.index, removeAt(from.index))
			}
			onCategoriesReorder(categories.map { it.copy(index = categories.indexOf(it)) })
		}

		LazyColumn(
			state = lazyListState
		) {
			itemsIndexed(
				items = categories,
				key = { index, category -> category.id }
			) {index,  category ->
				var menuOpen by remember { mutableStateOf(false) }
				ReorderableItem(
					enabled = true,
					state = reorderableState,
					key = { category.id }
				) {
					NavigationDrawerItem(
						label = {
							Row(
								verticalAlignment = Alignment.CenterVertically,
								horizontalArrangement = Arrangement.SpaceBetween,
								modifier = Modifier
									.fillMaxWidth()
							) {
								Text(
									text = "${category.name} (${listState.list.count { it.categories.contains(category.id) }})",
									fontSize = 16.sp,
									modifier = Modifier
//									.longPressDraggableHandle(
//										onDragStarted = {
//											Log.i("Dragging", "Dragging")
//										}
//									)
								)
								IconButton(
									onClick = {
										menuOpen = true
									}
								) {
									Icon(
										imageVector = Icons.Default.MoreVert,
										contentDescription = null
									)
									DropdownMenu(
										expanded = menuOpen,
										onDismissRequest = {
											menuOpen = false
										}
									) {
										DropdownMenuItem(
											text = {
												Text(
													text = stringResource(R.string.delete_category)
												)
											},
											onClick = {
												selectedCategory = category
												menuOpen = false
												deleteConfirmationOpen = true
											}
										)
										DropdownMenuItem(
											text = {
												Text(
													text = stringResource(R.string.rename)
												)
											},
											onClick = {
												selectedCategory = category
												textFieldName = category.name
												renameDialogOpen = true
												menuOpen = false
											}
										)
									}
								}
							}
						},
						onClick = {
							onCategoryClick(category)
						},
						selected = listState.selectedCategory == category
					)
				}
			}
		}
		NavigationDrawerItem(
			icon = {
				Icon(
					imageVector = Icons.Default.Add,
					contentDescription = null
				)
			},
			label = {
				Text(
					text = stringResource(R.string.new_category),
					fontSize = 16.sp
				)
			},
			onClick = { newCategoryDialogOpen = true },
			selected = false
		)
		Spacer(modifier = Modifier.weight(1f))
		NavigationDrawerItem(
			icon = {
				Icon(
					imageVector = Icons.Default.Settings,
					contentDescription = null
				)
			},
			label = {
				Text(
					text = stringResource(R.string.settings),
					fontSize = 16.sp
				)
			},
			onClick = {
				onSettingsClick()
			},
			selected = false
		)
	}
}