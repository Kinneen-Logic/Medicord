package com.template.webserver.controller

import com.template.api.SERVICE_NAMES1
import com.template.states.*
import com.template.webserver.NodeRPCConnection
import net.corda.core.contracts.StateAndRef
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.messaging.vaultQueryBy
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Define CorDapp-specific endpoints in a controller such as this.
 */
@RestController
@RequestMapping("/api/patient") // The paths for GET and POST requests are relative to this base path.
class PatientController(
        private val rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    init {}

    private val proxy = rpc.proxy
    private val myLegalName: CordaX500Name = proxy.nodeInfo().legalIdentities.first().name



    @GetMapping(value = "/me", produces = arrayOf("application/json"))
    private fun getMe() : ResponseEntity<CordaX500Name> {
        val me = proxy.nodeInfo().legalIdentities.first().name
        return ResponseEntity.ok().body(me)
    }

    @GetMapping(value = "/peers", produces = arrayOf("application/json"))
    private fun getPeers() : ResponseEntity<List<CordaX500Name>> {
        val peers = proxy.networkMapSnapshot()
        val filtered = peers
                .map { it.legalIdentities.first().name }
                //filter out myself, notary and eventual network map started by driver
                .filter { it.organisation !in (SERVICE_NAMES1 + myLegalName.organisation) }

        return ResponseEntity.ok().body(filtered)
    }


    @GetMapping(value = "/appointments", produces = arrayOf("application/json"))
    private fun getAppointments() : ResponseEntity<List<StateAndRef<AppointmentState>>> {
        val appStates = proxy.vaultQueryBy<AppointmentState>().states
        return ResponseEntity.ok().body(appStates)
    }

    @GetMapping(value = "/prescriptions", produces = arrayOf("application/json"))
    private fun getPrescriptions() : ResponseEntity<List<StateAndRef<PrescriptionState>>>{
        val prescriptionStates = proxy.vaultQueryBy<PrescriptionState>().states
        return ResponseEntity.ok().body(prescriptionStates)
    }

    @GetMapping(value = "/insurance-history", produces = arrayOf("application/json"))
    private fun getInsuranceHistorys() : ResponseEntity<List<StateAndRef<InsurerBillState>>>{
            //TODO
            return ResponseEntity.ok().body(null)
    }

    @GetMapping(value = "/medicalcard-history", produces = arrayOf("application/json"))
    private fun getMedicardCardHistory() : ResponseEntity<List<StateAndRef<GovernmentAgencyBillState>>>{
            //TODO
            return ResponseEntity.ok().body(null)
    }

    @GetMapping(value = "/get-patient", produces = arrayOf("application/json"))
    private fun getPatient() : ResponseEntity<List<StateAndRef<PatientInitialState>>>{
        //TODO
        return ResponseEntity.ok().body(null)
    }

}