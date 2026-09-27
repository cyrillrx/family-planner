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
}
