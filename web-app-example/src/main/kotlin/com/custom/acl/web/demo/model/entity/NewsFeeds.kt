package com.custom.acl.web.demo.model.entity

import com.custom.acl.core.jdbc.dao.PersistedUsers
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.`java-time`.datetime

/**
 * News feed table
 */
object NewsFeeds : IntIdTable(name = "news") {
    val userId = varchar("user_id", 32)
    val title = varchar("title", 125)
    val content = varchar("content", 2200)
    val sourceLink = varchar("source", 2048 )
    val date = datetime("date")
    val isPublished = bool("isPublished")
}