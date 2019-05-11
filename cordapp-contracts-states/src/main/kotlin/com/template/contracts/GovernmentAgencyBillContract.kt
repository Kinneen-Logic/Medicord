package com.template.contract

import com.template.states.GovernmentAgencyBillState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

/**
 * A implementation of a basic smart contract in Corda.
 *
 * This contract enforces rules regarding the creation of a valid [GovernmentAgencyBillState]
 *
 * This contract checks for valid rules according to the medical card policy.
 *
 */

class GovernmentAgencyBillContract : Contract {
    companion object {
        @JvmStatic
        val GBS_CONTRACT_ID = "com.template.contract.GovernmentAgencyBillContract"
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
            val out = tx.outputsOfType<GovernmentAgencyBillState>().single()
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