package vadimerenkov.aucards.screens.fullscreencard.toolbar

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import vadimerenkov.aucards.screens.fullscreencard.CardAction
import vadimerenkov.aucards.ui.theme.AucardsTheme

@Composable
fun ColorPickerPopup(
	onAction: (CardAction) -> Unit,
	selectedColor: Color,
	selectedHexCode: String,
	isHexCodeValid: Boolean,
	isWideScreen: Boolean,
	modifier: Modifier = Modifier,
	contentColor: Color = MaterialTheme.colorScheme.primary
) {
	val colorController = rememberColorPickerController()

	if (isWideScreen ) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = modifier
				.padding(top = 16.dp)
				.height(IntrinsicSize.Max)
		) {
			HsvColorPicker(
				controller = colorController,
				onColorChanged = {
					onAction(CardAction.ColorSelected(it.color))
					onAction(CardAction.HexCodeChanged(it.hexCode))
				},
				initialColor = selectedColor,
				modifier = Modifier
					.fillMaxHeight()
					.aspectRatio(1f)
			)
			Spacer(modifier = Modifier.width(16.dp))
			Column(
				verticalArrangement = Arrangement.spacedBy(8.dp),
				horizontalAlignment = Alignment.CenterHorizontally,
				modifier = Modifier
					.fillMaxHeight()
					.width(IntrinsicSize.Max)
			) {
				PaletteTab(
					selectedColor = selectedColor,
					contentColor = contentColor,
					onAction = {
						if (it is CardAction.ColorSelected) {
							colorController.selectByColor(it.color, true)
							onAction(it)
						}
					},
					itemsNumber = 9
				)
				BrightnessSlider(
					controller = colorController,
					initialColor = selectedColor,
					wheelRadius = 8.dp,
					wheelColor = contentColor,
					modifier = Modifier
						.height(24.dp)
				)
				OutlinedTextField(
					value = selectedHexCode.uppercase(),
					isError = !isHexCodeValid,
					singleLine = true,
					textStyle = LocalTextStyle.current.copy(
						textAlign = TextAlign.Center,
						color = MaterialTheme.colorScheme.primary
					),
					onValueChange = {
						onAction(CardAction.HexCodeChanged(it))
					},
					modifier = Modifier
						.fillMaxWidth()
				)
			}
		}
	} else {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = modifier

		) {
			var selectedTab by remember { mutableIntStateOf(0) }
			TabRow(
				selectedTabIndex = selectedTab,
				modifier = Modifier
					.fillMaxWidth()
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

			AnimatedContent(targetState = selectedTab) { tab ->
				when (tab) {
					0 -> {
						PaletteTab(
							selectedColor = selectedColor,
							contentColor = contentColor,
							onAction = onAction,
							modifier = Modifier
								.padding(top = 16.dp)
						)
					}

					1 -> {
						Row(
							verticalAlignment = Alignment.CenterVertically,
							horizontalArrangement = Arrangement.spacedBy(8.dp),
							modifier = modifier
								.padding(16.dp)
						) {
							HsvColorPicker(
								controller = colorController,
								onColorChanged = {
									onAction(CardAction.ColorSelected(it.color))
									onAction(CardAction.HexCodeChanged(it.hexCode))
								},
								initialColor = selectedColor,
								modifier = Modifier
									.size(200.dp)
							)
							Column(
								verticalArrangement = Arrangement.SpaceBetween,
								horizontalAlignment = Alignment.CenterHorizontally,
								modifier = Modifier
									.padding(8.dp)
									.width(IntrinsicSize.Max)
							) {
								BrightnessSlider(
									controller = colorController,
									initialColor = selectedColor,
									wheelRadius = 8.dp,
									wheelColor = contentColor,
									modifier = Modifier
										.height(24.dp)
								)
								OutlinedTextField(
									value = selectedHexCode.uppercase(),
									isError = !isHexCodeValid,
									singleLine = true,
									textStyle = LocalTextStyle.current.copy(
										textAlign = TextAlign.Center,
										color = MaterialTheme.colorScheme.primary
									),
									onValueChange = {
										onAction(CardAction.HexCodeChanged(it))
									},
									modifier = Modifier
								)
							}
						}
					}
				}
			}
		}
	}
}

@Composable
private fun ColumnScope.PaletteTab(
	selectedColor: Color,
	contentColor: Color,
	onAction: (CardAction) -> Unit,
	modifier: Modifier = Modifier,
	itemsNumber: Int = 6
) {
	FlowRow(
		horizontalArrangement = Arrangement.spacedBy(8.dp),
		verticalArrangement = Arrangement.spacedBy(8.dp),
		maxItemsInEachRow = itemsNumber,
		modifier = modifier
			.align(Alignment.CenterHorizontally)
	) {
		Palette.colors.forEach { color ->
			Box(
				modifier = Modifier
					//.padding(4.dp)
					.clip(MaterialTheme.shapes.medium)
					.clickable(
						onClick = { onAction(CardAction.ColorSelected(color)) }
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

@Preview(device = "spec:width=1280dp,height=800dp,dpi=240")
@Composable
private fun ColorPickerPreview() {
	AucardsTheme {
		Box(
			modifier = Modifier
				.background(MaterialTheme.colorScheme.primaryContainer)
				.padding(16.dp)
		) {
			ColorPickerPopup(
				onAction = {},
				selectedColor = Color.Red,
				selectedHexCode = "#FFFFFFF",
				isHexCodeValid = true,
				isWideScreen = true
			)
		}
	}
}