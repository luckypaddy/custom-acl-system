package com.custom.acl.web.demo

import com.custom.acl.core.jdbc.dao.RoleHierarchyDAO
import com.custom.acl.core.jdbc.dao.RoleHierarchyDatabase
import com.custom.acl.core.jdbc.dao.UserManagementDAO
import com.custom.acl.core.jdbc.dao.UserManagementDatabase
import com.custom.acl.core.jdbc.utils.DatabaseFactory
import com.custom.acl.core.role.GrantedRole
import com.custom.acl.core.role.Role
import com.custom.acl.core.user.User
import com.zaxxer.hikari.HikariConfig
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
import io.ktor.http.Parameters
import io.ktor.locations.*
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.serialization.json
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.sessions.*
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.hex
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import org.jetbrains.exposed.sql.Database
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import org.kodein.di.ktor.kodein
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Entry Point of the application. This function is referenced in the
 * resources/application.conf file inside the ktor.application.modules.
 *
 */
@KtorExperimentalLocationsAPI
@KtorExperimentalAPI
fun Application.main() {
    // This adds automatically Date and Server headers to each response, and would allow you to configure
    // additional headers served to each response.
    install(DefaultHeaders)
    // This uses use the logger to log every call (request/response)
    install(CallLogging)

    install(Locations)

    install(Sessions) {
        header<CustomAclSession>(name = "Custom-Auth-Key", storage = SessionStorageMemory()) {
            transform(SessionTransportTransformerEncrypt(hashKey1, hashKey2, { hashKey3 }))
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
        val db = DatabaseFactory.connectToDb(HikariConfig())
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
        login(::hash)
        authenticate("custom-session-auth") {
            get("/") {
                call.respond(HttpStatusCode.OK, "DEMO has started")
            }
        }
    }
}

@Location("/login")
data class Login(val userName: String = "", val password: String = "")

@Location("/logout")
class Logout()

@KtorExperimentalAPI
val hashKey = hex("2819b57a376945c1968f45237589") //TODO read from env variables
val hashKey1 = hex("eff63fab3be7b43571fa1092db1eafa6") //TODO read from env variables
val hashKey2 = hex("24e63e09e1c8ea3fde9e63974402e13e") //TODO read from env variables
val hashKey3 = hex("02bb13afe8dc7e750f87114a53e286d3") //TODO read from env variables

/**
 * Pattern to validate an `userId`
 */
private val userNamePattern = "[a-zA-Z0-9_\\.]+".toRegex()



@Serializable
data class UserCredentials(val name: String, val password: String)

/**
 * Validates that an [userId] (that is also the user name) is a valid identifier.
 * Here we could add additional checks like the length of the user.
 * Or other things like a bad word filter.
 */
internal fun userNameValid(userId: String) = userId.matches(userNamePattern)

fun hash(password: String): String {
    val hmac = Mac.getInstance("HmacSHA1")
    val hmacKey = SecretKeySpec(hashKey, "HmacSHA1")
    hmac.init(hmacKey)
    return hex(hmac.doFinal(password.toByteArray(Charsets.UTF_8)))
}

/**
 * Registers the [Login] and [Logout] routes '/login' and '/logout'.
 */
@KtorExperimentalLocationsAPI
fun Route.login(hash: (String) -> String) {

    /**
     * A POST request to the [Login] actually processes the [Parameters] to validate them, if valid it sets the session.
     * It will redirect either to the [Login] page with an error in the case of error,
     * or to the [UserPage] if the login was successful.
     */
    post<Login> {
        val userDao by kodein().instance<UserManagementDAO>()
        val userCredentials = call.receive<UserCredentials>()

        val login = when {
            userCredentials.name.length < 4 -> null
            userCredentials.password.length < 6 -> null
            !userNameValid(userCredentials.name) -> null
            else -> userDao.find(userCredentials.name, hash(userCredentials.password))
        }

        if (login == null) {
            call.respond(HttpStatusCode.BadRequest.description("Invalid username or password"))
        } else {
            call.sessions.set(
                CustomAclSession(
                    UUID.randomUUID().toString(),
                    login.username,
                    login.roles.map(GrantedRole::getRoleIdentity)
                )
            )
            call.respond(HttpStatusCode.OK)
        }
    }

    /**
     * A GET request to the [Logout] page, removes the session and redirects to the [Index] page.
     */
    get<Logout> {
        call.sessions.clear<CustomAclSession>()
        call.respond(HttpStatusCode.NoContent)
    }
}