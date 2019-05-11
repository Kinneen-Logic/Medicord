package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contract.PatientInitialContract
import com.template.contract.PatientInitialContract.Companion.PATIENT_INITIAL_CONTRACT
import com.template.states.PatientInitialState
import net.corda.core.contracts.Command
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.CordaService
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

/**
 *  Initial flow for Doctor to gather a new patients credentials
 */

object InitialFlow {
    @InitiatingFlow
    @StartableByRPC
    class PatientInitiatorFlow( val name: String,
                                val dob: String,
                                val address: String,
                                val phoneNumber: String,
                                val patient: Party) : FlowLogic<SignedTransaction>() {

        /**
         * The progress tracker checkpoints each stage of the flow and outputs the specified messages when each
         * checkpoint is reached in the code. See the 'progressTracker.currentStep' expressions within the call() function.
         */
        companion object {
            object GENERATING_TRANSACTION : ProgressTracker.Step("Generating transaction based on new Patients Credentials.")
            object VERIFYING_TRANSACTION : ProgressTracker.Step("Verifying contract constraints.")
            object SIGNING_TRANSACTION : ProgressTracker.Step("Signing transaction with our private key.")
            object GATHERING_SIGS : ProgressTracker.Step("Gathering the patient's signature.") {
                override fun childProgressTracker() = CollectSignaturesFlow.tracker()
            }

            object FINALISING_TRANSACTION : ProgressTracker.Step("Obtaining notary signature and recording transaction in participant's vaults.") {
                override fun childProgressTracker() = FinalityFlow.tracker()
            }

            fun tracker() = ProgressTracker(
                    GENERATING_TRANSACTION,
                    VERIFYING_TRANSACTION,
                    SIGNING_TRANSACTION,
                    GATHERING_SIGS,
                    FINALISING_TRANSACTION
            )

        }

        override val progressTracker = tracker()

        // call method is like the start or run method of the run
        @Suspendable
        override fun call(): SignedTransaction { //entry point <----
            // Flow implementation
            // Obtain a reference to the agreed notary
            val notary = serviceHub.networkMapCache.notaryIdentities[0]

            // Obtain reference to your own identity
            val me = serviceHub.myInfo.legalIdentities.first() //doctor

            //define state being created and written to participants ledgers
            val initialState = PatientInitialState(name, dob, address, phoneNumber, patient, me) //constructor

            // Obtain reference to contract command -> the
            val createCommand = Command(PatientInitialContract.Commands.Create(), initialState.participants.map { it.owningKey })


            progressTracker.currentStep = GENERATING_TRANSACTION

            //create transaction builder
            val transactionBuilder = TransactionBuilder(notary)
                    .addCommand(createCommand)
                    .addOutputState(initialState, PATIENT_INITIAL_CONTRACT)

            progressTracker.currentStep = VERIFYING_TRANSACTION
            // verify transaction contract is adhered to.
            transactionBuilder.verify(serviceHub) //if this is succesful, contract rules has been followed

            progressTracker.currentStep = SIGNING_TRANSACTION
            // Sign the transction
            val partSignedTx = serviceHub.signInitialTransaction(transactionBuilder)

            // Intiated a session down the wire with patient
            val patientSession = initiateFlow(patient)

            progressTracker.currentStep = GATHERING_SIGS

            //Gather the patient's signature by using the [CollectionSignatureFlow]
            val fullySigned = subFlow(CollectSignaturesFlow(partSignedTx, setOf(patientSession), GATHERING_SIGS.childProgressTracker()))

            progressTracker.currentStep = FINALISING_TRANSACTION

            // Notarise and record the transaction in both parties' vaults.
            return subFlow(FinalityFlow(fullySigned, FINALISING_TRANSACTION.childProgressTracker()))
        }

    }


    @InitiatedBy(PatientInitiatorFlow::class)
    class Responder(val otherPartyFlow: FlowSession) : FlowLogic<SignedTransaction>() {
        @Suspendable
        override fun call(): SignedTransaction {

            val signTransactionFlow = object : SignTransactionFlow(otherPartyFlow) {
                override fun checkTransaction(stx: SignedTransaction) = requireThat {

                    val output = stx.tx.outputs.single().data
                    "The credentials must be correct" using (output is PatientInitialState)
                }
            }

            return subFlow(signTransactionFlow)
        }
    }
}
