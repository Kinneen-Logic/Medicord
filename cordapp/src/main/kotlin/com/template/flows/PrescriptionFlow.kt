package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contract.PrescriptionContract
import com.template.contract.PrescriptionContract.Companion.PRES_CONTRACT_ID
import com.template.states.AppointmentState
import com.template.states.PrescriptionState
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

/**
 *  Prescription flow to model the multi-step, multi-party agreement on the a patients medical prescription
 *  This flow chooses a pharmacy in the network to link into the transaction such that they have an up-to-date
 *  shared, identical copy of the prescription data, digitalising the cumbersome paper process
 */

object PrescriptionFlow {
    @InitiatingFlow
    @StartableByRPC
    class PrescriptionInitiatorFlow(val details : String,
                                    val quantity : String,
                                    val medicine : String,
                                    val patient : Party,
                                    val pharmacy : Party) : FlowLogic<SignedTransaction>() {

        /**
         * The progress tracker checkpoints each stage of the flow and outputs the specified messages when each
         * checkpoint is reached in the code. See the 'progressTracker.currentStep' expressions within the call() function.
         */
        companion object {
            object GENERATING_TRANSACTION : ProgressTracker.Step("Generating transaction based on new IOU.")
            object VERIFYING_TRANSACTION : ProgressTracker.Step("Verifying contract constraints.")
            object SIGNING_TRANSACTION : ProgressTracker.Step("Signing transaction with our private key.")
            object GATHERING_SIGS : ProgressTracker.Step("Gathering the counterparty's signature.") {
                override fun childProgressTracker() = CollectSignaturesFlow.tracker()
            }

            object FINALISING_TRANSACTION : ProgressTracker.Step("Obtaining notary signature and recording transaction.") {
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

        @Suspendable
        override fun call(): SignedTransaction { //entry point <----
            // Flow implementation
            // Obtain a reference to the agreed notary
            val notary = serviceHub.networkMapCache.notaryIdentities[0]

            // Obtain reference to your own identity
            val me = serviceHub.myInfo.legalIdentities.first() //doctor

            //define state being created and written to participants ledgers
            val prescriptionState = PrescriptionState(details, quantity, medicine, patient, pharmacy, me) //constructor

            // Obtain reference to contract command -> the
            val createCommand = Command(PrescriptionContract.Commands.Create(), prescriptionState.participants.map { it.owningKey })

            //create transaction builder
            val transactionBuilder = TransactionBuilder(notary)
                    .addCommand(createCommand)
                    .addOutputState(prescriptionState, PRES_CONTRACT_ID)

            // verify transaction contract is adhered to.
            transactionBuilder.verify(serviceHub) //if this is succesful, contract rules has been followed

            // Sign the transction
            val partSignedTx = serviceHub.signInitialTransaction(transactionBuilder)

            // Intiated a session down the wire with patient
            val patientSession = initiateFlow(patient)
            val pharmacySession = initiateFlow(pharmacy)


            //Gather the patient's signature by using the [CollectionSignatureFlow]
            val fullySigned = subFlow(CollectSignaturesFlow(partSignedTx, setOf(patientSession,pharmacySession)))

            // Notarise and record the transaction in both parties' vaults.
            return subFlow(FinalityFlow(fullySigned))
        }

    }

    @InitiatedBy(PrescriptionInitiatorFlow::class)
    class Responder(val otherPartyFlow: FlowSession) : FlowLogic<SignedTransaction>() {
        @Suspendable
        override fun call(): SignedTransaction {
            val signTransactionFlow = object : SignTransactionFlow(otherPartyFlow) {
                override fun checkTransaction(stx: SignedTransaction) = requireThat {

                    val output = stx.tx.outputs.single().data
                    "The appointment notes must be accepted by the patient." using (output is PrescriptionState)
                }
            }

            return subFlow(signTransactionFlow)
        }
    }
}
