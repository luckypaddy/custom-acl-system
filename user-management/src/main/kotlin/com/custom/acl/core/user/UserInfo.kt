package com.custom.acl.core.user

import com.custom.acl.core.role.GrantedRole

/**
 * Provides minimal needed information of actor in system
 *
 */
interface UserInfo {
    /**
     * Return name of user
     *
     * @return user name
     */
    fun getUsername(): String

    /**
     * Return user's password representation
     *
     * @return
     */
    fun getPassword(): String

    /**
     * Return set roles granted to user
     *
     * @return
     */
    fun getRoles(): Collection<GrantedRole>
}