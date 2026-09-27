package com.cyrillrx.family.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.savedstate.serialization.SavedStateConfiguration
import com.cyrillrx.family.navigation.OnboardingRoute
import com.cyrillrx.family.navigation.registerOnboardingRoutes
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

internal val navSerializersModule = SerializersModule {
    polymorphic(NavKey::class) {
        subclass(MainRoute.Home::class, MainRoute.Home.serializer())
        registerOnboardingRoutes()

        // A route takes its fully qualified name as polymorphic discriminator, so moving one to
        // another package makes back stacks persisted by an older build undecodable. Falling back
        // resets navigation instead of crashing at launch.
        defaultDeserializer { OnboardingRoute.DisplayName.serializer() }
    }
}

internal val navSavedStateConfig = SavedStateConfiguration {
    serializersModule = navSerializersModule
}

@Composable
internal fun rememberAppBackStack(): NavBackStack<NavKey> {
    val backStack = rememberNavBackStack(navSavedStateConfig, OnboardingRoute.DisplayName)

    // During composition rather than in an effect: NavDisplay composes the entries in this same
    // pass, and a repeated key crashes it before any effect would get the chance to run.
    remember(backStack) { backStack.resetIfRestoredThroughFallback() }

    return backStack
}

/** Only the fallback above puts the first step past the first entry, and it fills every slot with it. */
internal fun MutableList<NavKey>.resetIfRestoredThroughFallback() {
    if (drop(1).none { it == OnboardingRoute.DisplayName }) return

    clear()
    add(OnboardingRoute.DisplayName)
}
