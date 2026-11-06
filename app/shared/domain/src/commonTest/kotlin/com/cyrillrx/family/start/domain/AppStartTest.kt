package com.cyrillrx.family.start.domain

import com.cyrillrx.family.group.domain.Onboarding
import com.cyrillrx.family.group.domain.OnboardingStep
import com.cyrillrx.family.group.domain.RamGroupRepository
import com.cyrillrx.family.group.domain.RamUserRepository
import com.cyrillrx.family.group.domain.SampleInvitationRepository
import com.cyrillrx.family.group.model.Group
import com.cyrillrx.family.group.model.GroupId
import com.cyrillrx.family.group.model.User
import com.cyrillrx.family.group.model.UserId
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class AppStartTest {

    private val users = RamUserRepository()
    private val groups = RamGroupRepository()

    @Test
    fun `opens home for a member of a group`() = runTest {
        users.register(User(id = UserId("alice"), displayName = "Alice"))
        groups.setGroup(Group(id = GroupId("group-1"), name = "Family", createdAt = Instant.fromEpochMilliseconds(0)))

        assertEquals(StartDestination.Home, appStart().destination())
    }

    @Test
    fun `opens the onboarding on the display name when nobody is registered`() = runTest {
        assertEquals(StartDestination.Onboarding(OnboardingStep.DISPLAY_NAME), appStart().destination())
    }

    @Test
    fun `opens the onboarding on the group choice for a member without a group`() = runTest {
        users.register(User(id = UserId("alice"), displayName = "Alice"))

        assertEquals(StartDestination.Onboarding(OnboardingStep.GROUP_CHOICE), appStart().destination())
    }

    private fun appStart() = AppStart(groups, Onboarding(users, groups, SampleInvitationRepository()))
}
