package com.cyrillrx.family.app

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import com.cyrillrx.family.navigation.OnboardingRoute
import com.cyrillrx.family.navigation.registerOnboardingRoutes
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

/** What a route persisted by an older build decodes to once its discriminator stops resolving. */
@Serializable
internal data object UnrecognizedRoute : NavKey

internal val navSerializersModule = SerializersModule {
    polymorphic(NavKey::class) {
        subclass(MainRoute.Home::class, MainRoute.Home.serializer())
        subclass(UnrecognizedRoute::class, UnrecognizedRoute.serializer())
        registerOnboardingRoutes()

        // A route takes its fully qualified name as polymorphic discriminator, so moving one to
        // another package makes back stacks persisted by an older build undecodable.
        defaultDeserializer { UnrecognizedRoute.serializer() }
    }
}

internal val navSavedStateConfig = SavedStateConfiguration {
    serializersModule = navSerializersModule
}

internal fun MutableList<NavKey>.resetIfRestoredThroughFallback() {
    if (none { it == UnrecognizedRoute }) return

    clear()
    add(OnboardingRoute.DisplayName)
}
