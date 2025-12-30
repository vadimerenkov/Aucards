package vadimerenkov.aucards

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import androidx.window.core.layout.WindowWidthSizeClass
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import vadimerenkov.aucards.screens.fullscreencard.CardFullscreen
import vadimerenkov.aucards.screens.fullscreencard.EditScreen
import vadimerenkov.aucards.screens.list.ListScreen
import vadimerenkov.aucards.screens.settings.SettingsScreen
import vadimerenkov.aucards.ui.ObserveAsEvents
import vadimerenkov.aucards.ui.SnackbarController

@Serializable
data class ListScreen(
	val page: Int
)

@Serializable
data object SettingsScreen

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
fun MainNavHost(
	isDarkTheme: Boolean,
	navController: NavHostController = rememberNavController(),
) {
	SharedTransitionLayout {
		val isWideScreen = when (currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass) {
			WindowWidthSizeClass.COMPACT -> false
			else -> true
		}
		val snackbarHost = remember { SnackbarHostState() }
		val scope = rememberCoroutineScope()
		ObserveAsEvents(SnackbarController.events) { event ->
			scope.launch {
				snackbarHost.showSnackbar(event)
			}
		}
		NavHost(
			navController = navController,
			startDestination = ListScreen(0)
		) {
			composable<ListScreen> { entry ->
				val route = entry.toRoute<ListScreen>()
				ListScreen(
					viewModel = koinViewModel { parametersOf(route.page) },
					onCardClicked = { navController.navigate(FullscreenCard(it)) },
					onAddButtonClicked = { navController.navigate(EditScreen(0, index = it)) },
					onCardEditClicked = { navController.navigate(EditScreen(it)) },
					onSettingsClicked = { navController.navigate(SettingsScreen) },
					animatedVisibilityScope = this,
					sharedTransitionScope = this@SharedTransitionLayout,
					isWideScreen = isWideScreen,
					snackbar = snackbarHost
				)
			}
			composable<FullscreenCard>(
				deepLinks = listOf(
					navDeepLink<FullscreenCard>(
						basePath = "vadimerenkov://aucards"
					)
				)
			) {
				val route = it.toRoute<FullscreenCard>()
				CardFullscreen(
					viewModel = koinViewModel { parametersOf(
						route.id, isDarkTheme
					) },
					onBackClicked = { navController.navigateUp() },
					scope = this,
				)
			}
			composable<EditScreen> {
				val route = it.toRoute<EditScreen>()
				EditScreen(
					viewModel = koinViewModel {
						parametersOf(isDarkTheme, route.id, route.index)
					},
					onBackClicked = { navController.navigateUp() },
					scope = this,
					isWideScreen = isWideScreen
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
					onBackClicked = { navController.navigate(ListScreen(it)) },
					isWideScreen = isWideScreen,
					snackbar = snackbarHost
				)
			}
		}
	}
}