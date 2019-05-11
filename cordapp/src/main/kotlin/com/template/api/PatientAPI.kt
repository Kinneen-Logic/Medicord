package com.template.api

import com.template.states.*
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.IdentityService
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

val SERVICE_NAMES1 = listOf("Notary", "Network Map Service")

// *****************
// * API Endpoints *
// *****************

// This API is accessible from /api/medicord/. All paths specified below are relative to it.
@Path("patient")
class PatientAPI(val rpcOps: CordaRPCOps) {
    // Accessible at /api/medico/medicoGetEndpoint.
    val myLegalName: CordaX500Name = rpcOps.nodeInfo().legalIdentities.first().name

    /**
     * Returns the node's name through API endpoint /api/medicord/me
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
                .filter { it.organisation !in (SERVICE_NAMES1 + myLegalName.organisation) })
    }

    /**
     * Displays all Patient's Appointment states that exist in the node's vault.
     * The comprises of the Patients complete Medical History
     */
    @GET
    @Path("appointments")
    @Produces(MediaType.APPLICATION_JSON)
    fun getAppointments() = rpcOps.vaultQueryBy<AppointmentState>().states

    /**
     * Displays all Patient's Prescription states that exist in the node's vault.
     * The comprises of the Patients complete prescription history
     */
    @GET
    @Path("prescriptions")
    @Produces(MediaType.APPLICATION_JSON)
    fun getPrescritpions() = rpcOps.vaultQueryBy<PrescriptionState>().states


    /**
     * Displays all Patient's Medical Insurance Claims and payments from the node's vault.
     * The comprises of the Patients complete private medical bill history
     */
    @GET
    @Path("insurer-history")
    @Produces(MediaType.APPLICATION_JSON)
    fun getInsurerBillHistory() = rpcOps.vaultQueryBy<InsurerBillState>().states


    /**
     * Displays the Patient's Medical Card history.
     * The comprises of the Patients complete public medical bill history
     */
    @GET
    @Path("medical-card-history")
    @Produces(MediaType.APPLICATION_JSON)
    fun getMedicalCardHistory() = rpcOps.vaultQueryBy<GovernmentAgencyBillState>().states


}







