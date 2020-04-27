package com.custom.acl.web.demo.route

import com.custom.acl.core.jdbc.dao.UserManagementDAO
import com.custom.acl.web.demo.ChangePassword
import com.custom.acl.web.demo.auth.CustomUserSession
import com.custom.acl.web.demo.exception.ValidationException
import com.custom.acl.web.demo.jsonMessage
import com.custom.acl.web.demo.model.PasswordChangeRequest
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.put
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import kotlinx.serialization.json.json
import org.kodein.di.generic.instance
import org.kodein.di.ktor.kodein

/**
 * Change password route for logged in users
 *
 * @param hashFunction
 */
@KtorExperimentalLocationsAPI
fun Route.changePassword(hashFunction: (String) -> String) {

    /**
     * A PUT request to [ChangePassword] route, will try to change password for specified user
     * @throws ValidationException if validation is not passed
     */
    put<ChangePassword> {
        val session = call.sessions.get<CustomUserSession>()
            ?: return@put call.respond(HttpStatusCode.Unauthorized, json { "message" to "User session is missing" })

        val userDao by kodein().instance<UserManagementDAO>()

        val ( oldPassword, newPassword) = call.receive<PasswordChangeRequest>()

        if (newPassword.length < 6) throw ValidationException("Password should be at least 6 characters long")

        when (val user = userDao.find(session.userId, hashFunction(oldPassword))) {
            null -> throw ValidationException("Credentials are wrong")
            else -> userDao.updatePassword(user, hashFunction(newPassword))
        }
        call.respond(HttpStatusCode.OK, jsonMessage("Password was updated"))
    }
}
