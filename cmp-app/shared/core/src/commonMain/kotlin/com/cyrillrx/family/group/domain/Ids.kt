package com.cyrillrx.family.group.domain

import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

@JvmInline
value class GroupId(val value: String)

/**
 * Identifies a member. Distinct from the credential that authenticates them, held in
 * [Member.credentialId]: a member keeps this identity when a credential is attached or replaced.
 */
@JvmInline
value class MemberId(val value: String)

/** Identifies an invitation. Distinct from [Invitation.code], which is the secret. */
@JvmInline
value class InvitationId(val value: String)

/** Identifiers are generated here rather than assigned by the store, so they survive a move. */
interface IdGenerator {
    fun newGroupId(): GroupId

    fun newMemberId(): MemberId

    fun newInvitationId(): InvitationId
}

object UuidIdGenerator : IdGenerator {
    override fun newGroupId(): GroupId = GroupId(Uuid.random().toString())

    override fun newMemberId(): MemberId = MemberId(Uuid.random().toString())

    override fun newInvitationId(): InvitationId = InvitationId(Uuid.random().toString())
}
