package com.cyrillrx.family.group.domain

/**
 * Resolves two versions of the same record: the later revision wins, a deletion being one such
 * version.
 *
 * This mirrors the store's own resolution rather than replacing it, so that the rule can be
 * asserted without a network. Anything learnt about the store's actual semantics belongs here.
 */
fun <T : SharedRecord> resolveConflict(a: T, b: T): T {
    val byTime = a.revision.updatedAt.compareTo(b.revision.updatedAt)
    if (byTime != 0) return if (byTime > 0) a else b

    // Equal timestamps fall back to a value both devices agree on, or the winner would depend on
    // the order the versions arrived in and the two would never converge.
    val byAuthor = a.revision.updatedBy.value.compareTo(b.revision.updatedBy.value)
    return if (byAuthor >= 0) a else b
}
