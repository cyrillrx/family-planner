package com.cyrillrx.family.group.domain

import com.cyrillrx.core.domain.Error
import com.cyrillrx.core.domain.Result
import com.cyrillrx.core.domain.getOrElse
import com.cyrillrx.core.domain.map
import com.cyrillrx.family.group.model.Group
import com.cyrillrx.family.group.model.GroupId
import com.cyrillrx.family.group.model.Invitation
import com.cyrillrx.family.group.model.User

class Onboarding(
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository,
    private val invitationRepository: InvitationRepository,
    private val idGenerator: IdGenerator = UuidIdGenerator,
    private val groupFactory: GroupFactory = GroupFactory(idGenerator),
) {

    /** The step to resume at, for someone not in a group yet: a member of a group is past the onboarding. */
    suspend fun currentStep(): Result<OnboardingStep, NoAnswer> =
        userRepository.registeredUserId().map { registration ->
            when (registration) {
                is Registration.Registered -> OnboardingStep.GROUP_CHOICE
                Registration.NotRegistered -> OnboardingStep.DISPLAY_NAME
            }
        }

    suspend fun register(displayName: String): Result<User, RegisterError> {
        val name = displayName.trim()
        if (name.isEmpty()) return Result.Failure(RegisterError.BlankDisplayName)

        val registration = userRepository.registeredUserId()
            .getOrElse { return Result.Failure(RegisterError.Registration(it)) }

        // TODO(#71): a store-backed repository discards this identifier; let the repository own it.
        val id = when (registration) {
            is Registration.Registered -> registration.id
            Registration.NotRegistered -> idGenerator.newUserId()
        }

        return when (val registered = userRepository.register(User(id = id, displayName = name))) {
            is Result.Success -> registered
            is Result.Failure -> Result.Failure(RegisterError.Registration(registered.error))
        }
    }

    suspend fun createGroup(): Result<Group, CreateGroupError> {
        val registration = userRepository.registeredUserId()
            .getOrElse { return Result.Failure(CreateGroupError.Creation(it)) }

        val userId = when (registration) {
            is Registration.Registered -> registration.id
            Registration.NotRegistered -> return Result.Failure(CreateGroupError.NotRegistered)
        }

        val group = groupFactory.create()

        return when (val created = groupRepository.create(group, founder = userId)) {
            is Result.Success -> Result.Success(group)
            is Result.Failure -> Result.Failure(CreateGroupError.Creation(created.error))
        }
    }

    suspend fun joinGroup(code: String): Result<GroupId, JoinGroupError> {
        val registration = userRepository.registeredUserId()
            .getOrElse { return Result.Failure(JoinGroupError.Lookup(it)) }

        val userId = when (registration) {
            is Registration.Registered -> registration.id
            Registration.NotRegistered -> return Result.Failure(JoinGroupError.NotRegistered)
        }

        // Trimmed like the display name: a code pasted from a message carries what surrounds it.
        val trimmed = code.trim()

        // Refused here rather than at the service: a string this short cannot be a code, and the
        // fewer paths it travels the fewer places it can end up in a log.
        if (trimmed.length < Invitation.MIN_CODE_LENGTH) {
            return Result.Failure(JoinGroupError.CodeTooShort)
        }

        val membership = groupRepository.group().getOrElse { return Result.Failure(JoinGroupError.Lookup(it)) }
        if (membership is Membership.InGroup) return Result.Failure(JoinGroupError.AlreadyInAGroup)

        // Only the redemption. The membership is the service's write, never ours (ADR-003), and the
        // redeemed invitation stays here — it carries the code.
        return when (val redeemed = invitationRepository.redeem(trimmed, userId)) {
            is Result.Success -> Result.Success(redeemed.value.groupId)
            is Result.Failure -> Result.Failure(JoinGroupError.Redemption(redeemed.error))
        }
    }

    suspend fun leaveMissingGroup(): Result<Unit, NoAnswer> = groupRepository.leaveMissingGroup()
}

enum class OnboardingStep { DISPLAY_NAME, GROUP_CHOICE }

sealed interface RegisterError : Error {
    data object BlankDisplayName : RegisterError
    data class Registration(val cause: RegisterUserError) : RegisterError
}

sealed interface CreateGroupError : Error {
    data object NotRegistered : CreateGroupError
    data class Creation(val cause: GroupError.Write) : CreateGroupError
}

sealed interface JoinGroupError : Error {
    data object NotRegistered : JoinGroupError
    data object CodeTooShort : JoinGroupError
    data object AlreadyInAGroup : JoinGroupError
    data class Lookup(val cause: GroupError.Read) : JoinGroupError
    data class Redemption(val cause: RedeemInvitationError) : JoinGroupError
}
