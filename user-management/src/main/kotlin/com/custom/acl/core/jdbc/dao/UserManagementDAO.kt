package com.custom.acl.core.jdbc.dao

import com.custom.acl.core.role.GrantedRole
import com.custom.acl.core.user.User
import java.io.Closeable

/**
 * DAO interface for operations with users
 *
 */
interface UserManagementDAO {

    /**
     * Assign set of roles to user
     *
     * @param username name of user
     * @param roles roles to be assigned
     * @return [User] with set roles or null if user is not found
     */
    fun assingRoles(username: String, vararg roles: GrantedRole): User?

    /**
     * Create new user in system
     *
     * @param user [User] representation of new user
     * @return [User] saved in db
     */
    fun createUser(user: User): User

    /**
     * Delete user with given name
     *
     * @param username name of user
     */
    fun deleteByUsername(username: String)

    /**
     * Find user by name (and password)
     *
     * @param username user name
     * @param passwordHash hash of user password
     * @return [User] found in database or null if not
     */
    fun find(username: String, passwordHash: String? = null): User?

    /**
     * Find all registered users
     *
     * @return [Collection] of [User] which are found in database
     */
    fun findAll(): Collection<User>

    /**
     * Save new password for user with validation of previous one
     *
     * @param user
     * @param newPasswordHash
     * @return
     */
    fun updatePassword(user: User, newPasswordHash: String): User?

}