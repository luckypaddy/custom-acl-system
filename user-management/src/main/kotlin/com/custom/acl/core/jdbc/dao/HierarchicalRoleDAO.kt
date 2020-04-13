package com.custom.acl.core.jdbc.dao

import com.custom.acl.core.role.GrantedRole
import com.custom.acl.core.role.hierarchy.RoleHierarchy
import java.io.Closeable

interface HierarchicalRoleDAO: RoleHierarchy, Closeable {

    fun create(role: GrantedRole, parentRole: GrantedRole)

    fun deleteByIdentity(identity: String)

    fun findBasicRoles(): Collection<GrantedRole>

}