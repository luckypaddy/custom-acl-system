package com.custom.acl.core

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

