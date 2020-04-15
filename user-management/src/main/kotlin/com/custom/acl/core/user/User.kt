package com.custom.acl.core.user

import com.custom.acl.core.role.GrantedRole


/**
 * Basic implementation of user
 *
 * @property username name
 * @property passwordHash password
 * @property roles granted roles
 */
data class User(
    val username: String,
    val passwordHash: String,
    val roles: Collection<GrantedRole>
)