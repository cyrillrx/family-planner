package com.cyrillrx.family.presentation.groupnotfound

import com.cyrillrx.family.group.domain.StoreUnavailable

data class GroupNotFoundState(
    val submitting: Boolean = false,
    val error: StoreUnavailable? = null,
)
