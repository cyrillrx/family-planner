package com.cyrillrx.family.app

import androidx.navigation3.runtime.NavKey
import com.cyrillrx.family.navigation.OnboardingRoute
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CanResumeWithoutStartTest {

    @Test
    fun `resumes a restored back stack whose routes are all recognized`() {
        assertTrue(listOf<NavKey>(OnboardingRoute.DisplayName, OnboardingRoute.GroupChoice).canResumeWithoutStart())
    }

    @Test
    fun `cannot resume without a restored back stack`() {
        assertFalse(emptyList<NavKey>().canResumeWithoutStart())
    }

    @Test
    fun `cannot resume a restored back stack holding an unrecognized route`() {
        assertFalse(listOf(OnboardingRoute.DisplayName, UnrecognizedRoute).canResumeWithoutStart())
    }
}
