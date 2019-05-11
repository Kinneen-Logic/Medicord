package com.template.api

import com.template.states.PrescriptionState
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.IdentityService
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

val SERVICE_NAMES2 = listOf("Notary", "Network Map Service")

// *****************
// * Pharmacy API Endpoints *
// *****************

// This API is accessible from /api/medicord/. All paths specified below are relative to it.
@Path("pharmacy")
class PharmacyAPI(val rpcOps: CordaRPCOps) {
    val myLegalName: CordaX500Name = rpcOps.nodeInfo().legalIdentities.first().name

    /**
     * Returns the node's name through API endpoint /api/medicord/pharmacy/me
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
                .filter { it.organisation !in (SERVICE_NAMES2 + myLegalName.organisation) })
    }


    /**
     * Displays all Appointment states that exist in the node's vault.
     */
    @GET
    @Path("prescriptions")
    @Produces(MediaType.APPLICATION_JSON)
    fun getPrescriptions() = rpcOps.vaultQueryBy<PrescriptionState>().states

    //POST Write new Appointment

    //POST Write new Prescription
    //GET Prescriptions (privacy)

    //POST Write new Insurer Bill State
    //GET Insurer bill record

    //POST Write new Government Bill Claim
    //GET Medical Card Record


}







