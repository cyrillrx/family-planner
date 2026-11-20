package com.cyrillrx.family.presentation.groupnotfound

import com.cyrillrx.family.group.domain.NoAnswer

data class GroupNotFoundState(
    val submitting: Boolean = false,
    val error: NoAnswer? = null,
)
