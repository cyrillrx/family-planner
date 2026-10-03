package com.cyrillrx.family.presentation

import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.domain.GroupRepository
import com.cyrillrx.family.group.domain.RamGroupRepository
import com.cyrillrx.family.group.domain.RamUserRepository
import com.cyrillrx.family.group.domain.RegisterUserError
import com.cyrillrx.family.group.domain.UserRepository
import com.cyrillrx.family.group.model.Group
import com.cyrillrx.family.group.model.GroupId
import com.cyrillrx.family.group.model.User
import com.cyrillrx.family.group.model.UserId
import kotlin.time.Instant

internal val NOW: Instant = Instant.fromEpochMilliseconds(1_700_000_000_000)

internal suspend fun groupRepositoryWithAGroup() = RamGroupRepository().apply {
    setGroup(Group(id = GroupId("group-1"), name = "Home", createdAt = NOW))
}

internal suspend fun registeredUserRepository() = RamUserRepository().apply {
    register(User(id = UserId("alice"), displayName = "Alice"))
}

internal class AmnesicUserRepository : UserRepository {
    override suspend fun registeredUserId(): UserId? = null

    override suspend fun register(user: User) = Result.Success(user)
}

internal object FailingUserRepository : UserRepository {
    override suspend fun registeredUserId(): UserId? = null

    override suspend fun register(user: User) = Result.Failure(RegisterUserError.Unknown)
}

internal class CountingUserRepository : UserRepository {
    var registrations = 0
        private set

    private val delegate = RamUserRepository()

    override suspend fun registeredUserId() = delegate.registeredUserId()

    override suspend fun register(user: User) = delegate.register(user).also { registrations++ }
}

internal class CountingGroupRepository(
    private val delegate: RamGroupRepository = RamGroupRepository(),
) : GroupRepository by delegate {
    var reads = 0
        private set

    override suspend fun group() = delegate.group().also { reads++ }
}
