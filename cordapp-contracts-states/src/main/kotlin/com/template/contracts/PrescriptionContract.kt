package com.template.contract

import com.template.states.PrescriptionState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

/**
 *
 * This contract enforces rules regarding the creation of a valid [PrescriptionState]
 *
 * For a new Patient to be issued onto the ledger, a transaction is required which takes:
 * - Zero input states.
 * - One output state: the new [PrescriptionState] -  The patients pharmacy prescription
 * - An Create() command with the public keys of both the doctor and the patient and pharmacy
 *
 * All contracts must sub-class the [Contract] interface.
 */
class PrescriptionContract : Contract {
    companion object {
        @JvmStatic
        val PRES_CONTRACT_ID = "com.template.contract.PrescriptionContract"
    }

    /**
     * The verify() function of all the states' contracts must not throw an exception for a transaction to be
     * considered valid.
     */
    override fun verify(tx : LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands.Create>()

        requireThat {
            // Generic constraints around the Initial transaction.
            "No inputs should be consumed when issuing an PrescriptionState." using (true)
            "Only one output state should be created." using (tx.outputs.size == 1)

            // Prescription-specific constraints.
            // Medicine must not be over regulated standards etc
        }
    }

    /**
     * This contract only implements one command, Create.
     */
    interface Commands : CommandData {
        class Create : Commands
    }
}