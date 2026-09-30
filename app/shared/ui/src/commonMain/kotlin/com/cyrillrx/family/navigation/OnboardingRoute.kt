package com.cyrillrx.family.navigation

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.cyrillrx.family.app.AppGraph
import com.cyrillrx.family.presentation.onboarding.displayname.DisplayNameScreen
import com.cyrillrx.family.presentation.onboarding.displayname.DisplayNameViewModel
import com.cyrillrx.family.presentation.onboarding.groupchoice.GroupChoiceScreen
import com.cyrillrx.family.presentation.onboarding.groupchoice.GroupChoiceViewModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.PolymorphicModuleBuilder

sealed interface OnboardingRoute {
    @Serializable
    data object DisplayName : OnboardingRoute, NavKey

    @Serializable
    data object GroupChoice : OnboardingRoute, NavKey
}

fun PolymorphicModuleBuilder<NavKey>.registerOnboardingRoutes() {
    subclass(OnboardingRoute.DisplayName::class, OnboardingRoute.DisplayName.serializer())
    subclass(OnboardingRoute.GroupChoice::class, OnboardingRoute.GroupChoice.serializer())
}

fun EntryProviderScope<NavKey>.handleOnboardingRoutes(
    backStack: NavBackStack<NavKey>,
    graph: AppGraph,
) {
    val router = OnboardingRouterImpl(backStack)

    entry<OnboardingRoute.DisplayName> {
        DisplayNameScreen(viewModel { DisplayNameViewModel(graph.onboarding) }, router)
    }

    entry<OnboardingRoute.GroupChoice> {
        GroupChoiceScreen(viewModel { GroupChoiceViewModel(graph.onboarding) }, router)
    }
}
