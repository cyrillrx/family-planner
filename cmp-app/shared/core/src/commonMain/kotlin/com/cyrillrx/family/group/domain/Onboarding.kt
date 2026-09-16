package com.cyrillrx.family.group.domain

import com.cyrillrx.core.domain.Error
import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.domain.model.Group
import com.cyrillrx.family.group.domain.model.GroupId
import com.cyrillrx.family.group.domain.model.Invitation
import com.cyrillrx.family.group.domain.model.Member
import com.cyrillrx.family.group.domain.model.User

class Onboarding(
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository,
    private val invitationRepository: InvitationRepository,
    private val groupFactory: GroupFactory = GroupFactory(),
    private val idGenerator: IdGenerator = UuidIdGenerator,
) {

    suspend fun register(displayName: String): Result<User, RegisterUserError> {
        val name = displayName.trim()
        if (name.isEmpty()) return Result.Failure(RegisterUserError.BlankDisplayName)

        val id = userRepository.registeredUserId() ?: idGenerator.newUserId()

        return userRepository.register(User(id = id, displayName = name))
    }

    suspend fun createGroup(): Result<Group, CreateGroupError> {
        val userId = userRepository.registeredUserId()
            ?: return Result.Failure(CreateGroupError.NotRegistered)

        if (groupRepository.group() != null) {
            return Result.Failure(CreateGroupError.GroupAlreadyExists)
        }

        val group = groupFactory.create()

        groupRepository.setGroup(group)
        // The founder joins the moment the group exists, so both dates come from one reading.
        groupRepository.addMember(
            Member(userId = userId, groupId = group.id, joinedAt = group.createdAt),
        )

        return Result.Success(group)
    }

    suspend fun joinGroup(code: String): Result<GroupId, JoinGroupError> {
        val userId = userRepository.registeredUserId()
            ?: return Result.Failure(JoinGroupError.NotRegistered)

        // Refused here rather than at the service: a string this short cannot be a code, and the
        // fewer paths it travels the fewer places it can end up in a log.
        if (code.length < Invitation.MIN_CODE_LENGTH) {
            return Result.Failure(JoinGroupError.InvalidCode)
        }

        if (groupRepository.group() != null) {
            return Result.Failure(JoinGroupError.AlreadyInAGroup)
        }

        // Only the redemption. The membership is the service's write, never ours (ADR-003), and the
        // redeemed invitation stays here — it carries the code.
        return when (val redeemed = invitationRepository.redeem(code, userId)) {
            is Result.Success -> Result.Success(redeemed.value.groupId)
            is Result.Failure -> Result.Failure(JoinGroupError.Redemption(redeemed.error))
        }
    }
}

sealed interface CreateGroupError : Error {
    data object NotRegistered : CreateGroupError
    data object GroupAlreadyExists : CreateGroupError
}

sealed interface JoinGroupError : Error {
    data object NotRegistered : JoinGroupError
    data object InvalidCode : JoinGroupError
    data object AlreadyInAGroup : JoinGroupError
    data class Redemption(val cause: RedeemInvitationError) : JoinGroupError
}
