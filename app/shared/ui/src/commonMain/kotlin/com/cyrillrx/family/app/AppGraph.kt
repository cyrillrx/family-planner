package com.cyrillrx.family.app

import com.cyrillrx.family.group.domain.GroupRepository
import com.cyrillrx.family.group.domain.RamGroupRepository

class AppGraph(val groupRepository: GroupRepository = RamGroupRepository()) {

    companion object {
        // Outside the composition: a configuration change destroys it but keeps the view models,
        // which would go on writing to the repositories of the graph that went with it.
        val shared by lazy { AppGraph() }
    }
}
