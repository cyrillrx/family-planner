package com.cyrillrx.family.group.domain

import com.cyrillrx.core.domain.Error
import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.domain.model.Group
import com.cyrillrx.family.group.domain.model.Member
import com.cyrillrx.family.group.domain.model.User

class Onboarding(
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository,
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

        val group = groupFactory.newGroup()

        groupRepository.setGroup(group)
        // The founder joins the moment the group exists, so both dates come from one reading.
        groupRepository.addMember(
            Member(userId = userId, groupId = group.id, joinedAt = group.createdAt),
        )

        return Result.Success(group)
    }
}

sealed interface CreateGroupError : Error {
    data object NotRegistered : CreateGroupError
    data object GroupAlreadyExists : CreateGroupError
}
