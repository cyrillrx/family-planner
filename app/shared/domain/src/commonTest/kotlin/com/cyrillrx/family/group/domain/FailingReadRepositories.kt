package com.cyrillrx.family.group.domain

import com.cyrillrx.core.domain.Result

internal class UnavailableUserRepository(
    private val delegate: UserRepository = RamUserRepository(),
) : UserRepository by delegate {
    override suspend fun registeredUserId() = Result.Failure(StoreUnavailable)
}

internal class MissingGroupRepository(
    private val delegate: GroupRepository = RamGroupRepository(),
) : GroupRepository by delegate {
    var departures = 0
        private set

    override suspend fun group() = Result.Failure(GroupNotFound)

    override suspend fun leaveMissingGroup() = Result.Success(Unit).also { departures++ }
}

internal class UnavailableGroupRepository(
    private val delegate: GroupRepository = RamGroupRepository(),
) : GroupRepository by delegate {
    override suspend fun group() = Result.Failure(StoreUnavailable)
}
