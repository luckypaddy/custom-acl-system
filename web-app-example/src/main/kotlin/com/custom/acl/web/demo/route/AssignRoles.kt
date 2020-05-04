package com.custom.acl.web.demo.route

import com.custom.acl.core.jdbc.dao.UserManagementDAO
import com.custom.acl.core.role.Role
import com.custom.acl.web.demo.AssignRoles
import com.custom.acl.web.demo.jsonMessage
import com.custom.acl.web.demo.model.RolesAssignRequest
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.put
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import org.kodein.di.generic.instance
import org.kodein.di.ktor.kodein
import java.net.URI

/**
 * Route for role assignment operation
 *
 */
@KtorExperimentalLocationsAPI
fun Route.assignRoles() {
    /**
     * PUT method for processing roles assignment
     */
    put<AssignRoles> {
        val (userName, roles) = call.receive<RolesAssignRequest>()
        val userDao by kodein().instance<UserManagementDAO>()
        when (userDao.assingRoles(userName, *roles.map(::Role).toTypedArray())) {
            null -> call.respond(HttpStatusCode.NotFound, jsonMessage("User $userName not found"))
            else -> call.respond(HttpStatusCode.OK, jsonMessage("User's roles were successfully updated"))
        }
    }
}