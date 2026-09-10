package com.cyrillrx.family.group.domain

import com.cyrillrx.core.domain.Error
import com.cyrillrx.core.domain.Result
import kotlinx.coroutines.flow.first

/**
 * The first launch asks for a name and one explicit choice (PRD-001). Nothing here runs on its own:
 * the app never creates a group silently, because an invited person who lands in a group of their
 * own cannot be extracted from it.
 */
class Onboarding(
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository,
    private val invitationRepository: InvitationRepository,
    private val currentUserStore: CurrentUserStore,
    private val groupFactory: GroupFactory = GroupFactory(),
    private val ids: IdGenerator = UuidIdGenerator,
) {

    suspend fun register(displayName: String): Result<User, RegisterError> {
        val name = displayName.trim()
        if (name.isEmpty()) return Result.Failure(RegisterError.BlankDisplayName)

        // Reuse the identifier this device already has: registration is idempotent, so an
        // interrupted onboarding resumes instead of leaving a second user behind.
        val id = currentUserStore.observeCurrentUserId().first() ?: ids.newUserId()

        return when (val registered = userRepository.register(User(id = id, displayName = name))) {
            is Result.Success -> registered.also { currentUserStore.setCurrentUserId(it.value.id) }
            is Result.Failure -> Result.Failure(RegisterError.Failed(registered.error))
        }
    }

    suspend fun createGroup(): Result<Group, CreateGroupError> {
        val userId = currentUserStore.observeCurrentUserId().first()
            ?: return Result.Failure(CreateGroupError.NotRegistered)

        if (groupRepository.observeGroup().first() != null) {
            return Result.Failure(CreateGroupError.GroupAlreadyExists)
        }

        val group = groupFactory.newGroup()

        groupRepository.setGroup(group)
        // The founder joins the moment the group exists, so both dates come from one reading.
        groupRepository.addMember(
            Member(userId = userId, groupId = group.id, joinedAt = group.createdAt),
        )

        return Result.Success(group)
    }

    suspend fun joinGroup(code: String): Result<GroupId, JoinGroupError> {
        val userId = currentUserStore.observeCurrentUserId().first()
            ?: return Result.Failure(JoinGroupError.NotRegistered)

        // Refused here rather than at the service: a string this short cannot be a code, and the
        // fewer paths it travels the fewer places it can end up in a log.
        if (code.length < Invitation.MIN_CODE_LENGTH) {
            return Result.Failure(JoinGroupError.InvalidCode)
        }

        if (groupRepository.observeGroup().first() != null) {
            return Result.Failure(JoinGroupError.AlreadyInAGroup)
        }

        // Only the redemption. The membership is the service's write, never ours (ADR-003), and the
        // redeemed invitation itself stays here — it carries the code.
        return when (val redeemed = invitationRepository.redeem(code, userId)) {
            is Result.Success -> Result.Success(redeemed.value.groupId)
            is Result.Failure -> Result.Failure(JoinGroupError.Redemption(redeemed.error))
        }
    }
}

sealed interface RegisterError : Error {
    data object BlankDisplayName : RegisterError
    data class Failed(val cause: RegisterUserError) : RegisterError
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
