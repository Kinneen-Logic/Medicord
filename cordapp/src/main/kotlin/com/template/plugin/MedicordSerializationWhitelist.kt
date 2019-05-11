package com.template.plugin

import net.corda.core.serialization.SerializationWhitelist

// Serialization whitelist.
class MedicordSerializationWhitelist : SerializationWhitelist {
    override val whitelist: List<Class<*>> = listOf(MedicordData::class.java)
}

// This class is not annotated with @CordaSerializable, so it must be added to the serialization whitelist, above, if
// we want to send it to other nodes within a flow.
data class MedicordData(val payload: String)