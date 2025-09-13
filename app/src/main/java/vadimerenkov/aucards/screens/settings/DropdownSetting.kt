package vadimerenkov.aucards.screens.settings

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DropdownSetting(
	options: List<Int>,
	description: String,
	chosenOption: String,
	onOptionChosen: (Int) -> Unit,
	modifier: Modifier = Modifier,
	@DrawableRes icon: Int? = null
) {
	var expanded by remember { mutableStateOf(false) }

	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = modifier
			.fillMaxWidth()
	) {
		Row(
			modifier = Modifier
				.weight(2f)
		) {
			icon?.let {
				Icon(
					painter = painterResource(it),
					contentDescription = null,
					tint = MaterialTheme.colorScheme.primary
				)
				Spacer(modifier = Modifier.width(8.dp))
			}
			Text(
				text = description,
				style = MaterialTheme.typography.bodyLarge
			)
		}
		ExposedDropdownMenuBox(
			expanded = expanded,
			onExpandedChange = { expanded = it },
			modifier = Modifier
				.weight(3f)
		) {
			TextField(
				value = chosenOption,
				onValueChange = {},
				readOnly = true,
				modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
				trailingIcon = { Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null) }
			)
			ExposedDropdownMenu(
				expanded = expanded,
				onDismissRequest = { expanded = !expanded }
			) {
				options.forEach { option ->
					val text = stringResource(option)

					DropdownMenuItem(
						text = { Text(text) },
						trailingIcon = {
							if (text == chosenOption) {
								Icon(
									imageVector = Icons.Default.Check,
									contentDescription = null
								)
							} else null
						},
						onClick = {
							onOptionChosen(option)
							expanded = false
						}
					)
				}
			}
		}
	}
}