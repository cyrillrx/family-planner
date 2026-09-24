package com.cyrillrx.family.presentation.onboarding.displayname

import com.cyrillrx.family.group.domain.RegisterError

data class DisplayNameState(
    val displayName: String = "",
    val submitting: Boolean = false,
    val error: RegisterError? = null,
)
