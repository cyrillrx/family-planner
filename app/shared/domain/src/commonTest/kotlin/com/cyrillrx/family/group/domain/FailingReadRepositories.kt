package com.cyrillrx.family.group.domain

import com.cyrillrx.core.domain.Result

internal class UnavailableUserRepository(
    private val delegate: UserRepository = RamUserRepository(),
) : UserRepository by delegate {
    override suspend fun registeredUserId() = Result.Failure(StoreUnavailable)
}

internal class UnavailableGroupRepository(
    private val delegate: GroupRepository = RamGroupRepository(),
) : GroupRepository by delegate {
    override suspend fun group() = Result.Failure(StoreUnavailable)
}
