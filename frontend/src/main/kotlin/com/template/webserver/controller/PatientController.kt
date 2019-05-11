package com.template.webserver.controller

import com.template.states.AppointmentState
import com.template.states.PrescriptionState
import com.template.webserver.NodeRPCConnection
import com.template.webserver.utilities.Utilities.Companion.appointmentToJSON
import com.template.webserver.utilities.Utilities.Companion.prescriptionToJSON
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.StateRef
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

    init {

    }
    private val proxy = rpc.proxy

    @GetMapping(value = "/me", produces = arrayOf("application/json"))
    private fun getMe() : ResponseEntity<Party> {
        val me = proxy.nodeInfo().legalIdentities.first()
        return ResponseEntity.ok().body(me)
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

}