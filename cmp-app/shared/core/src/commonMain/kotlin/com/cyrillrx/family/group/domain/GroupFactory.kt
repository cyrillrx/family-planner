package com.cyrillrx.family.group.domain

import kotlin.time.Clock

/**
 * The group is not given a name: PRD-001 has it generated and displayed nowhere in V1, and it
 * becomes editable only if multi-group arrives and one group has to be told from another.
 */
class GroupFactory(
    private val ids: IdGenerator = UuidIdGenerator,
    private val clock: Clock = Clock.System,
) {

    fun newGroup() = Group(id = ids.newGroupId(), name = DEFAULT_NAME, createdAt = clock.now())

    private companion object {
        const val DEFAULT_NAME = "Family"
    }
}
