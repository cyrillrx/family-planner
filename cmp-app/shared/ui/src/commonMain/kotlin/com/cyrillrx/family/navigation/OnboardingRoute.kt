package com.cyrillrx.family.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.PolymorphicModuleBuilder

sealed interface OnboardingRoute {
    @Serializable
    data object DisplayName : NavKey

    @Serializable
    data object GroupChoice : NavKey

    @Serializable
    data object JoinGroup : NavKey
}

fun PolymorphicModuleBuilder<NavKey>.registerOnboardingRoutes() {
    subclass(OnboardingRoute.DisplayName::class, OnboardingRoute.DisplayName.serializer())
    subclass(OnboardingRoute.GroupChoice::class, OnboardingRoute.GroupChoice.serializer())
    subclass(OnboardingRoute.JoinGroup::class, OnboardingRoute.JoinGroup.serializer())
}
