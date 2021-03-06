package com.custom.acl.web.demo.route

import com.custom.acl.core.jdbc.dao.RoleHierarchyDAO
import com.custom.acl.core.jdbc.dao.UserManagementDAO
import com.custom.acl.core.role.GrantedRole
import com.custom.acl.web.demo.Login
import com.custom.acl.web.demo.auth.CustomUserSession
import com.custom.acl.web.demo.jsonMessage
import com.custom.acl.web.demo.model.UserCredentials
import com.custom.acl.web.demo.util.userNameValid
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.kodein.di.generic.instance
import org.kodein.di.ktor.kodein
import java.time.Instant

/**
 * Registers the [Login] route '/login'.
 */
@KtorExperimentalLocationsAPI
fun Route.login(hash: (String) -> String) {

    /**
     * A POST request to the [Login] actually processes the [UserCredentials] to validate them,
     * if valid it sets the session.
     * If login is successful, then in response there would be header Custom-Auth-Key,
     * which contains encrypted session information
     *
     */
    post<Login> {
        val userDao by kodein().instance<UserManagementDAO>()
        val rolesDao by kodein().instance<RoleHierarchyDAO>()
        val userCredentials = call.receive<UserCredentials>()

        val user = withContext(Dispatchers.Default) {
            val login = when {
                userCredentials.name.length < 4 -> null
                userCredentials.password.length < 6 -> null
                !userNameValid(userCredentials.name) -> null
                else -> userDao.find(userCredentials.name, hash(userCredentials.password))
            }

            if (login != null) {
                call.sessions.set(
                    CustomUserSession(
                        login.username, Instant.now().epochSecond,
                        rolesDao.effectiveRoles(login.roles).map(GrantedRole::getRoleIdentity)
                    )
                )
            }
            login
        }
        when {
            user != null -> call.respond(HttpStatusCode.OK)
            else -> call.respond(HttpStatusCode.BadRequest, jsonMessage("Invalid username or password"))
        }

    }

}