package com.template.plugin

import com.template.api.DoctorAPI
import net.corda.webserver.services.WebServerPluginRegistry
import java.util.function.Function

// ***********
// * Plugins *
// ***********


class DoctorWebPlugin : WebServerPluginRegistry {
    // A list of lambdas that create objects exposing web JAX-RS REST APIs.
    override val webApis = listOf(Function(::DoctorAPI))
    //A list of directories in the resources directory that will be served by Jetty under /web.
    // This template's web frontend is accessible at /web/template.
    override val staticServeDirs: Map<String, String> = mapOf(
            // This will serve the templateWeb directory in resources to /web/medicord
            "doctor" to javaClass.classLoader.getResource("templateWeb").toExternalForm()
    )
}
