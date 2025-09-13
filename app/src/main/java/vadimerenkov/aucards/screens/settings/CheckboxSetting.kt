package vadimerenkov.aucards.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun CheckboxSetting(
	title: String,
	onCheckedChange: (Boolean) -> Unit,
	modifier: Modifier = Modifier,
	description: String? = null,
	isDescVisible: Boolean = true,
	isChecked: Boolean = false
) {
	Column(
		verticalArrangement = Arrangement.spacedBy(8.dp),
		modifier = modifier
			.fillMaxWidth()
			.clickable(onClick = {
				onCheckedChange(!isChecked)
			})
			.padding(vertical = 8.dp)
	) {
		Row(
			verticalAlignment = Alignment.Top
		) {
			Text(
				text = title,
				style = MaterialTheme.typography.bodyLarge,
				modifier = Modifier
					.weight(1f)
			)
			CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
				Checkbox(
					checked = isChecked,
					onCheckedChange = onCheckedChange,
					modifier = Modifier
						.padding(start = 8.dp)
				)
			}
		}
		description?.let {
			AnimatedVisibility(isDescVisible) {
				Text(
					text = it,
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.primary
				)
			}
		}
	}
}