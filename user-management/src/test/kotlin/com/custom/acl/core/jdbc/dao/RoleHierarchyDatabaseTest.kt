package com.custom.acl.core.jdbc.dao

import com.custom.acl.core.jdbc.utils.DatabaseFactory
import com.custom.acl.core.role.Role
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class RoleHierarchyDatabaseTest {

    private val db: Database = DatabaseFactory.connectToDb(TestConfig.testConfiguration())
    private val roleDao = RoleHierarchyDatabase(db)
    init {
        TestConfig.initDB(db)


    }
    companion object {
        val USER_ROLE = Role("USER")
        val REVIEWER_ROLE = Role( "REVIEWER")
        val ADMIN_ROLE = Role( "ADMIN")
    }

    @AfterEach
    fun `reset table`() {
        transaction(db) {
            HierarchicalRoles.deleteAll()
        }
    }

    @Test
    fun `create role in empty hierarchy`() {
        roleDao.create(USER_ROLE, null)
        transaction(db) {
            val all = HierarchicalRole.all()
            val roleInDB = all.first()
            assert(all.count() == 1L)
            assert(roleInDB.identity == USER_ROLE.getRoleIdentity())
            assert(roleInDB.parentId == null)
        }
    }

    @Test
    fun `create roles with wrong hierarchy`() {
        assertThrows<IllegalArgumentException> { roleDao.create(REVIEWER_ROLE, USER_ROLE) }
    }

    @Test
    fun `create full hierarchy of roles`() {
        roleDao.create(USER_ROLE, null)
        roleDao.create(REVIEWER_ROLE, USER_ROLE)
        roleDao.create(ADMIN_ROLE, REVIEWER_ROLE)
        transaction {
            val all = HierarchicalRole.all().orderBy(HierarchicalRoles.left to SortOrder.ASC)
            assert(all.count() == 3L)
            val userRoleInDB = all.elementAt(0)
            val reviewerRoleInDB = all.elementAt(1)
            val adminRoleInDB = all.elementAt(2)
            assert(userRoleInDB.identity == USER_ROLE.getRoleIdentity())
            assert(userRoleInDB.parentId == null)
            assert(reviewerRoleInDB.identity == REVIEWER_ROLE.getRoleIdentity())
            assert(reviewerRoleInDB.parentId == userRoleInDB.id.value)
            assert(adminRoleInDB.identity == ADMIN_ROLE.getRoleIdentity())
            assert(adminRoleInDB.parentId == reviewerRoleInDB.id.value)

        }
    }
}