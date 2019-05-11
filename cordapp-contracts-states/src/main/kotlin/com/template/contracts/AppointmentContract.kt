package com.template.contract

import com.template.states.AppointmentState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

/**
 * A implementation of a basic smart contract in Corda.
 *
 * This contract enforces rules regarding the creation of a valid [InitialState]
 *
 * For a new Patient to be issued onto the ledger, a transaction is required which takes:
 * - Zero input states.
 * - One output state: the new [InitialPatientState] -  The patients credentials
 * - An Create() command with the public keys of both the doctor and the patient.
 *
 * All contracts must sub-class the [Contract] interface.
 */
class AppointmentContract : Contract {
    companion object {
        @JvmStatic
        val APP_CONTRACT_ID = "com.template.contract.PatientInitialContract"
    }

    /**
     * The verify() function of all the states' contracts must not throw an exception for a transaction to be
     * considered valid.
     */
    override fun verify(tx : LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands.Create>()

        requireThat {
            // Generic constraints around the Initial transaction.
            "No inputs should be consumed when issuing an PatientInitialState." using (tx.inputs.isEmpty())
            "Only one output state should be created." using (tx.outputs.size == 1)
            val out = tx.outputsOfType<AppointmentState>().single()
            "The doctor and the patient cannot be the same entity." using (out.patient != out.doctor)
            "All of the participants must be signers." using (command.signers.containsAll(out.participants.map { it.owningKey }))

            // Patient-specific constraints.
            //"The patients's age must be non-negative." using (out.dob != "0")
            //"The appointment must be on a date later than last appointment" using (tx.input)
        }
    }

    /**
     * This contract only implements one command, Create.
     */
    interface Commands : CommandData {
        class Create : Commands
    }
}