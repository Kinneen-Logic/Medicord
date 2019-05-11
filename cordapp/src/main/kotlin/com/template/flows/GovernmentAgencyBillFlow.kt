package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contract.GovernmentAgencyBillContract
import com.template.contract.GovernmentAgencyBillContract.Companion.GBS_CONTRACT_ID
import com.template.states.GovernmentAgencyBillState
import com.template.states.PrescriptionState
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

/**
 * NOTE : Could be multiple responder flows for various parties and their custom contract verifications
 */

object GovernmentAgencyBillFlow {
    @InitiatingFlow
    @StartableByRPC
    class GovInitiatorFlow(val price: String, // name might be wrong, who would initiate this flow - doctor or patient?
                         val patient : Party,
                         val doctor : Party,
                         val pharmacy : Party,
                         val governmentAgency : Party) : FlowLogic<SignedTransaction>() {



        @Suspendable
        override fun call(): SignedTransaction { //entry point <----
            // Flow implementation
            // Obtain a reference to the agreed notary
            val notary = serviceHub.networkMapCache.notaryIdentities[0]

            // **** FIX **** Obtain reference to your own identity - doctor ? whoever initiates the flow
            val me = serviceHub.myInfo.legalIdentities.first() //doctor

            //define state being created and written to participants ledgers
            val governmentAgencyBillState = GovernmentAgencyBillState(price, patient, pharmacy, governmentAgency, me) //constructor

            // Obtain reference to contract command -> the
            val createCommand = Command(GovernmentAgencyBillContract.Commands.Create(), governmentAgencyBillState.participants.map { it.owningKey })

            //create transaction builder
            val transactionBuilder = TransactionBuilder(notary)
                    .addCommand(createCommand)
                    .addOutputState(governmentAgencyBillState, GBS_CONTRACT_ID)

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

    @InitiatedBy(GovInitiatorFlow::class)
    class Responder(val otherPartyFlow: FlowSession) : FlowLogic<SignedTransaction>() {
        @Suspendable
        override fun call(): SignedTransaction {
            val signTransactionFlow = object : SignTransactionFlow(otherPartyFlow) {
                override fun checkTransaction(stx: SignedTransaction) = requireThat {
                    val output = stx.tx.outputs.single().data
                }
            }

            return subFlow(signTransactionFlow)
        }
    }
}
