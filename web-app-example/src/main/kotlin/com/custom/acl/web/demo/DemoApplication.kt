package com.custom.acl.web.demo

import com.custom.acl.core.jdbc.dao.RoleHierarchyDAO
import com.custom.acl.core.jdbc.dao.RoleHierarchyDatabase
import com.custom.acl.core.jdbc.dao.UserManagementDAO
import com.custom.acl.core.jdbc.dao.UserManagementDatabase
import com.custom.acl.core.jdbc.utils.DatabaseFactory
import com.custom.acl.web.demo.exception.ValidationException
import com.custom.acl.web.demo.route.login
import com.custom.acl.web.demo.route.registration
import com.custom.acl.web.demo.route.withRole
import com.custom.acl.web.demo.auth.CustomUserSession
import com.custom.acl.web.demo.auth.toUser
import com.custom.acl.web.demo.util.SecurityUtils
import com.custom.acl.web.demo.util.SecurityUtils.hash
import com.custom.acl.web.demo.util.checkAndInit
import com.custom.acl.web.demo.util.hikariConfig
import io.ktor.application.*
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.session
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.request.httpMethod
import io.ktor.request.uri
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.serialization.json
import io.ktor.sessions.*
import io.ktor.util.InternalAPI
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.hex
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.json
import org.jetbrains.exposed.sql.Database
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import org.kodein.di.ktor.kodein
import java.time.Instant

/**
 * Entry Point of the application. This function is referenced in the
 * resources/application.conf file inside the ktor.application.modules.
 *
 */


@InternalAPI
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

    val sessionDuration = environment.config.propertyOrNull("default.session.duration")?.getString()?.toLong() ?: 5

    // This adds automatically Date and Server headers to each response, and would allow you to configure
    // additional headers served to each response.
    install(DefaultHeaders)
    // This uses use the logger to log every call (request/response)
    install(CallLogging)

    install(Locations)

    kodein {
        val db = DatabaseFactory.connectToDb(this@main.hikariConfig())
        db.checkAndInit(defaultAdminName, hash(defaultAdminPassword))
        bind<Database>("mainDatabase") with singleton { db }
        bind<UserManagementDAO>() with singleton {
            UserManagementDatabase(instance("mainDatabase"))
        }
        bind<RoleHierarchyDAO>() with singleton {
            RoleHierarchyDatabase(instance("mainDatabase"))
        }
    }

    install(Sessions) {
        header<CustomUserSession>(name = "Custom-Auth-Key") {
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
                HttpStatusCode.BadRequest,
                jsonMessage(cause.message ?: "Serialization/deserialization problem")
            )
        }
        exception<ValidationException> { cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                jsonMessage(cause.message ?: "Validation is not passed")
            )
        }
        exception<Exception> { cause ->
            application.log.error(
                "Error during processing request call: ${call.request.httpMethod} ${call.request.uri}",
                cause
            )
            application.environment.log
            call.respond(
                HttpStatusCode.InternalServerError,
                jsonMessage("Something went wrong, please try again later or contact administrator")
            )
        }
    }


    install(Authentication) {
        session<CustomUserSession>("custom-session-auth") {
            challenge { call.respond(HttpStatusCode.Unauthorized, jsonMessage("Session is expired or missing")) }
            validate { customUserSession ->
                if (Instant.now().epochSecond - customUserSession.timestamp > sessionDuration) {
                    sessions.clear<CustomUserSession>()
                    return@validate null
                }
                customUserSession.toUser()
            }
        }
    }

    install(ContentNegotiation) {
        json()
    }

// Registers routes
    routing {
        registration(SecurityUtils::hash)
        login(SecurityUtils::hash)
        authenticate("custom-session-auth") {
            withRole("REVIEWER") {
                withRole("GODMODE") {
                    get("/") {
                        call.respond(HttpStatusCode.ExpectationFailed, jsonMessage("You should have no access here"))
                    }
                }
                get("/demo") {
                    call.respond(HttpStatusCode.OK, jsonMessage("DEMO has started for reviewer"))
                }
            }
        }
    }
}

fun jsonMessage(message: String) = json {
    "message" to message
}