package com.custom.acl.core.role

import com.custom.acl.core.GrantedRole

/**
 * Basic implementation of [GrantedRole]
 *
 * @property name identity of a role
 */
class Role(private val name: String) : GrantedRole {
    override fun getRoleIdentity(): String = name
}