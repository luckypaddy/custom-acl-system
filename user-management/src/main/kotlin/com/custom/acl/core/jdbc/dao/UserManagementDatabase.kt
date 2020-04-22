package com.custom.acl.core.jdbc.dao

import com.custom.acl.core.role.GrantedRole
import com.custom.acl.core.user.User
import com.zaxxer.hikari.HikariDataSource
import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * IMplementation of user management which is persisted in database
 *
 * @property db
 */
class UserManagementDatabase(
    private val db: Database = Database.connect(HikariDataSource())
) : UserManagementDAO {
    override fun assingRoles(username: String, vararg roles: GrantedRole) = transaction(db) {
        val foundUser = PersistedUser
            .find { PersistedUsers.username eq username }
            .singleOrNull()
        if (foundUser == null) {
            logger.warn { "There is no user with name: $username" }
            return@transaction null
        }
        val foundRoles = findUserRoles(roles.toList())
        foundUser.roles = foundRoles
        return@transaction foundUser.toUser()
    }

    private fun findUserRoles(roles: Collection<GrantedRole>): SizedIterable<HierarchicalRole> {
        val identites = roles.map(GrantedRole::getRoleIdentity)
        val result = HierarchicalRole.find { HierarchicalRoles.identity inList identites }
        if (result.empty()) {
            logger.error { "There are no roles with given identities: $identites" }
            throw IllegalArgumentException("There are no roles with given identities: $identites")
        }
        return result
    }

    override fun createUser(user: User): User = transaction(db) {
        val foundRoles = findUserRoles(user.roles)
        logger.info { "Creating new user with name: ${user.username}" }
        val persistedUser = PersistedUser.new(UUID.randomUUID()) {
            username = user.username
            passwordHash = user.passwordHash
            roles = foundRoles
        }
        return@transaction persistedUser.toUser()
    }

    override fun deleteByUsername(username: String) = transaction(db) {
        logger.info { "Deleting user with name: $username" }
        PersistedUsers.deleteWhere {
            PersistedUsers.username eq username
        }
        return@transaction
    }

    override fun find(username: String, passwordHash: String?): User? = transaction(db) {
        logger.info { "Searching  user info for $username" }
        PersistedUser
            .find {
                when (passwordHash) {
                    null -> PersistedUsers.username eq username
                    else -> PersistedUsers.username eq username and
                            (PersistedUsers.passwordHash eq passwordHash)
                }
            }
            .singleOrNull()
            ?.toUser()
    }

    override fun findAll(): Collection<User> = transaction(db) {
        PersistedUser.all().map(PersistedUser::toUser)
    }

    override fun updatePassword(user: User, newPasswordHash: String): User? = transaction(db) {
        logger.info { "Updating password for user: ${user.username}" }
        val persistedUser = PersistedUser
            .find {
                PersistedUsers.username eq user.username and
                        (PersistedUsers.passwordHash eq user.passwordHash)
            }
            .singleOrNull()
        if (persistedUser == null) {
            logger.warn { "There is no user with name: ${user.username}" }
            return@transaction null
        }
        persistedUser.passwordHash = newPasswordHash
        return@transaction persistedUser.toUser()
    }

    override fun close() {
        db.connector().close()
    }
}