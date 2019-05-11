package com.template.states

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty
import net.corda.core.transactions.LedgerTransaction
import net.corda.core.identity.Party

// *********
// * Insurer Bill State *
// A state to share the bill for a patient-doctor appointment with an insurer like VHI etc
// who can then proceed to pay for the appointment if the patient is adequetly insured
// *********
data class InsurerBillState(val price: String,
                        val vat : String,
                        val patient : Party,
                        val doctor : Party,
                        val insurer : Party) : ContractState {

    override val participants: List<AbstractParty> = listOf(patient, doctor, insurer)
}
