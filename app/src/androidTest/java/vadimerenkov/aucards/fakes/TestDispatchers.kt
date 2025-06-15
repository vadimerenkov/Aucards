package vadimerenkov.aucards.fakes

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import vadimerenkov.aucards.DispatchersProvider

class TestDispatchers : DispatchersProvider {
	@OptIn(ExperimentalCoroutinesApi::class)
	val testDispatcher = UnconfinedTestDispatcher()
	override val main: CoroutineDispatcher
		get() = testDispatcher
	override val io: CoroutineDispatcher
		get() = testDispatcher
	override val default: CoroutineDispatcher
		get() = testDispatcher
}