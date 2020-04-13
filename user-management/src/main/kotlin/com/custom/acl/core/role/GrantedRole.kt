package com.custom.acl.core.role

/**
 * Represent role granted to user in system
 *
 */
interface GrantedRole {
    /**
     * Return identity of granted role
     *
     * @return
     */
    fun getRoleIdentity(): String
}

