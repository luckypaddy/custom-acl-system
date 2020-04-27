package com.custom.acl.web.demo.auth

import com.custom.acl.core.role.GrantedRole
import com.custom.acl.core.role.Role
import io.ktor.auth.Principal

/**
 * Authorization session for user with roles. To update roles new session should be acquired.
 *
 * @property userId user identification
 * @property timestamp timestamp is epoch seconds when session was issued
 * @property roles user role identities for moment of getting session
 */
data class CustomUserSession(val userId: String, val timestamp: Long, val roles: Collection<String>)

/**
 * Transform [CustomUserSession] to [CustomUser] authentication principal
 *
 */
fun CustomUserSession.toUser() = CustomUser(userId, roles.map(::Role))