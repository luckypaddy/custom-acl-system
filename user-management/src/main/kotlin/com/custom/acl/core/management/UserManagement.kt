package com.custom.acl.core.management

import com.custom.acl.core.user.UserInfo

/**
 * Set of operations with users of system
 *
 */
interface UserManagement {

    fun createUser(user: UserInfo): UserInfo

    fun updatePassword(user: UserInfo, password: String): UserInfo

    fun deleteByUsername(userInformation: UserInfo)

    fun findByUsername(username: String): UserInfo


}