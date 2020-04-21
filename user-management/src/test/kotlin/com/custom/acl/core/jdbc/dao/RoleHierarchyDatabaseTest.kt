package com.custom.acl.core.jdbc.dao

import com.custom.acl.core.role.Role
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.selectAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class RoleHierarchyDatabaseTest : DatabaseTestsBase() {

    private val roleDao = RoleHierarchyDatabase(database)

    companion object {
        val USER_ROLE = Role("USER")
        val REVIEWER_ROLE = Role("REVIEWER")
        val ADMIN_ROLE = Role("ADMIN")
    }

    private fun createBasicRoles() {
        roleDao.create(USER_ROLE, null)
        roleDao.create(REVIEWER_ROLE, USER_ROLE)
        roleDao.create(ADMIN_ROLE, REVIEWER_ROLE)
    }

    @Test
    fun `create role in empty hierarchy`() {
        withTables(HierarchicalRoles) {
            roleDao.create(USER_ROLE, null)
            val all = HierarchicalRole.all()
            val roleInDB = all.first()
            assert(all.count() == 1L)
            assert(roleInDB.identity == USER_ROLE.getRoleIdentity())
            assert(roleInDB.parentId == null)
        }
    }

    @Test
    fun `create roles with wrong hierarchy`() {
        withTables(HierarchicalRoles) {
            assertThrows<IllegalArgumentException> { roleDao.create(REVIEWER_ROLE, USER_ROLE) }
        }
    }

    @Test
    fun `create full hierarchy of roles`() {
        withTables(HierarchicalRoles) {
            createBasicRoles()
            val all = HierarchicalRole.all().orderBy(HierarchicalRoles.left to SortOrder.ASC)
            assert(all.count() == 3L)
            val userRoleInDB = all.elementAt(0)
            val reviewerRoleInDB = all.elementAt(1)
            val adminRoleInDB = all.elementAt(2)

            assert(userRoleInDB.parentId == null) { "User role shouldn't have any member_of relations" }
            assert(userRoleInDB.identity == USER_ROLE.getRoleIdentity()) { "Identity should be USER" }

            assert(reviewerRoleInDB.parentId == userRoleInDB.id.value) { "REVIEWER role should be member_of USER role" }
            assert(reviewerRoleInDB.identity == REVIEWER_ROLE.getRoleIdentity()) { "Identity should be REVIEWER" }

            assert(adminRoleInDB.parentId == reviewerRoleInDB.id.value) { "ADMIN role should be member_of REVIEWER role" }
            assert(adminRoleInDB.identity == ADMIN_ROLE.getRoleIdentity()) { "Identity should be ADMIN" }
        }
    }

    @Test
    fun `get hierarchy without any roles`() {
        withTables(HierarchicalRoles) {
            assert(roleDao.hierarchy().isEmpty()) { "Hierarchy should be empty" }
        }
    }

    @Test
    fun `get hierarchy of roles`() {
        withTables(HierarchicalRoles) {
            createBasicRoles()

            val hierarchy = roleDao.hierarchy()

            assert(hierarchy[USER_ROLE] == null) { "User role shouldn't have any member_of relations" }
            assert(hierarchy[REVIEWER_ROLE] == USER_ROLE) { "REVIEWER role should be member_of USER role" }
            assert(hierarchy[ADMIN_ROLE] == REVIEWER_ROLE) { "ADMIN role should be member_of REVIEWER role" }
        }
    }

    @Test
    fun `find existing role by its identity`() {
        withTables(HierarchicalRoles) {
            createBasicRoles()

            val grantedRole = roleDao.findByIdentity(REVIEWER_ROLE.getRoleIdentity())

            assert(grantedRole?.getRoleIdentity() == REVIEWER_ROLE.getRoleIdentity()) { "Granted role should have REVIEWER identity" }
        }
    }

    @Test
    fun `find non-existing role by its identity`() {
        withTables(HierarchicalRoles) {
            createBasicRoles()

            val grantedRole = roleDao.findByIdentity("FOO")

            assert(grantedRole?.getRoleIdentity() == null) { "Granted role should be null" }
        }
    }

    @Test
    fun `get effective roles of existing role`() {
        withTables(HierarchicalRoles) {
            createBasicRoles()
            val effectiveRoles = roleDao.effectiveRoles(listOf(ADMIN_ROLE))

            assert(effectiveRoles.containsAll(listOf(USER_ROLE, REVIEWER_ROLE, ADMIN_ROLE))) {
                "Effective roles should contain: USER REVIEWER ADMIN"
            }
            assert(effectiveRoles.size == 3) {
                "Effective roles should contain only 3 roles"
            }
        }
    }

    @Test
    fun `get effective roles of non-existing role`() {
        withTables(HierarchicalRoles) {
            createBasicRoles()
            val effectiveRoles = roleDao.effectiveRoles(listOf(Role("BAR")))

            assert(effectiveRoles.isEmpty()) {
                "There shoudn't be any effective roles"
            }
        }
    }

    @Test
    fun `delete existing role from hierarchy`() {
        withTables(HierarchicalRoles) {
            createBasicRoles()
            roleDao.delete(REVIEWER_ROLE)
            commit()
            //---
            val all = HierarchicalRole.all().orderBy(HierarchicalRoles.left to SortOrder.ASC)
            assert(all.count() == 2L)
            val userRoleInDB = all.elementAt(0)
            val adminRoleInDB = all.elementAt(1)

            assert(userRoleInDB.parentId == null) { "User role shouldn't have any member_of relations" }
            assert(userRoleInDB.identity == USER_ROLE.getRoleIdentity()) { "Identity should be USER" }


            assert(adminRoleInDB.parentId == userRoleInDB.id.value) { "ADMIN role should be member_of USER role" }
            assert(adminRoleInDB.identity == ADMIN_ROLE.getRoleIdentity()) { "Identity should be ADMIN" }
        }
    }

    @Test
    fun `delete non-existing role`() {
        withTables(HierarchicalRoles) {
            createBasicRoles()
            roleDao.delete(Role("DOES_NOT_EXIST"))
            assert(HierarchicalRoles.selectAll().count() == 3L) {
                "Basic roles should not be touched"
            }
        }
    }
}