package com.template.contract

import com.template.states.InsurerBillState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

/**
 * A implementation of a basic smart contract in Corda.
 *
 * This contract enforces rules regarding the creation of a valid [InsurerBillState]
 *
 * Examples of these rules may be calculating and comparing the price of medicine prescribed and comparing
 * to expected of previous prescriptions. Rules can be defined to see whether a patient is covered by insurance at all.
 *
 * All contracts must sub-class the [Contract] interface.
 */
class InsurerBillContract : Contract {
    companion object {
        @JvmStatic
        val IBC__CONTRACT_ID = "com.template.contract.InsurerBillContract"
    }

    /**
     * The verify() function of all the states' contracts must not throw an exception for a transaction to be
     * considered valid.
     */
    override fun verify(tx : LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands.Create>()

        requireThat {
            // Generic constraints around the Initial transaction.
            "Only one output state should be created." using (tx.outputs.size == 1)
            val out = tx.outputsOfType<InsurerBillState>().single()
            "All of the participants must be signers." using (command.signers.containsAll(out.participants.map { it.owningKey }))

            // Bill-specific constraints.

        }
    }

    /**
     * This contract only implements one command, Create.
     */
    interface Commands : CommandData {
        class Create : Commands
    }
}