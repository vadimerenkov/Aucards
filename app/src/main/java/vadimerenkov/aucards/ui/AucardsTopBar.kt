@file:OptIn(ExperimentalMaterial3Api::class)

package vadimerenkov.aucards.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import vadimerenkov.aucards.R

@Composable
fun AucardsTopBar(
	currentPage: Int,
	selectedNumber: Int,
	onDeleteClick: () -> Unit,
	onEditClick: () -> Unit,
	onSettingsClick: () -> Unit,
	isEditEnabled: Boolean = true,
	isSelectMode: Boolean = false,
	isDeleteEnabled: Boolean = true
) {
	val delete_color by animateColorAsState(
		if (isDeleteEnabled) MaterialTheme.colorScheme.onPrimaryContainer else Color.Companion.Gray
	)
	val edit_color by animateColorAsState(
		if (isEditEnabled) MaterialTheme.colorScheme.onPrimaryContainer else Color.Companion.Gray
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
					Text(
						stringResource(R.string.selected_number, selectedNumber),
						color = MaterialTheme.colorScheme.onPrimaryContainer
					)
				} else {
					when (currentPage) {
						0 -> {
							Text(
								stringResource(R.string.app_name),
								color = MaterialTheme.colorScheme.onPrimaryContainer
							)
						}

						1 -> {
							Text(
								stringResource(R.string.favourites),
								color = MaterialTheme.colorScheme.onPrimaryContainer
							)
						}
					}

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
					IntOffset(0, siz.height * 4)
				},
				exit = fadeOut() + slideOut { siz ->
					IntOffset(0, siz.height * 4)
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