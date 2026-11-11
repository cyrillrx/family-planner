package com.cyrillrx.family.app

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface MainRoute {
    @Serializable
    data object Home : MainRoute, NavKey

    @Serializable
    data object GroupNotFound : MainRoute, NavKey
}
