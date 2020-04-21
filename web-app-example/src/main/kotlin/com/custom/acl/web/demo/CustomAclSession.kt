package com.custom.acl.web.demo

import com.custom.acl.core.role.GrantedRole
import com.custom.acl.core.role.Role
import io.ktor.auth.Principal
import java.util.*

interface PrincipalWithRoles: Principal {

    fun resolveRoles(): Collection<GrantedRole>
}
data class CustomAclSession(val id: String, val username: String, val roles: Collection<String>): PrincipalWithRoles {
    override fun resolveRoles(): Collection<GrantedRole> {
        return roles.map(::Role)
    }
}
