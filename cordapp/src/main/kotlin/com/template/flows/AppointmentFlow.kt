package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contract.AppointmentContract
import com.template.contract.AppointmentContract.Companion.APP_CONTRACT_ID
import com.template.states.AppointmentState
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

/**
 *  Appointment flow to model a doctor's visit,
 *  allowing a doctor to record information surrounding a Patient appointment
 */

object AppointmentFlow {
    @InitiatingFlow
    @StartableByRPC
    class AppointmentInitiatorFlow(val symptoms : String,
                                   val diagnosis : String,
                                   val date : String,
                                   val notes : String,
                                   val prescription : String,
                                   val patient : Party) : FlowLogic<SignedTransaction>() {
        /**
         * The progress tracker checkpoints each stage of the flow and outputs the specified messages when each
         * checkpoint is reached in the code. See the 'progressTracker.currentStep' expressions within the call() function.
         */
        companion object {
            object GENERATING_TRANSACTION : ProgressTracker.Step("Generating transaction based on new Appointment data.")
            object VERIFYING_TRANSACTION : ProgressTracker.Step("Verifying contract constraints.")
            object SIGNING_TRANSACTION : ProgressTracker.Step("Signing transaction with our private key.")
            object GATHERING_SIGS : ProgressTracker.Step("Gathering the counterparty's signature.") {
                override fun childProgressTracker() = CollectSignaturesFlow.tracker()
            }

            object FINALISING_TRANSACTION : ProgressTracker.Step("Obtaining notary signature and recording transaction, updating patient's medical history.") {
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
            val appointmentState = AppointmentState(symptoms, diagnosis, date, notes, prescription , patient, me) //constructor

            // Obtain reference to contract command -> the
            val requiredSigners = listOf(ourIdentity.owningKey, patient.owningKey)
            val createCommand = Command(AppointmentContract.Commands.Create(), requiredSigners)

            //create transaction builder
            val transactionBuilder = TransactionBuilder(notary)
                    .addCommand(createCommand)
                    .addOutputState(appointmentState, APP_CONTRACT_ID)

            // verify transaction contract is adhered to.
            transactionBuilder.verify(serviceHub) //if this is succesful, contract rules has been followed

            // Sign the transction
            val partSignedTx = serviceHub.signInitialTransaction(transactionBuilder)

            // Intiated a session down the wire with patient
            val patientSession = initiateFlow(patient)


            //Gather the patient's signature by using the [CollectionSignatureFlow]
            val fullySigned = subFlow(CollectSignaturesFlow(partSignedTx, setOf(patientSession)))


            // Notarise and record the transaction in both parties' vaults.
            return subFlow(FinalityFlow(fullySigned))
        }

    }

    @InitiatedBy(AppointmentInitiatorFlow::class)
    class Responder(val otherPartyFlow: FlowSession) : FlowLogic<SignedTransaction>() {
        @Suspendable
        override fun call(): SignedTransaction {
            val signTransactionFlow = object : SignTransactionFlow(otherPartyFlow) {
                override fun checkTransaction(stx: SignedTransaction) = requireThat {

                    val output = stx.tx.outputs.single().data
                    "The appointment notes must be accepted by the patient." using (output is AppointmentState)
                }
            }

            return subFlow(signTransactionFlow)
        }
    }
}
