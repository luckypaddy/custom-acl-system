package com.custom.acl.core.jdbc.dao

import com.custom.acl.core.role.Role
import com.custom.acl.core.user.User
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.insertAndGetId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class UserManagementDatabaseTest : DatabaseTestsBase() {
    private val userDao = UserManagementDatabase(database)

    companion object {
        val USER_ROLE = Role("USER")
        val REVIEWER_ROLE = Role("REVIEWER")
        val ADMIN_ROLE = Role("ADMIN")
        const val USERNAME = "username"
        const val PASSWORD_HASH = "passwordHash"
    }

    private fun withUserAndRoles(statement: Transaction.() -> Unit) {
        withTables(HierarchicalRoles, PersistedUsers, UserRoles) {
            val user = HierarchicalRoles.insertAndGetId {
                it[parentId] = null
                it[identity] = "USER"
                it[left] = 1
                it[right] = 6
            }
            val reviewer = HierarchicalRoles.insertAndGetId {
                it[parentId] = user.value
                it[identity] = "REVIEWER"
                it[left] = 2
                it[right] = 5
            }
            HierarchicalRoles.insertAndGetId {
                it[parentId] = reviewer.value
                it[identity] = "ADMIN"
                it[left] = 3
                it[right] = 4
            }
            statement()
        }
    }

    @Test
    fun `create user`() {
        withUserAndRoles {
            val createdUser = userDao.createUser(User(USERNAME, PASSWORD_HASH, listOf(REVIEWER_ROLE)))

            //external behavior assertion
            assert(createdUser.username == USERNAME) { "Username should be the same" }
            assert(createdUser.passwordHash == PASSWORD_HASH) { "Password should be the same" }
            assert(createdUser.roles.size == 1) { "User should have only 1 role " }
            assert(createdUser.roles.contains(REVIEWER_ROLE)) { "User should have REVIEWER role" }

            //internal behavior assertion
            assert(PersistedUser.all().count() == 1L) { "Only one user should be present" }

            val persistedUser = PersistedUser.find { PersistedUsers.username eq USERNAME }.single()
            assert(persistedUser.username == USERNAME) { "Username should be the same in DB" }
            assert(persistedUser.passwordHash == PASSWORD_HASH) { "Password hash should be in DB" }
            assert(persistedUser.roles.count() == 1L) { "Only one role should be mapped in DB" }
            assert(persistedUser.roles.first().identity == REVIEWER_ROLE.getRoleIdentity()) { "REVIEWER role should be mapped to user in DB" }
        }
    }

    @Test
    fun `create user with non-existing role`() {
        withUserAndRoles {
            assertThrows<IllegalArgumentException>("Should throw IllegalArgumentException") {
                userDao.createUser(User(USERNAME, PASSWORD_HASH, listOf(Role("NOT_EXIST"))))
            }
        }
    }

    @Test
    fun `create user same user twice`() {
        withUserAndRoles {
            assertThrows<ExposedSQLException>("Should throw ExposedSQLException") {
                userDao.createUser(User(USERNAME, PASSWORD_HASH, listOf(USER_ROLE)))
                userDao.createUser(User(USERNAME, PASSWORD_HASH, listOf(USER_ROLE)))
            }
        }
    }

    @Test
    fun `assign new roles to user`() {
        withUserAndRoles {
            userDao.createUser(User(USERNAME, PASSWORD_HASH, listOf(USER_ROLE)))
            userDao.assingRoles(USERNAME, ADMIN_ROLE, REVIEWER_ROLE)
            val persistedUser = PersistedUser.find { PersistedUsers.username eq USERNAME }.single()
            assert(persistedUser.username == USERNAME) { "Username should be the same in DB" }
            assert(persistedUser.passwordHash == PASSWORD_HASH) { "Password hash should be in DB" }
            assert(persistedUser.roles.count() == 2L) { "2 roles should be assigned to user in db" }
            assert(
                persistedUser.roles.map(HierarchicalRole::identity).containsAll(
                    listOf(ADMIN_ROLE.getRoleIdentity(), REVIEWER_ROLE.getRoleIdentity())
                )
            ) { "User should have ADMIN and REVIEWER role" }
        }
    }

    @Test
    fun `assign non-existing role to user`() {
        withUserAndRoles {
            userDao.createUser(User(USERNAME, PASSWORD_HASH, listOf(USER_ROLE)))
            assertThrows<IllegalArgumentException> { userDao.assingRoles(USERNAME, Role("NOT_FOUND")) }
        }
    }

    @Test
    fun `assign role to non-existing user`() {
        withUserAndRoles {
            val user = userDao.assingRoles("not_found", ADMIN_ROLE)
            assert(user == null) { "User should be null" }
        }
    }

    @Test
    fun `update password for user`() {
        withUserAndRoles {
            val user = userDao.createUser(User(USERNAME, PASSWORD_HASH, listOf(USER_ROLE)))
            val newPasswordHash = "newPasswordHash"
            userDao.updatePassword(user, newPasswordHash)
            val persistedUser = PersistedUser.find { PersistedUsers.username eq USERNAME }.single()
            assert(persistedUser.username == USERNAME) { "Username should be the same in DB" }
            assert(persistedUser.passwordHash == newPasswordHash) { "Password hash should be updated in DB" }
        }
    }

    @Test
    fun `update password for user with wronf old password`() {
        withUserAndRoles {
            val user = userDao.createUser(User(USERNAME, PASSWORD_HASH, listOf(USER_ROLE)))
            val newPasswordHash = "newPasswordHash"
            val updatedUser = userDao.updatePassword(user.copy(passwordHash = newPasswordHash), newPasswordHash)
            assert(updatedUser == null) { "User should be null" }

        }
    }

    @Test
    fun `update password for non-existing user`() {
        withUserAndRoles {
            val user = User(USERNAME, PASSWORD_HASH, listOf(USER_ROLE))
            val newPasswordHash = "newPasswordHash"
            assert(userDao.updatePassword(user, newPasswordHash) == null) { "User should be null" }
        }
    }

    @Test
    fun `find all users in empty table`() {
        withUserAndRoles {
            assert(userDao.findAll().isEmpty()) { "User list should be empty" }
        }
    }

    @Test
    fun `find all users in filled table`() {
        withUserAndRoles {
            val usernames = List(3) { index -> "$USERNAME-${index + 1}" }
            usernames.forEach { username ->
                userDao.createUser(User(username, PASSWORD_HASH, listOf(USER_ROLE)))
            }
            val all = userDao.findAll()
            assert(all.size == 3) { "User list should have 3 elements" }
            assert(all.map(User::username).containsAll(usernames)) { "All username should be found" }

        }
    }

    @Test
    fun `find user by username`() {
        withUserAndRoles {
            val usernames = List(3) { index -> "$USERNAME-${index + 1}" }
            usernames.forEach { username ->
                userDao.createUser(User(username, PASSWORD_HASH, listOf(USER_ROLE)))
            }
            val user = userDao.find(usernames[1])
            assert(user?.username == usernames[1]) { "User name should match" }
        }
    }

    @Test
    fun `find user by username & password`() {
        withUserAndRoles {
            val usernames = List(3) { index -> "$USERNAME-${index + 1}" }
            usernames.forEach { username ->
                userDao.createUser(User(username, PASSWORD_HASH, listOf(USER_ROLE)))
            }
            val user = userDao.find(usernames[2])
            assert(user?.username == usernames[2]) { "User name should match" }
            assert(user?.passwordHash == PASSWORD_HASH) { "Passwords should match" }
        }
    }

    @Test
    fun `find non-existing user by username`() {
        withUserAndRoles {
            val usernames = List(3) { index -> "$USERNAME-${index + 1}" }
            usernames.forEach { username ->
                userDao.createUser(User(username, PASSWORD_HASH, listOf(USER_ROLE)))
            }
            val user = userDao.find("$USERNAME-0")
            assert(user == null) { "User should not be found" }
        }
    }

    @Test
    fun `delete user by username`() {
        withUserAndRoles {
            val usernames = List(3) { index -> "$USERNAME-${index + 1}" }
            usernames.forEach { username ->
                userDao.createUser(User(username, PASSWORD_HASH, listOf(USER_ROLE)))
            }
            userDao.deleteByUsername(usernames[0])
            assert(PersistedUser.all().count() == 2L)
            assert(
                PersistedUser.all().map(PersistedUser::username).none { it == usernames[0] }
            ) { "Deleted user should not be found" }
        }

    }

    @Test
    fun `delete non-existing user by username`() {
        withUserAndRoles {
            val usernames = List(3) { index -> "$USERNAME-${index + 1}" }
            usernames.forEach { username ->
                userDao.createUser(User(username, PASSWORD_HASH, listOf(USER_ROLE)))
            }
            userDao.deleteByUsername("$USERNAME-0")
            assert(PersistedUser.all().count() == 3L)
            assert(
                PersistedUser.all().map(PersistedUser::username).containsAll(usernames)
            ) { "Deleted user should not be found" }
        }

    }

}