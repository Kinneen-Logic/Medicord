package com.template.webserver.controller

import com.template.api.SERVICE_NAMES1
import com.template.flows.AppointmentFlow.AppointmentInitiatorFlow
import com.template.states.AppointmentState
import com.template.states.PatientInitialState
import com.template.states.PrescriptionState
import com.template.flows.InitialFlow.PatientInitiatorFlow
import com.template.flows.PrescriptionFlow
import com.template.flows.PrescriptionFlow.PrescriptionInitiatorFlow
import com.template.webserver.NodeRPCConnection
import net.corda.core.contracts.StateAndRef
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.startTrackedFlow
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.utilities.getOrThrow
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.TEXT_PLAIN_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest


/**
 * Define CorDapp-specific endpoints in a controller such as this.
 */
@RestController
@RequestMapping("/api/doctor") // The paths for GET and POST requests are relative to this base path.
class DoctorController(
        private val rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    init {
    }

    private val proxy = rpc.proxy
    private val myLegalName: CordaX500Name = proxy.nodeInfo().legalIdentities.first().name


    @GetMapping(value = "/me", produces = arrayOf("application/json"))
    private fun getMe(): ResponseEntity<CordaX500Name> {
        val me = proxy.nodeInfo().legalIdentities.first().name
        return ResponseEntity.ok().body(me)
    }


    @PostMapping(value = "/test", produces = arrayOf("application/json"))
    private fun test(): ResponseEntity<String> {
        val pp= "O=Patient, L=London, C=GB"
        val patientPartyX500Name = CordaX500Name.parse(pp)

        val patientParty = proxy.wellKnownPartyFromX500Name(patientPartyX500Name) ?: return ResponseEntity.badRequest().body("Party named $patientPartyX500Name cannot be found.\n")

        logger.info("Party : ${patientParty.name.organisation}")
        val signedTx = proxy.startTrackedFlow(::PatientInitiatorFlow, "d", "dfs", "sdf", "sdfs", patientParty).returnValue.getOrThrow()
        return ResponseEntity.ok().body("success")
    }


    @GetMapping(value = "/peers", produces = arrayOf("application/json"))
    private fun getPeers(): ResponseEntity<List<CordaX500Name>> {
        val peers = proxy.networkMapSnapshot()
        val filtered = peers
                .map { it.legalIdentities.first().name }
                //filter out myself, notary and eventual network map started by driver
                .filter { it.organisation !in (SERVICE_NAMES1 + myLegalName.organisation) }

        return ResponseEntity.ok().body(filtered)
    }

    @GetMapping(value = "/patients", produces = arrayOf("application/json"))
    private fun getPatients(): ResponseEntity<List<StateAndRef<PatientInitialState>>> {
        val patientState = proxy.vaultQueryBy<PatientInitialState>().states
        return ResponseEntity.ok().body(patientState)
    }


    @GetMapping(value = "/appointments", produces = arrayOf("application/json"))
    private fun getAppointments(): ResponseEntity<List<StateAndRef<AppointmentState>>> {
        val appStates = proxy.vaultQueryBy<AppointmentState>().states
        return ResponseEntity.ok().body(appStates)
    }

    @GetMapping(value = "/prescriptions", produces = arrayOf("application/json"))
    private fun getPrescriptions(): ResponseEntity<List<StateAndRef<PrescriptionState>>> {
        val prescriptionStates = proxy.vaultQueryBy<PrescriptionState>().states
        return ResponseEntity.ok().body(prescriptionStates)
    }

    @PostMapping(value = "/create-patient" , produces = arrayOf(org.springframework.http.MediaType.APPLICATION_JSON_VALUE))
    fun createPatient(request: HttpServletRequest): ResponseEntity<String> {
        val name = request.getParameter("name").toString()
        val dob = request.getParameter("dob").toString()
        val address = request.getParameter("address").toString()
        val phoneNumber = request.getParameter("phoneNumber").toString()
        val patientName = request.getParameter("patientName").toString()


        logger.info("Name: $name  dob: $dob")

        if(patientName == null){
            return ResponseEntity.badRequest().body("Query parameter 'patient name' must not be null.\n")
        }

        val patientPartyX500Name = CordaX500Name.parse(patientName)

        val patientParty = proxy.wellKnownPartyFromX500Name(patientPartyX500Name) ?: return ResponseEntity.badRequest().body("Party named $patientPartyX500Name cannot be found.\n")

        logger.info("Party : ${patientParty.name.organisation}")

        return try {
            logger.info("Patient Flow Start")
            val signedTx = proxy.startTrackedFlow(::PatientInitiatorFlow, name, dob, address, phoneNumber, patientParty).returnValue.getOrThrow()
            logger.info("Patient Init Flow Finish")
            ResponseEntity.ok().body("Transaction id ${signedTx.id} committed to ledger.\n")

        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }

    @PostMapping(value = "/create-appointment" , produces = arrayOf(org.springframework.http.MediaType.APPLICATION_JSON_VALUE))
    fun createAppointment(request: HttpServletRequest): ResponseEntity<String> {
        val symptoms = request.getParameter("symptoms").toString()
        val diagnosis = request.getParameter("diagnosis").toString()
        val date = request.getParameter("date").toString()
        val notes = request.getParameter("notes").toString()
        val prescription = request.getParameter("prescription").toString()
        val patientName = request.getParameter("patient").toString()

        val patientPartyX500Name = CordaX500Name.parse(patientName)
        val patientParty = proxy.wellKnownPartyFromX500Name(patientPartyX500Name) ?: return ResponseEntity.badRequest().body("Party named $patientPartyX500Name cannot be found.\n")


        return try {
            val signedTx = proxy.startTrackedFlow(::AppointmentInitiatorFlow, symptoms, diagnosis, date, notes, prescription, patientParty).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.CREATED).body("Transaction id ${signedTx.id} committed to ledger.\n")

        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }

    @PostMapping(value = "/create-prescription" , produces = arrayOf(org.springframework.http.MediaType.APPLICATION_JSON_VALUE))
    fun createPrescription(request: HttpServletRequest): ResponseEntity<String> {

        val details = request.getParameter("details").toString()
        val quantity = request.getParameter("quantity").toString()
        val medicine = request.getParameter("medicine").toString()
        val patientName = request.getParameter("patient").toString()
        val pharmacyName = request.getParameter("pharmacy").toString()

        val patientPartyX500Name = CordaX500Name.parse(patientName)
        val patientParty = proxy.wellKnownPartyFromX500Name(patientPartyX500Name) ?: return ResponseEntity.badRequest().body("Party named $patientPartyX500Name cannot be found.\n")

        val pharmacyPartyX500Name = CordaX500Name.parse(pharmacyName)
        val pharmacyParty = proxy.wellKnownPartyFromX500Name(pharmacyPartyX500Name) ?: return ResponseEntity.badRequest().body("Party named $pharmacyPartyX500Name cannot be found.\n")

        return try {
            val signedTx = proxy.startTrackedFlow(::PrescriptionInitiatorFlow, details, quantity, medicine, patientParty, pharmacyParty).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.CREATED).body("Transaction id ${signedTx.id} committed to ledger.\n")

        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }
}