package com.template.webserver.utilities

import com.template.states.AppointmentState
import com.template.states.PatientInitialState
import com.template.states.PrescriptionState
import net.corda.core.utilities.loggerFor
import org.slf4j.Logger


class Utilities{
    companion object {

        private val logger: Logger = loggerFor<Utilities>()

    fun appointmentToJSON(appointment : AppointmentState) : Map<String,String>{
        return kotlin.collections.mapOf(
                "doctor" to appointment.doctor.name.toString(),
                "patient" to appointment.patient.name.toString(),
                "date" to appointment.date.toString(),
                "symptoms" to appointment.symptoms,
                "diagnosis" to appointment.diagnosis,
                "prescription" to appointment.prescription,
                "notes" to appointment.notes
        )
    }

    fun prescriptionToJSON(prescription : PrescriptionState) : Map<String,String>{
        return kotlin.collections.mapOf(
                "medicine" to prescription.medicine,
                "patient" to prescription.patient.name.toString(),
                "doctor" to prescription.doctor.toString(),
                "quantity" to prescription.quantity,
                "pharmacy" to prescription.pharmacy.toString()
        )
    }

    fun patientToJSON(appointment : PatientInitialState) : Map<String,String>{
        return kotlin.collections.mapOf(

         // TODO

        )
    }
  }
}
