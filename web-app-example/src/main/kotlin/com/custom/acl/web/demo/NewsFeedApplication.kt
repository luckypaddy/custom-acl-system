package com.custom.acl.web.demo

import com.custom.acl.core.jdbc.dao.RoleHierarchyDAO
import com.custom.acl.core.jdbc.dao.RoleHierarchyDatabase
import com.custom.acl.core.jdbc.dao.UserManagementDAO
import com.custom.acl.core.jdbc.dao.UserManagementDatabase
import com.custom.acl.core.jdbc.utils.DatabaseFactory
import com.custom.acl.web.demo.auth.CustomUserSession
import com.custom.acl.web.demo.auth.toUser
import com.custom.acl.web.demo.dao.NewsFeedDAO
import com.custom.acl.web.demo.dao.NewsFeedDatabase
import com.custom.acl.web.demo.exception.ValidationException
import com.custom.acl.web.demo.route.*
import com.custom.acl.web.demo.util.SecurityUtils
import com.custom.acl.web.demo.util.SecurityUtils.hash
import com.custom.acl.web.demo.util.checkAndInit
import com.custom.acl.web.demo.util.hikariConfig
import io.ktor.application.*
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.session
import io.ktor.features.*
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.*
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.request.httpMethod
import io.ktor.request.uri
import io.ktor.response.respond
import io.ktor.routing.routing
import io.ktor.serialization.json
import io.ktor.sessions.*
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.hex
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.json
import org.jetbrains.exposed.sql.Database
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import org.kodein.di.ktor.kodein
import java.time.Instant

/**
 * Main application with defined DI and session configuration
 *
 */
@KtorExperimentalLocationsAPI
@KtorExperimentalAPI
fun Application.main() = demoApp(
    di = {
        val adminConfig = environment.config.config("default.admin")
        val defaultAdminName = adminConfig.property("name").getString()
        val defaultAdminPassword = adminConfig.property("password").getString()
        val db = DatabaseFactory.connectToDb(hikariConfig())
        db.checkAndInit(defaultAdminName, hash(defaultAdminPassword))

        bind<Database>("mainDatabase") with singleton { db }

        bind<UserManagementDAO>() with singleton {
            UserManagementDatabase(instance("mainDatabase"))
        }
        bind<RoleHierarchyDAO>() with singleton {
            RoleHierarchyDatabase(instance("mainDatabase"))
        }
        bind<NewsFeedDAO>() with singleton {
            NewsFeedDatabase(instance("mainDatabase"))
        }


    },
    sessionConfig = {
        val sessionEncryptionConfig = environment.config.config("crypto.session")
        val sessionEncryptionKey = hex(sessionEncryptionConfig.property("encryptionKey").getString())
        val sessionSignKey = hex(sessionEncryptionConfig.property("signKey").getString())
        val sessionIV = hex(sessionEncryptionConfig.property("iv").getString())
        header<CustomUserSession>(name = "Custom-Auth-Key") {
            transform(
                SessionTransportTransformerEncrypt(
                    encryptionKey = sessionEncryptionKey,
                    signKey = sessionSignKey,
                    ivGenerator = { sessionIV })
            )
        }
    }
)

/**
 * Entry Point of the application. This function is referenced in the
 * resources/application.conf file inside the ktor.application.modules.
 * Externalized Kodein and Session configurations give flexibility for
 * different scenarios (e.g. override during tests)
 * F
 *
 */
@KtorExperimentalLocationsAPI
@KtorExperimentalAPI
fun Application.demoApp(di: Kodein.MainBuilder.() -> Unit = {}, sessionConfig: Sessions.Configuration.() -> Unit = {}) {

    val sessionDuration = environment.config.propertyOrNull("default.session.duration")?.getString()?.toLong() ?: 5

    // This adds automatically Date and Server headers to each response, and would allow you to configure
    // additional headers served to each response.
    install(DefaultHeaders)
    // This uses use the logger to log every call (request/response)
    install(CallLogging)
    // Location
    install(Locations)
    //CORS
    install(CORS) {
        // development only, for production can be externalized via configuration file
        anyHost()

        //for preflight requests
        method(HttpMethod.Options)

        method(HttpMethod.Get)
        method(HttpMethod.Post)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
    }
    //DI
    kodein(di)

    install(Sessions, sessionConfig)

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

    routing {
        static {
            resource("/","static/index.html")
            resources("static")
        }
        registration(SecurityUtils::hash)
        login(SecurityUtils::hash)
        newsFeed()
        authenticate("custom-session-auth") {

            withRole("USER") {
                changePassword(SecurityUtils::hash)
                postNew()
            }

            withRole("REVIEWER") {
                unpublishedFeeds()
                viewFeed()
                editFeed()
                publishFeed()
            }

            withRole("ADMIN") {
                assignRoles()
                deleteFeed()
            }
        }
    }
}

/**
 * Wrapper function to create json body with "message" field
 *
 * @param message
 */
fun jsonMessage(message: String) = json {
    "message" to message
}