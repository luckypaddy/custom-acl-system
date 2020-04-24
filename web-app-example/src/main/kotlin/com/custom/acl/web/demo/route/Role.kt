package com.custom.acl.web.demo.route

import com.custom.acl.core.role.GrantedRole
import com.custom.acl.web.demo.auth.PrincipalWithRoles
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.auth.principal
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.RouteSelector
import io.ktor.routing.RouteSelectorEvaluation
import io.ktor.routing.RoutingResolveContext
import io.ktor.util.KtorExperimentalAPI

/**
 * Build route with check for particular roles, if there are no roles, access would be forbidden
 *
 * @param role role identity to be present in [PrincipalWithRoles]
 */
@KtorExperimentalAPI
fun Route.withRole(role: String, build: Route.() -> Unit): Route = withAnyRole(listOf(role), build)


@KtorExperimentalAPI
fun Route.withAnyRole(roles: Collection<String>, build: Route.() -> Unit): Route {
    val aclRoute = createChild(AclRouteSelector(roles))
    aclRoute.intercept(ApplicationCallPipeline.Call) {
        val principalWithRoles = call.principal<PrincipalWithRoles>()
        if (principalWithRoles != null
            && principalWithRoles.grantedRoles()
                .map(GrantedRole::getRoleIdentity)
                .any(roles::contains)
        ) return@intercept
        call.respond(HttpStatusCode.Forbidden.description("Insufficient roles"))
        finish()
    }
    aclRoute.build()
    return aclRoute
}

/**
 * An role check route node that is used to check if user have sufficient privileges to access further
 * @param roles identities to be applied to this route
 */
internal class AclRouteSelector(val roles: Collection<String>) : RouteSelector(RouteSelectorEvaluation.qualityConstant) {
    override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation {
        return RouteSelectorEvaluation.Constant
    }

    override fun toString(): String = "(check roles ${roles.joinToString()})"
}
