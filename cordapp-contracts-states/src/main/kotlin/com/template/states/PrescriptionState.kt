package com.template.states

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty
import net.corda.core.transactions.LedgerTransaction
import net.corda.core.identity.Party

// *********
// * Prescription State *
// A state to store a patients prescription after each appointment
// This information is shared with the pharmacy for ease of payment and convenience, digitalising the traditional paper prescription
// *********
data class PrescriptionState(val details : String,
                            val quantity : String,
                            val medicine : String,
                            val patient : Party,
                            val pharmacy : Party,
                            val doctor : Party) : ContractState {

    override val participants: List<AbstractParty> = listOf(patient, doctor, pharmacy)
}
