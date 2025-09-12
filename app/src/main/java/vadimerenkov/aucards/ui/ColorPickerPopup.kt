package vadimerenkov.aucards.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import vadimerenkov.aucards.R

@Composable
fun ColorPickerPopup(
	selectedColor: Color,
	onColorSelected: (Color) -> Unit,
	selectedHexCode: String,
	onHexCodeChanged: (String) -> Unit,
	isHexCodeValid: Boolean,
	modifier: Modifier = Modifier,
	contentColor: Color = MaterialTheme.colorScheme.primary
) {
	val colorController = rememberColorPickerController()

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = modifier

	) {
		var selectedTab by remember { mutableIntStateOf(0) }
		TabRow(
			selectedTabIndex = selectedTab,
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = 3.dp)
				.padding(horizontal = 2.dp)
				.clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
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

		AnimatedContent(targetState = selectedTab) {tab ->
			when (tab) {
				0 -> {
					FlowRow(
						modifier = Modifier
							.padding(8.dp)
							.wrapContentSize()
							.align(Alignment.CenterHorizontally)
					) {
						Palette.colors.forEach { color ->
							Box(
								modifier = Modifier
									.padding(4.dp)
									.clip(MaterialTheme.shapes.medium)
									.clickable(
										onClick = { onColorSelected(color) }
									)
									.background(color)
									.minimumInteractiveComponentSize()

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
				1 -> {
					Row(
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.spacedBy(8.dp),
						modifier = Modifier
							.padding(16.dp)
					) {
						HsvColorPicker(
							controller = colorController,
							onColorChanged = {
								onColorSelected(it.color)
								onHexCodeChanged(it.hexCode)
							},
							initialColor = selectedColor,
							modifier = Modifier
								.size(200.dp)
						)
						Column(
							verticalArrangement = Arrangement.spacedBy(8.dp),
							horizontalAlignment = Alignment.CenterHorizontally,
							modifier = Modifier
								.background(MaterialTheme.colorScheme.background)
								.padding(8.dp)
						) {
							BrightnessSlider(
								controller = colorController,
								initialColor = selectedColor,
								wheelRadius = 8.dp,
								wheelColor = contentColor,
								modifier = Modifier
									.fillMaxWidth()
									.height(24.dp)
							)
							OutlinedTextField(
								value = selectedHexCode.uppercase(),
								isError = !isHexCodeValid,
								singleLine = true,
								textStyle = LocalTextStyle.current.copy(
									textAlign = TextAlign.Center,
									color = Color.White
								),
								onValueChange = onHexCodeChanged,
								modifier = Modifier
							)
						}
					}
				}
			}
		}
	}
}

@Preview
@Composable
private fun ColorPickerPreview() {
	Box(
		modifier = Modifier
			.background(Color.Black)
			.padding(16.dp)
	) {
		ColorPickerPopup(
			selectedColor = Color.Red,
			onColorSelected = {},
			selectedHexCode = "",
			onHexCodeChanged = {},
			isHexCodeValid = true,
		)
	}
}