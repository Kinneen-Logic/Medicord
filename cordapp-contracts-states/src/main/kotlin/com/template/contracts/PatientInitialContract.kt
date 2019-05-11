package com.template.contract

import com.template.states.PatientInitialState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

/**
 * A implementation of a basic smart contract in Corda.
 *
 * This contract enforces rules regarding the creation of a valid [PatientInitialContract]
 *
 * For a new Patient to be issued onto the ledger, a transaction is required which takes:
 * - Zero input states.
 * - One output state: the new [InitialPatientState] -  The patients credentials
 * - An Create() command with the public keys of both the doctor and the patient.
 *
 * A [PatientInitialState] can also be Updated, and Removed from the ledger
 *
 * All contracts must sub-class the [Contract] interface.
 */
class PatientInitialContract : Contract {
    companion object {
        @JvmStatic
        val PATIENT_INITIAL_CONTRACT = "com.template.contract.PatientInitialContract"
    }

    /**
     * The verify() function of all the states' contracts must not throw an exception for a transaction to be
     * considered valid.
     */
    override fun verify(tx : LedgerTransaction) {
        val command = tx.getCommand<CommandData>(0)

        if(command.value is Commands.Create){
            requireThat {
                // Generic constraints around the Initial transaction.
                /*
                "No inputs should be consumed when issuing an PatientInitialState." using (tx.inputs.isEmpty())
                "Only one output state should be created." using (tx.outputs.size == 1)
                val out = tx.outputsOfType<PatientInitialState>().single()
                "The doctor and the patient cannot be the same entity." using (out.patient != out.doctor)
                "All of the participants must be signers." using (command.signers.containsAll(out.participants.map { it.owningKey }))

                // Patient-specific constraints.
                "The patients's age must be non-negative." using (out.dob != "0")
                */
            }
        }

        else if(command.value is Commands.Update){
            requireThat {
                val input = tx.inputsOfType<PatientInitialState>().single()
                val output = tx.outputsOfType<PatientInitialState>().single()
                "the updated address must not equal to current address" using (input.address != output.address)
            }
        }

        else if(command.value is Commands.Withdraw){
            requireThat {
                val input = tx.inputsOfType<PatientInitialState>().single()
                val output = tx.outputsOfType<PatientInitialState>()
                "The patient state should no longer exist" using (output.isEmpty())
            }
        }

    }

    /**
     * This contract only implements one command, Create.
     */
    interface Commands : CommandData {
        class Create : Commands
        class Update : Commands
        class Withdraw : Commands
    }
}