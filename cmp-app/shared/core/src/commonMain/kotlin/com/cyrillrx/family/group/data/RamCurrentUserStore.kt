package com.cyrillrx.family.group.data

import com.cyrillrx.family.group.domain.CurrentUserStore
import com.cyrillrx.family.group.domain.UserId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class RamCurrentUserStore(initial: UserId? = null) : CurrentUserStore {

    private val current = MutableStateFlow(initial)

    override fun observeCurrentUserId(): Flow<UserId?> = current.asStateFlow()

    override suspend fun setCurrentUserId(id: UserId) {
        current.value = id
    }
}
