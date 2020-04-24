package com.custom.acl.web.demo.auth

import com.custom.acl.core.role.GrantedRole
import io.ktor.auth.Principal

/**
 * Interface for principal with roles
 *
 */
interface PrincipalWithRoles : Principal {
    fun grantedRoles(): Collection<GrantedRole>
}

data class CustomUser(val username: String, val roles: Collection<GrantedRole>): PrincipalWithRoles {
    override fun grantedRoles(): Collection<GrantedRole> = roles
}

