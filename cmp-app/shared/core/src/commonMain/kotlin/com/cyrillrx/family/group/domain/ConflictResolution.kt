package com.cyrillrx.family.group.domain

/**
 * Resolves two versions of the same record: the later revision wins.
 *
 * A deletion is one of those versions, so a deletion landing after an edit removes the record and
 * an edit landing after a deletion keeps it. PRD-001 v0.3 takes this rule from the store rather
 * than overriding it — there is no conflict layer of ours to tune.
 *
 * **This function models what the store does; it is not what runs in production.** The Firestore
 * implementation resolves server-side and never calls this. `RamGroupRepository` does call it, and
 * the tests assert on it, which is how PRD-001's "same result on every device" stays verifiable
 * without a network. The risk is real: if the store's behaviour and this rule drift apart, the
 * tests keep passing while the product misbehaves. Anything learnt about the store's actual
 * semantics belongs in this function.
 *
 * Equal timestamps fall back to the author, a value both devices already agree on, so the outcome
 * does not depend on the order the two versions arrived in. When author and timestamp are both
 * equal the two versions are the same write, and either may be returned.
 */
fun <T : SharedRecord> resolveConflict(a: T, b: T): T {
    val byTime = a.revision.updatedAt.compareTo(b.revision.updatedAt)
    if (byTime != 0) return if (byTime > 0) a else b

    val byAuthor = a.revision.updatedBy.value.compareTo(b.revision.updatedBy.value)
    return if (byAuthor >= 0) a else b
}
