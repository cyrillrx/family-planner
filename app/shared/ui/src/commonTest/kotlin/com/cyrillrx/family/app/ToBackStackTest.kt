package com.cyrillrx.family.app

import androidx.navigation3.runtime.NavKey
import com.cyrillrx.family.group.domain.OnboardingStep
import com.cyrillrx.family.navigation.OnboardingRoute
import com.cyrillrx.family.start.domain.StartDestination
import kotlin.test.Test
import kotlin.test.assertEquals

class ToBackStackTest {

    @Test
    fun `opens the display name for someone not registered yet`() {
        assertEquals(
            listOf<NavKey>(OnboardingRoute.DisplayName),
            StartDestination.Onboarding(OnboardingStep.DISPLAY_NAME).toBackStack(),
        )
    }

    @Test
    fun `keeps the display name under the group choice so the member can go back and rename`() {
        assertEquals(
            listOf<NavKey>(OnboardingRoute.DisplayName, OnboardingRoute.GroupChoice),
            StartDestination.Onboarding(OnboardingStep.GROUP_CHOICE).toBackStack(),
        )
    }

    @Test
    fun `opens home alone for a member of a group`() {
        assertEquals(listOf<NavKey>(MainRoute.Home), StartDestination.Home.toBackStack())
    }

    @Test
    fun `opens the missing group alone for a membership whose group does not exist`() {
        assertEquals(listOf<NavKey>(MainRoute.GroupNotFound), StartDestination.GroupNotFound.toBackStack())
    }
}
