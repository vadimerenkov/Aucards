package vadimerenkov.aucards.ui

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

object SnackbarController {

	private val channel = Channel<String>()
	val events = channel.receiveAsFlow()

	suspend fun send(message: String) {
		channel.send(message)
	}
}