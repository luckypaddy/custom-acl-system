package com.custom.acl.web.demo

import com.custom.acl.core.jdbc.dao.*
import com.custom.acl.core.jdbc.utils.DatabaseFactory
import com.custom.acl.web.demo.util.SecurityUtils
import com.custom.acl.web.demo.util.SecurityUtils.hash
import com.custom.acl.web.demo.util.checkAndInit
import com.custom.acl.web.demo.util.hikariConfig
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.session
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.locations.*
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.serialization.json
import io.ktor.sessions.*
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.hex
import kotlinx.serialization.SerializationException
import mu.KotlinLogging
import org.jetbrains.exposed.sql.*
import org.kodein.di.generic.bind
import org.kodein.di.generic.singleton
import org.kodein.di.ktor.kodein

private val logger = KotlinLogging.logger {}

/**
 * Entry Point of the application. This function is referenced in the
 * resources/application.conf file inside the ktor.application.modules.
 *
 */


@KtorExperimentalLocationsAPI
@KtorExperimentalAPI
fun Application.main() {

    val adminConfig = environment.config.config("default.admin")
    val defaultAdminName = adminConfig.property("name").getString()
    val defaultAdminPassword = adminConfig.property("password").getString()

    val sessionEncryptionConfig = environment.config.config("crypto.session")
    val sessionEncryptionKey = hex(sessionEncryptionConfig.property("encryptionKey").getString())
    val sessionSignKey = hex(sessionEncryptionConfig.property("signKey").getString())
    val sessionIV = hex(sessionEncryptionConfig.property("iv").getString())

    // This adds automatically Date and Server headers to each response, and would allow you to configure
    // additional headers served to each response.
    install(DefaultHeaders)
    // This uses use the logger to log every call (request/response)
    install(CallLogging)

    install(Locations)

    install(Sessions) {
        header<CustomAclSession>(name = "Custom-Auth-Key", storage = SessionStorageMemory()) {
            transform(
                SessionTransportTransformerEncrypt(
                    encryptionKey = sessionEncryptionKey,
                    signKey = sessionSignKey,
                    ivGenerator = { sessionIV })
            )
        }
    }

    install(StatusPages) {
        exception<SerializationException> { cause ->
            call.respond(
                HttpStatusCode.BadRequest.description(
                    cause.message ?: "Serialization/deserialization problem"
                )
            )
        }
    }

    kodein {
        val db = DatabaseFactory.connectToDb(hikariConfig(this@main))
        checkAndInit(db, defaultAdminName, hash(defaultAdminPassword))
        bind<Database>("db") with singleton { db }
        bind<UserManagementDAO>() with singleton {
            UserManagementDatabase(db)
        }
        bind<RoleHierarchyDAO>() with singleton {
            RoleHierarchyDatabase(db)
        }
    }

    install(Authentication) {
        session<CustomAclSession>("custom-session-auth") {
            challenge { call.respond(HttpStatusCode.Unauthorized) }
            validate { customAclSession -> customAclSession }
        }
    }

    install(ContentNegotiation) {
        json()
    }

    // Registers routes
    routing {
        login(SecurityUtils::hash)
        authenticate("custom-session-auth") {
            get("/") {
                call.respond(HttpStatusCode.OK, "DEMO has started")
            }
        }
    }
}


