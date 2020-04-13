package com.custom.acl.core.jdbc.dao

import com.custom.acl.core.role.GrantedRole
import com.custom.acl.core.user.UserInfo

interface UserDAO {

    fun assingRoles(user: UserInfo, vararg roles: GrantedRole)

    fun createUser(user: UserInfo): UserInfo

    fun deleteByUsername(username: String)

    fun findAll()

    fun findByUsername(username: String): UserInfo

    fun updatePassword(user: UserInfo, password: String): UserInfo

}