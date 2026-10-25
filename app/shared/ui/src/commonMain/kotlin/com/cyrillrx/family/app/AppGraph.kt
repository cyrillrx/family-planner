package com.cyrillrx.family.app

import com.cyrillrx.family.group.data.UserStore
import com.cyrillrx.family.group.data.firestore.firestoreGroupStore
import com.cyrillrx.family.group.data.firestore.firestoreUserStore
import com.cyrillrx.family.group.domain.GroupRepository
import com.cyrillrx.family.group.domain.InvitationRepository
import com.cyrillrx.family.group.domain.Onboarding
import com.cyrillrx.family.group.domain.RamGroupRepository
import com.cyrillrx.family.group.domain.RamUserRepository
import com.cyrillrx.family.group.domain.SampleInvitationRepository
import com.cyrillrx.family.group.domain.StoreGroupRepository
import com.cyrillrx.family.group.domain.StoreUserRepository
import com.cyrillrx.family.group.domain.UserRepository

class AppGraph(
    userStore: UserStore? = firestoreUserStore(),
    userRepository: UserRepository = userStore?.let(::StoreUserRepository) ?: RamUserRepository(),
    val groupRepository: GroupRepository = storeGroupRepository(userStore) ?: RamGroupRepository(),
    invitationRepository: InvitationRepository = SampleInvitationRepository(),
) {
    val onboarding = Onboarding(userRepository, groupRepository, invitationRepository)

    companion object {
        // Outside the composition: a configuration change destroys it but keeps the view models,
        // which would go on writing to the repositories of the graph that went with it.
        val shared by lazy { AppGraph() }
    }
}

private fun storeGroupRepository(userStore: UserStore?): GroupRepository? {
    val groupStore = firestoreGroupStore() ?: return null

    return userStore?.let { StoreGroupRepository(groupStore, it) }
}
