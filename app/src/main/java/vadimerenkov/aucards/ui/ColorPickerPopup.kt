package vadimerenkov.aucards.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import vadimerenkov.aucards.R

@Composable
fun ColorPickerPopup(
	isOpen: Boolean,
	onDismissRequest: () -> Unit,
	selectedColor: Color,
	onColorSelected: (Color) -> Unit,
	selectedHexCode: String,
	onHexCodeChanged: (String) -> Unit,
	isHexCodeValid: Boolean,
	modifier: Modifier = Modifier,
	offset: DpOffset = DpOffset(0.dp, 0.dp),
	tabRowSize: DpSize = DpSize(300.dp, 50.dp),
	contentColor: Color = MaterialTheme.colorScheme.primary
) {
	val colorController = rememberColorPickerController()

	DropdownMenu(
		offset = offset,
		onDismissRequest = onDismissRequest,
		expanded = isOpen,
		modifier = modifier
	) {
		var selectedTab by remember { mutableIntStateOf(0) }
		TabRow(
			selectedTabIndex = selectedTab,
			modifier = Modifier
				.size(tabRowSize)
		) {
			Tab(
				selected = selectedTab == 0,
				onClick = { selectedTab = 0 }
			) {
				Text(
					text = stringResource(R.string.palette),
					textAlign = TextAlign.Center,
					modifier = Modifier
						.padding(8.dp)
				)
			}
			Tab(
				selected = selectedTab == 1,
				onClick = { selectedTab = 1 }
			) {
				Text(
					text = stringResource(R.string.custom_color),
					textAlign = TextAlign.Center,
					modifier = Modifier
						.padding(8.dp)
				)
			}
		}

		if (selectedTab == 0) {
			FlowRow(
				maxItemsInEachRow = 4,
				modifier = Modifier
					.padding(8.dp)
					.align(Alignment.CenterHorizontally)
			) {
				Palette.colors.forEach { color ->
					Box(
						modifier = Modifier
							.padding(4.dp)
							.clip(MaterialTheme.shapes.medium)
							.background(color)
							.minimumInteractiveComponentSize()
							.clickable(
								onClick = { onColorSelected(color) }
							)
					) {
						if (color == selectedColor) {
							Icon(
								imageVector = Icons.Default.Done,
								contentDescription = color.toString(),
								tint = contentColor
							)
						}
					}
				}
			}
		}
		if (selectedTab == 1) {
			HsvColorPicker(
				controller = colorController,
				onColorChanged = {
					onColorSelected(it.color)
					onHexCodeChanged(it.hexCode)
				},
				initialColor = selectedColor,
				modifier = Modifier
					.padding(8.dp)
					.size(200.dp)
					.align(Alignment.CenterHorizontally)
			)
			BrightnessSlider(
				controller = colorController,
				initialColor = selectedColor,
				wheelRadius = 8.dp,
				wheelColor = contentColor,
				modifier = Modifier
					.padding(horizontal = 16.dp)
					.fillMaxWidth()
					.height(24.dp)
			)
			OutlinedTextField(
				value = selectedHexCode.uppercase(),
				isError = !isHexCodeValid,
				singleLine = true,
				textStyle = LocalTextStyle.current.copy(
					textAlign = TextAlign.Center
				),
				onValueChange = onHexCodeChanged,
				modifier = Modifier
					.padding(8.dp)
					.width(120.dp)
					.align(Alignment.CenterHorizontally)
			)
		}
	}
}