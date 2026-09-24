package com.cyrillrx.family.presentation.home

data class HomeState(val body: Body = Body.Loading) {

    sealed interface Body {
        data object Loading : Body
        data object WaitingForTheGroup : Body
        data class InGroup(val groupName: String) : Body
    }
}
