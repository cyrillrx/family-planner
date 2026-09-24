package com.cyrillrx.family.presentation

import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.domain.InvitationRepository
import com.cyrillrx.family.group.domain.RamGroupRepository
import com.cyrillrx.family.group.domain.RamUserRepository
import com.cyrillrx.family.group.domain.RedeemInvitationError
import com.cyrillrx.family.group.domain.RegisterUserError
import com.cyrillrx.family.group.domain.SampleInvitationRepository
import com.cyrillrx.family.group.domain.UserRepository
import com.cyrillrx.family.group.domain.model.Group
import com.cyrillrx.family.group.domain.model.GroupId
import com.cyrillrx.family.group.domain.model.User
import com.cyrillrx.family.group.domain.model.UserId
import kotlin.time.Instant

internal val NOW: Instant = Instant.fromEpochMilliseconds(1_700_000_000_000)

internal suspend fun registeredUserRepository() = RamUserRepository().apply {
    register(User(id = UserId("alice"), displayName = "Alice"))
}

internal suspend fun groupRepositoryWithAGroup() = RamGroupRepository().apply {
    setGroup(Group(id = GroupId("group-1"), name = "Home", createdAt = NOW))
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

internal class CountingInvitationRepository : InvitationRepository {
    var redemptions = 0
        private set

    private val delegate = SampleInvitationRepository()

    override suspend fun redeem(code: String, user: UserId) =
        delegate.redeem(code, user).also { redemptions++ }
}

internal class RefusingInvitationRepository(
    private val reason: RedeemInvitationError,
) : InvitationRepository {
    override suspend fun redeem(code: String, user: UserId) = Result.Failure(reason)
}
