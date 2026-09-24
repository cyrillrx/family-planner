package com.cyrillrx.family.app

import com.cyrillrx.family.group.domain.GroupRepository
import com.cyrillrx.family.group.domain.InvitationRepository
import com.cyrillrx.family.group.domain.Onboarding
import com.cyrillrx.family.group.domain.RamGroupRepository
import com.cyrillrx.family.group.domain.RamUserRepository
import com.cyrillrx.family.group.domain.SampleInvitationRepository
import com.cyrillrx.family.group.domain.UserRepository

/**
 * The composition root, wired by hand: one screen does not need a container.
 *
 * Everything is in memory and dies with the process. Persistence is Firestore's offline cache
 * (ADR-003), and the api implementations land with the owned service — until then these are what
 * the application has to talk to.
 */
class AppDependencies(
    userRepository: UserRepository = RamUserRepository(),
    groupRepository: GroupRepository = RamGroupRepository(),
    invitationRepository: InvitationRepository = SampleInvitationRepository(),
) {
    val onboarding = Onboarding(userRepository, groupRepository, invitationRepository)
}
