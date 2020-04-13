package com.custom.acl.core.role

/**
 * Basic implementation of [GrantedRole]
 *
 * @property name identity of a role
 */
data class Role(private val name: String) : GrantedRole {
    override fun getRoleIdentity(): String = name
}