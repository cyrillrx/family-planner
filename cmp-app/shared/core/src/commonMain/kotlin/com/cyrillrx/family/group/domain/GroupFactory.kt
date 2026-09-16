package com.cyrillrx.family.group.domain

import com.cyrillrx.family.group.domain.model.Group
import kotlin.time.Clock

class GroupFactory(
    private val idGenerator: IdGenerator = UuidIdGenerator,
    private val clock: Clock = Clock.System,
) {

    fun newGroup() = Group(id = idGenerator.newGroupId(), name = DEFAULT_NAME, createdAt = clock.now())

    private companion object {
        const val DEFAULT_NAME = "Family"
    }
}
