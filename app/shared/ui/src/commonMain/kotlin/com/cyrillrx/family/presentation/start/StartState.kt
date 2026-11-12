package com.cyrillrx.family.presentation.start

import com.cyrillrx.family.group.domain.NoAnswer
import com.cyrillrx.family.start.domain.StartDestination

sealed interface StartState {
    data object Resolving : StartState
    data class Resolved(val destination: StartDestination) : StartState
    data class Failed(val error: NoAnswer) : StartState
}
