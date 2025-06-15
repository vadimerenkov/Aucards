package vadimerenkov.aucards

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import vadimerenkov.aucards.screens.CardFullscreen
import vadimerenkov.aucards.screens.ListScreen
import vadimerenkov.aucards.screens.SettingsScreen

@Serializable
object ListScreen

@Serializable
object SettingsScreen

@Serializable
data class FullscreenCard(
	val id: Int,
	val isEditable: Boolean
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun AucardsApp(
	navController: NavHostController = rememberNavController(),
) {
	SharedTransitionLayout {
		NavHost(
			navController = navController,
			startDestination = ListScreen
		) {
			composable<ListScreen> {
				ListScreen(
					onCardClicked = { navController.navigate(FullscreenCard(it, false)) },
					onAddButtonClicked = { navController.navigate(FullscreenCard(0, true)) },
					onCardEditClicked = { navController.navigate(FullscreenCard(it, true)) },
					onSettingsClicked = { navController.navigate(SettingsScreen) },
					scope = this
				)
			}
			composable<FullscreenCard> { it ->
				val card = it.toRoute<FullscreenCard>()
				CardFullscreen(
					onBackClicked = { navController.navigateUp() },
					modifier = Modifier
						.sharedBounds(
							sharedContentState = rememberSharedContentState(card.id),
							animatedVisibilityScope = this,
							enter = scaleIn(),
							exit = scaleOut()
						)
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
					onBackClicked = { navController.navigateUp() }
				)
			}
		}
	}
}