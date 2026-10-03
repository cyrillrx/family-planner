package com.cyrillrx.family.presentation.onboarding.groupchoice

import com.cyrillrx.family.group.domain.CreateGroupError

data class GroupChoiceState(
    val submitting: Boolean = false,
    val error: CreateGroupError? = null,
)
