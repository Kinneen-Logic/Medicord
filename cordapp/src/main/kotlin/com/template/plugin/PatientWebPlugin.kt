package com.template.plugin

import com.template.api.PatientAPI
import net.corda.webserver.services.WebServerPluginRegistry
import java.util.function.Function

// ***********
// * Plugins *
// ***********
class PatientWebPlugin : WebServerPluginRegistry {
    // A list of lambdas that create objects exposing web JAX-RS REST APIs.
    override val webApis = listOf(Function(::PatientAPI))
    //A list of directories in the resources directory that will be served by Jetty under /web.
    // This template's web frontend is accessible at /web/template.
    override val staticServeDirs: Map<String, String> = mapOf(
            // This will serve the templateWeb directory in resources to /web/medicord
            "patient" to javaClass.classLoader.getResource("templateWeb").toExternalForm()
    )
}