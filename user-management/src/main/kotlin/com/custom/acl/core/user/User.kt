package com.custom.acl.core.user

import com.custom.acl.core.role.GrantedRole


/**
 * Basic implementation of [UserInfo]
 *
 * @property username name
 * @property password password
 * @property roles granted roles
 */
data class User(
    private val username: String,
    private val password: String,
    private val roles: Collection<GrantedRole>
): UserInfo {

    override fun getUsername(): String = username

    override fun getPassword(): String = password

    override fun getRoles(): Collection<GrantedRole> = roles

}
