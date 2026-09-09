package com.cyrillrx.family.group.domain

import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

@JvmInline
value class GroupId(val value: String)

@JvmInline
value class UserId(val value: String)

@JvmInline
value class InvitationId(val value: String)

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
