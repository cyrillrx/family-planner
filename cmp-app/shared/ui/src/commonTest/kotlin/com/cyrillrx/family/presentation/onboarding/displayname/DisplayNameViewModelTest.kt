package com.cyrillrx.family.presentation.onboarding.displayname

import com.cyrillrx.family.group.domain.Onboarding
import com.cyrillrx.family.group.domain.RamGroupRepository
import com.cyrillrx.family.group.domain.RamUserRepository
import com.cyrillrx.family.group.domain.RegisterError
import com.cyrillrx.family.group.domain.RegisterUserError
import com.cyrillrx.family.group.domain.SampleInvitationRepository
import com.cyrillrx.family.group.domain.UserRepository
import com.cyrillrx.family.presentation.CountingUserRepository
import com.cyrillrx.family.presentation.FailingUserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DisplayNameViewModelTest {

    @BeforeTest
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `opens on an empty name`() = runTest {
        assertEquals(DisplayNameState(), viewModel().state.value)
    }

    @Test
    fun `keeps what is typed`() = runTest {
        val viewModel = viewModel()

        viewModel.changeDisplayName("Alice")

        assertEquals(DisplayNameState(displayName = "Alice"), viewModel.state.value)
    }

    @Test
    fun `clears the refusal as soon as the name changes`() = runTest {
        val viewModel = viewModel()
        viewModel.register()
        advanceUntilIdle()

        viewModel.changeDisplayName("A")

        assertEquals(DisplayNameState(displayName = "A"), viewModel.state.value)
    }

    @Test
    fun `ignores what is typed while it registers`() = runTest {
        val viewModel = viewModel()
        viewModel.changeDisplayName("Alice")
        viewModel.register()

        viewModel.changeDisplayName("Bob")

        assertEquals(
            DisplayNameState(displayName = "Alice", submitting = true),
            viewModel.state.value,
        )
    }

    @Test
    fun `says it is working while it registers`() = runTest {
        val viewModel = viewModel()
        viewModel.changeDisplayName("Alice")

        viewModel.register()

        assertEquals(
            DisplayNameState(displayName = "Alice", submitting = true),
            viewModel.state.value,
        )
    }

    @Test
    fun `announces the registration once`() = runTest {
        val viewModel = viewModel()
        val registrations = collectRegistrations(viewModel)
        viewModel.changeDisplayName("Alice")

        viewModel.register()
        advanceUntilIdle()

        assertEquals(1, registrations.size)
    }

    @Test
    fun `stops working once registered`() = runTest {
        val viewModel = viewModel()
        viewModel.changeDisplayName("Alice")

        viewModel.register()
        advanceUntilIdle()

        assertEquals(DisplayNameState(displayName = "Alice"), viewModel.state.value)
    }

    @Test
    fun `refuses a blank name`() = runTest {
        val viewModel = viewModel()

        viewModel.register()
        advanceUntilIdle()

        assertEquals(
            DisplayNameState(error = RegisterError.BlankDisplayName),
            viewModel.state.value,
        )
    }

    @Test
    fun `carries the refusal the service gave`() = runTest {
        val viewModel = viewModel(userRepository = FailingUserRepository)
        viewModel.changeDisplayName("Alice")

        viewModel.register()
        advanceUntilIdle()

        assertEquals(
            DisplayNameState(
                displayName = "Alice",
                error = RegisterError.Registration(RegisterUserError.Unknown),
            ),
            viewModel.state.value,
        )
    }

    @Test
    fun `registers once however many times the button is pressed`() = runTest {
        val users = CountingUserRepository()
        val viewModel = viewModel(userRepository = users)
        viewModel.changeDisplayName("Alice")

        viewModel.register()
        viewModel.register()
        advanceUntilIdle()

        assertEquals(1, users.registrations)
    }

    private fun TestScope.collectRegistrations(viewModel: DisplayNameViewModel): List<Unit> {
        val registrations = mutableListOf<Unit>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.registered.collect { registrations += it }
        }

        return registrations
    }

    private fun viewModel(userRepository: UserRepository = RamUserRepository()) =
        DisplayNameViewModel(
            Onboarding(userRepository, RamGroupRepository(), SampleInvitationRepository()),
        )
}
