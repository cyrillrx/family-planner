package com.cyrillrx.family.app

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

interface MainRoute {
    @Serializable
    data object Home : NavKey
}
