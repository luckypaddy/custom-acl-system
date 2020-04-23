package com.custom.acl.web.demo

import com.custom.acl.core.jdbc.dao.RoleHierarchyDAO
import com.custom.acl.core.jdbc.dao.UserManagementDAO
import com.custom.acl.core.user.User
import com.custom.acl.web.demo.exception.ValidationException
import com.custom.acl.web.demo.model.PasswordChangeRequest
import com.custom.acl.web.demo.model.RegistrationRequest
import com.custom.acl.web.demo.util.userNameValid
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.post
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
 * Register routes for user registration in the [Register] route (/register)
 */
@KtorExperimentalLocationsAPI
fun Route.registration(hashFunction: (String) -> String) {

    /**
     * A POST request to the [Register] route, will try to create a new user.
     *
     * - If the user is already logged, send message that user is already registered.
     * - On success, it generates a new [User]. But instead of storing the password plain text,
     *   it stores a hash of the password.
     * @throws ValidationException if validation of user information is not passed
     */
    post<Register> {
        val userDao by kodein().instance<UserManagementDAO>()
        val roleDAO by kodein().instance<RoleHierarchyDAO>()

        val session = call.sessions.get<CustomAclSession>()
        if (session != null)
            return@post call.respond(HttpStatusCode.NoContent, json { "message" to "User is already registered" })


        val (userName, password) = call.receive<RegistrationRequest>()

        when {
            password.length < 6 -> throw ValidationException("Password should be at least 6 characters long")
            userName.length < 4 -> throw ValidationException("Login should be at least 4 characters long")
            !userNameValid(userName) -> throw ValidationException("Login should be consists of digits, letters, dots or underscores")
            userDao.find(userName) != null -> throw ValidationException("User with the following login is already registered")
            else -> {
                val basicRoles = roleDAO.hierarchy()
                    .filter { (_, value) -> value == null }
                    .keys

                userDao.createUser(
                    User(
                        username = userName,
                        passwordHash = hashFunction(password),
                        roles = basicRoles
                    )
                )
                call.respond(
                    HttpStatusCode.Created,
                    jsonMessage("User was successfully created. Now you can login.")
                )
            }
        }
    }

    /**
     * A PUT request to [ChangePassword] route, will try to change password for specified user
     * @throws ValidationException if validation is not passed
     */
    put<ChangePassword> {
        val userDao by kodein().instance<UserManagementDAO>()

        val (userName, oldPassword, newPassword) = call.receive<PasswordChangeRequest>()

        if (newPassword.length < 6) throw ValidationException("Password should be at least 6 characters long")

        when (val user = userDao.find(userName, hashFunction(oldPassword))) {
            null -> throw ValidationException("Credentials are wrong")
            else -> userDao.updatePassword(user, hashFunction(newPassword))
        }
        call.respond(HttpStatusCode.OK, jsonMessage("Password was updated"))
    }
}
