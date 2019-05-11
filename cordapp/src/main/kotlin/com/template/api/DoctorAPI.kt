package com.template.api

import com.template.states.AppointmentState
import com.template.states.PatientInitialState
import com.template.flows.InitialFlow.PatientInitiatorFlow
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.startTrackedFlow
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.IdentityService
import net.corda.core.utilities.getOrThrow
import net.corda.nodeapi.internal.serialization.amqp.EvolutionSerializer.Companion.logger
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.BAD_REQUEST
import javax.ws.rs.core.Response.Status.CREATED

val SERVICE_NAMES = listOf("Notary", "Network Map Service")

// *****************
// * API Endpoints *
// *****************

// This API is accessible from /api/medicord/doctor. All paths specified below are relative to it.
@Path("doctor")
class DoctorAPI(val rpcOps: CordaRPCOps) {
    val myLegalName: CordaX500Name = rpcOps.nodeInfo().legalIdentities.first().name

    /**
     * Returns the Doctor's name through API endpoint /api/medicord/me
     */
    @GET
    @Path("me")
    @Produces(MediaType.APPLICATION_JSON)
    fun whoami() = mapOf("me" to myLegalName)


    /**
     * Returns all parties registered with the [NetworkMapService]. These names can be used to look up identities
     * using the [IdentityService].
     */
    @GET
    @Path("peers")
    @Produces(MediaType.APPLICATION_JSON)
    fun getPeers(): Map<String, List<CordaX500Name>> {
        val nodeInfo = rpcOps.networkMapSnapshot()
        return mapOf("peers" to nodeInfo
                .map { it.legalIdentities.first().name }
                //filter out myself, notary and eventual network map started by driver
                .filter { it.organisation !in (SERVICE_NAMES + myLegalName.organisation) })
    }

    /**
     * Displays all Patient states that exist in the Doctor's vault.
     */
    @GET
    @Path("patients")
    @Produces(MediaType.APPLICATION_JSON)
    fun getPatients() = rpcOps.vaultQueryBy<PatientInitialState>().states

    @PUT
    @Path("create-patient")
    fun createPatient(@QueryParam("name") name: String,
                      @QueryParam("dob") dob: String,
                      @QueryParam("address") address: String,
                      @QueryParam("phoneNumber") phoneNumber: String,
                      @QueryParam("patient") patient: CordaX500Name) : Response {

        val patientAsParty = rpcOps.wellKnownPartyFromX500Name(patient)!!

        return try {
            val signedTx = rpcOps.startTrackedFlow(::PatientInitiatorFlow, name, dob, address, phoneNumber, patientAsParty).returnValue.getOrThrow()
            Response.status(CREATED).entity("Transaction id ${signedTx.id} committed to ledger.\n").build()

        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            Response.status(BAD_REQUEST).entity(ex.message!!).build()
        }
    }

    /**
     * Displays all Appointment states that exist in the Doc's vault.
     */
    @GET
    @Path("appointments")
    @Produces(MediaType.APPLICATION_JSON)
    fun getAppointments() = rpcOps.vaultQueryBy<AppointmentState>().states

    //POST Write new Appointment

    //POST Write new Prescription
    //GET Prescriptions (privacy)

    //POST Write new Insurer Bill State
    //GET Insurer bill record

    //POST Write new Government Bill Claim
    //GET Medical Card Record


}







