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
    private val idGenerator: IdGenerator = UuidIdGenerator,
    private val groupFactory: GroupFactory = GroupFactory(idGenerator),
) {

    suspend fun register(displayName: String): Result<User, RegisterError> {
        val name = displayName.trim()
        if (name.isEmpty()) return Result.Failure(RegisterError.BlankDisplayName)

        val id = userRepository.registeredUserId() ?: idGenerator.newUserId()

        return when (val registered = userRepository.register(User(id = id, displayName = name))) {
            is Result.Success -> registered
            is Result.Failure -> Result.Failure(RegisterError.Registration(registered.error))
        }
    }

    suspend fun createGroup(): Result<Group, CreateGroupError> {
        val userId = userRepository.registeredUserId()
            ?: return Result.Failure(CreateGroupError.NotRegistered)

        if (groupRepository.group() != null) {
            return Result.Failure(CreateGroupError.GroupAlreadyExists)
        }

        val group = groupFactory.create()

        // TODO(#16): one guarded write — a failure between the two strands the founder outside the group.
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

        // Trimmed like the display name: a code pasted from a message carries what surrounds it.
        val trimmed = code.trim()

        // Refused here rather than at the service: a string this short cannot be a code, and the
        // fewer paths it travels the fewer places it can end up in a log.
        if (trimmed.length < Invitation.MIN_CODE_LENGTH) {
            return Result.Failure(JoinGroupError.InvalidCode)
        }

        if (groupRepository.group() != null) {
            return Result.Failure(JoinGroupError.AlreadyInAGroup)
        }

        // Only the redemption. The membership is the service's write, never ours (ADR-003), and the
        // redeemed invitation stays here — it carries the code.
        return when (val redeemed = invitationRepository.redeem(trimmed, userId)) {
            is Result.Success -> Result.Success(redeemed.value.groupId)
            is Result.Failure -> Result.Failure(JoinGroupError.Redemption(redeemed.error))
        }
    }
}

sealed interface RegisterError : Error {
    data object BlankDisplayName : RegisterError
    data class Registration(val cause: RegisterUserError) : RegisterError
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
