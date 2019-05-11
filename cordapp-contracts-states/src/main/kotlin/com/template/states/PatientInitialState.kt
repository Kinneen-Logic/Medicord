package com.template.states

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty
import net.corda.core.transactions.LedgerTransaction
import net.corda.core.identity.Party

// *********
// * Initial State *
// A state to store the credentials of each new Patient that visits a doctor
// *********
data class PatientInitialState(val name: String,
                               val dob : String,
                               val address : String,
                               val phoneNumber : String,
                               val patient : Party,
                               val doctor : Party) : ContractState {

                        override val participants: List<AbstractParty> = listOf(patient, doctor)
}