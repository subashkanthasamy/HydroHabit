package com.bose.hydrohabit.core.util

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Generates stable unique identifiers. Abstracted behind an interface so tests can supply a
 * deterministic generator. Backed by [kotlin.uuid.Uuid] (multiplatform, no expect/actual needed).
 */
interface IdGenerator {
    fun newId(): String
}

class UuidGenerator : IdGenerator {
    @OptIn(ExperimentalUuidApi::class)
    override fun newId(): String = Uuid.random().toString()
}
