package com.cyrillrx.family.group.domain

import com.cyrillrx.family.group.model.GroupId
import com.cyrillrx.family.group.model.InvitationId
import com.cyrillrx.family.group.model.UserId
import kotlin.uuid.Uuid

interface IdGenerator {
    fun newGroupId(): GroupId

    fun newUserId(): UserId

    fun newInvitationId(): InvitationId
}

object UuidIdGenerator : IdGenerator {
    override fun newGroupId(): GroupId = GroupId(Uuid.random().toString())

    override fun newUserId(): UserId = UserId(Uuid.random().toString())

    override fun newInvitationId(): InvitationId = InvitationId(Uuid.random().toString())
}
