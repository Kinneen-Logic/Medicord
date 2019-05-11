package com.template.states

import net.corda.core.contracts.ContractState
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party


// *********
// * AppointmentState *
// A state for storing the information of a patients visit or appointment with a doctor
// including the patients symptoms of illness, the doctors notes, diagnosis and other essential information
// These states over time will be the basis of the patients own medical records
// This state implements the Corda LinearState class which requires the definition of participants that will store this state
// Which are clearly the patient and the doctor
// *********

data class AppointmentState(val symptoms : String,
                            val diagnosis : String,
                            val date : String,
                            val notes : String,
                            val prescription : String,
                            val patient : Party,
                            val doctor : Party,
                            override val linearId: UniqueIdentifier = UniqueIdentifier()) : ContractState, LinearState {

    override val participants: List<AbstractParty> = listOf(patient, doctor)

}

