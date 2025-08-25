package vadimerenkov.aucards

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.window.core.layout.WindowWidthSizeClass
import kotlinx.serialization.Serializable
import vadimerenkov.aucards.screens.CardFullscreen
import vadimerenkov.aucards.screens.EditScreen
import vadimerenkov.aucards.screens.ListScreen
import vadimerenkov.aucards.screens.SettingsScreen

@Serializable
object ListScreen

@Serializable
object SettingsScreen

@Serializable
data class FullscreenCard(
	val id: Int
)

@Serializable
data class EditScreen(
	val id: Int,
	val index: Int? = null
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun AucardsApp(
	isDarkTheme: Boolean,
	navController: NavHostController = rememberNavController(),
) {
	SharedTransitionLayout {
		val isWideScreen = when (currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass) {
			WindowWidthSizeClass.COMPACT -> false
			else -> true
		}
		NavHost(
			navController = navController,
			startDestination = ListScreen
		) {
			composable<ListScreen> {
				ListScreen(
					onCardClicked = { navController.navigate(FullscreenCard(it)) },
					onAddButtonClicked = { navController.navigate(EditScreen(0, index = it)) },
					onCardEditClicked = { navController.navigate(EditScreen(it)) },
					onSettingsClicked = { navController.navigate(SettingsScreen) },
					animatedVisibilityScope = this,
					sharedTransitionScope = this@SharedTransitionLayout,
					isWideScreen = isWideScreen
				)
			}
			composable<FullscreenCard> {
				val route = it.toRoute<FullscreenCard>()
				CardFullscreen(
					onBackClicked = { navController.navigateUp() },
					scope = this,
					id = route.id
				)
			}
			composable<EditScreen> {
				val route = it.toRoute<EditScreen>()
				EditScreen(
					isDarkTheme = isDarkTheme,
					onBackClicked = { navController.navigateUp() },
					scope = this,
					id = route.id,
					index = route.index
				)
			}
			composable<SettingsScreen>(
				enterTransition = {
					slideInHorizontally(
						initialOffsetX = { it ->
							-it
						}
					)
				},
				exitTransition = {
					slideOutHorizontally(
						targetOffsetX = { it ->
							-it
						}
					)
				}
			) {
				SettingsScreen(
					onBackClicked = { navController.navigateUp() },
					isWideScreen = isWideScreen
				)
			}
		}
	}
}