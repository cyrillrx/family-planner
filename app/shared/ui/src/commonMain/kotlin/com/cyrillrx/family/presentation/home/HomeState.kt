package com.cyrillrx.family.presentation.home

import com.cyrillrx.family.group.domain.NoAnswer

data class HomeState(val body: Body = Body.Loading) {

    sealed interface Body {
        data object Loading : Body
        data object WaitingForTheGroup : Body
        data class Failed(val error: NoAnswer) : Body
        data class InGroup(val groupName: String) : Body
    }
}
