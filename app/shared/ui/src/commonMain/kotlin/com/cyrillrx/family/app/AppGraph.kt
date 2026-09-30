package com.cyrillrx.family.app

import com.cyrillrx.family.group.domain.GroupRepository
import com.cyrillrx.family.group.domain.InvitationRepository
import com.cyrillrx.family.group.domain.Onboarding
import com.cyrillrx.family.group.domain.RamGroupRepository
import com.cyrillrx.family.group.domain.RamUserRepository
import com.cyrillrx.family.group.domain.SampleInvitationRepository
import com.cyrillrx.family.group.domain.UserRepository

class AppGraph(
    userRepository: UserRepository = RamUserRepository(),
    val groupRepository: GroupRepository = RamGroupRepository(),
    invitationRepository: InvitationRepository = SampleInvitationRepository(),
) {
    val onboarding = Onboarding(userRepository, groupRepository, invitationRepository)

    companion object {
        // Outside the composition: a configuration change destroys it but keeps the view models,
        // which would go on writing to the repositories of the graph that went with it.
        val shared by lazy { AppGraph() }
    }
}
