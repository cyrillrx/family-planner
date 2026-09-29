package com.cyrillrx.family.presentation

import com.cyrillrx.family.group.domain.GroupRepository
import com.cyrillrx.family.group.domain.RamGroupRepository
import com.cyrillrx.family.group.model.Group
import com.cyrillrx.family.group.model.GroupId
import kotlin.time.Instant

internal val NOW: Instant = Instant.fromEpochMilliseconds(1_700_000_000_000)

internal suspend fun groupRepositoryWithAGroup() = RamGroupRepository().apply {
    setGroup(Group(id = GroupId("group-1"), name = "Home", createdAt = NOW))
}

internal class CountingGroupRepository(
    private val delegate: RamGroupRepository = RamGroupRepository(),
) : GroupRepository by delegate {
    var reads = 0
        private set

    override suspend fun group() = delegate.group().also { reads++ }
}
