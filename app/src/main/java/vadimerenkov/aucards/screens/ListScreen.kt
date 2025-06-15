@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class,
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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import vadimerenkov.aucards.R
import vadimerenkov.aucards.ViewModelFactory
import vadimerenkov.aucards.data.Aucard
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
				isDeleteEnabled = listState.selectedList.isNotEmpty()
			)
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
			LazyVerticalGrid(
				columns = GridCells.Adaptive(150.dp),
				horizontalArrangement = Arrangement.SpaceBetween,
				modifier = Modifier
					.align(Alignment.TopCenter)
					.padding(6.dp)
			) {
				items(
					items = listState.list,
					key = { item ->
						item.id
					}
				) { card ->
					val isSelected = listState.selectedList.contains(card.id)
					AucardItem(
						aucard = card,
						onClick = { id ->
							if (listState.isSelectMode) {
								if (isSelected) {
									viewModel.DeselectId(id)
								}
								else {
									viewModel.SelectId(id)
								}
							}
							else {
								onCardClicked(id)
							}
						},
						onLongPress = {
							viewModel.EnterSelectMode(card.id)
						},
						isSelectMode = listState.isSelectMode,
						isSelected = isSelected,
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
				AnimatedVisibility(
					visible = listState.list.isEmpty() && !listState.isLoading,
					modifier = Modifier
						.align(Alignment.Center)
				) {
					Text(
						text = stringResource(R.string.empty_list_prompt),
						style = MaterialTheme.typography.titleLarge,
						textAlign = TextAlign.Center,
						color = Color.Gray
					)
				}
				AnimatedVisibility(
					visible = listState.isLoading,
					modifier = Modifier
						.align(Alignment.Center)
				) {
					CircularProgressIndicator()
				}
			}
		}
	}



@Composable
fun AucardItem(
	aucard: Aucard,
	onClick: (Int) -> Unit,
	onLongPress: () -> Unit,
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
	val border by animateDpAsState(
		targetValue = if (isSelected) 3.dp else 0.dp
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
			Column(
				verticalArrangement = Arrangement.Center,
				horizontalAlignment = Alignment.CenterHorizontally,
				modifier = Modifier
					.fillMaxSize()
			) {
				Text(
					text = aucard.text,
					style = MaterialTheme.typography.titleMedium,
					color = Color.Black,
					textAlign = TextAlign.Center
				)
			}
		}
	}
}


@Composable
fun AucardsTopBar(
	selectedNumber: Int,
	onDeleteClick: () -> Unit,
	onEditClick: () -> Unit,
	onSettingsClick: () -> Unit,
	isEditEnabled: Boolean = true,
	isSelectMode: Boolean = false,
	isDeleteEnabled: Boolean = true
) {
	val delete_color by animateColorAsState(
		if (isDeleteEnabled) MaterialTheme.colorScheme.onPrimaryContainer else Color.Gray
	)
	val edit_color by animateColorAsState(
		if (isEditEnabled) MaterialTheme.colorScheme.onPrimaryContainer else Color.Gray
	)
	TopAppBar(
		title = {
			AnimatedContent(
				targetState = isSelectMode,
				transitionSpec = {
					slideInVertically(
						initialOffsetY = { it ->
							if (isSelectMode) it else -it
						}
					).togetherWith(
						slideOutVertically(
							targetOffsetY = { it ->
								if (isSelectMode) -it else it
							}
						)
					)
				}
			) { isSelectMode ->
				if (isSelectMode) {
					Text(stringResource(R.string.selected_number, selectedNumber))
				}
				else {
					Text(stringResource(R.string.app_name))
				}
			}
		},
		colors = TopAppBarDefaults.topAppBarColors(
			containerColor = MaterialTheme.colorScheme.primaryContainer
		),
		navigationIcon = {
			IconButton(
				onClick = onSettingsClick
			) {
				Icon(
					imageVector = Icons.Default.Settings,
					contentDescription = stringResource(R.string.open_settings),
					tint = MaterialTheme.colorScheme.onPrimaryContainer
				)
			}
		},
		actions = {
			AnimatedVisibility(
				visible = isSelectMode,
				enter = fadeIn() + slideIn { siz ->
					IntOffset(0, siz.height*4)
			    },
				exit = fadeOut() + slideOut { siz ->
					IntOffset(0, siz.height*4)
				}
			) {
				Row {
					IconButton(
						enabled = isDeleteEnabled,
						onClick = { onDeleteClick() },
						colors = IconButtonDefaults.iconButtonColors(
							contentColor = delete_color
						)
					) {
						Icon(
							imageVector = Icons.Default.Delete,
							contentDescription = stringResource(R.string.delete)
						)
					}
					IconButton(
						enabled = isEditEnabled,
						onClick = { onEditClick() },
						colors = IconButtonDefaults.iconButtonColors(
							contentColor = edit_color
						)
					) {
						Icon(
							imageVector = Icons.Default.Edit,
							contentDescription = stringResource(R.string.edit)
						)
					}
				}
			}
		}
	)
}