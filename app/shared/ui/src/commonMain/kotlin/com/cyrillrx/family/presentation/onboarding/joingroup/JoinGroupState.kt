package com.cyrillrx.family.presentation.onboarding.joingroup

import com.cyrillrx.family.group.domain.JoinGroupError

data class JoinGroupState(
    val code: String = "",
    val submitting: Boolean = false,
    val error: JoinGroupError? = null,
)
