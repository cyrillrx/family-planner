package com.cyrillrx.family.navigation

import androidx.navigation.NavHostController

interface OnboardingRouter {
    fun openGroupChoice()

    fun openJoinGroup()

    fun openHome()

    fun navigateUp()
}

class NavOnboardingRouter(private val navController: NavHostController) : OnboardingRouter {

    override fun openGroupChoice() {
        navController.navigate(OnboardingStep.GroupChoice.route) {
            popUpTo(OnboardingStep.DisplayName.route) { inclusive = true }
        }
    }

    override fun openJoinGroup() {
        navController.navigate(OnboardingStep.JoinGroup.route)
    }

    override fun openHome() {
        navController.navigate(OnboardingStep.Home.route) {
            popUpTo(OnboardingStep.GroupChoice.route) { inclusive = true }
        }
    }

    override fun navigateUp() {
        navController.navigateUp()
    }
}
