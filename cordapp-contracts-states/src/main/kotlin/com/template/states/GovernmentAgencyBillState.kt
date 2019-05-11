package com.template.states

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty
import net.corda.core.transactions.LedgerTransaction
import net.corda.core.identity.Party

// *********
// * GA Bill State *
// A state to share the appointment prescription bill allowing a government agency to pay for the prescription, in
// the case there is public healthcare, like the medical card system in Ireland
// *********
data class GovernmentAgencyBillState(val price: String,
                                     val patient : Party,
                                     val doctor : Party,
                                     val pharmacy : Party,
                                     val governmentAgency : Party) : ContractState {

    override val participants: List<AbstractParty> = listOf(patient, doctor, pharmacy, governmentAgency)
}
