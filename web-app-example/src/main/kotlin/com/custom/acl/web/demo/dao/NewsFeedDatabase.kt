package com.custom.acl.web.demo.dao

import com.custom.acl.web.demo.model.NewsFeed
import com.custom.acl.web.demo.model.entity.NewsFeeds
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.URL
import java.time.LocalDateTime

/**
 * Implementation of [NewsFeedDAO] to store news feed in database
 *
 * @property database
 */
class NewsFeedDatabase(private val database: Database) : NewsFeedDAO {
    override fun create(userId: String, title: String, content: String, source: URL, date: LocalDateTime): NewsFeed =
        transaction(database) {
            val id = NewsFeeds.insertAndGetId {
                it[NewsFeeds.userId] = userId
                it[NewsFeeds.title] = title
                it[NewsFeeds.content] = content
                it[NewsFeeds.sourceLink] = source.toString()
                it[NewsFeeds.date] = date
                it[NewsFeeds.isPublished] = false
            }.value
            NewsFeed(id, userId, title, content, date, source)
        }

    override fun update(id: Int, title: String, content: String, source: URL, date: LocalDateTime) =
        transaction(database) {
            val result = NewsFeeds.update({ NewsFeeds.id eq id }) {
                it[NewsFeeds.title] = title
                it[NewsFeeds.content] = content
                it[NewsFeeds.sourceLink] = source.toString()
                it[NewsFeeds.date] = date
            }
            return@transaction result != 0
        }

    override fun findById(id: Int): NewsFeed? = transaction(database) {
        NewsFeeds.select { NewsFeeds.id eq id }.singleOrNull()?.let {
            NewsFeed(
                id = it[NewsFeeds.id].value,
                userId = it[NewsFeeds.userId],
                title = it[NewsFeeds.title],
                content = it[NewsFeeds.content],
                source = URL(it[NewsFeeds.sourceLink]),
                updatedAt = it[NewsFeeds.date]
            )
        }
    }

    override fun publish(id: Int) = transaction(database) {
        val result = NewsFeeds.update({ NewsFeeds.id eq id }) {
            it[isPublished] = true
        }
        return@transaction result != 0
    }

    override fun delete(id: Int) = transaction(database) {
        val result = NewsFeeds.deleteWhere { NewsFeeds.id eq id }
        return@transaction result != 0

    }

    override fun latest(isPublished: Boolean, page: Page) = transaction(database) {
        NewsFeeds
            .select { NewsFeeds.isPublished eq isPublished }
            .limit(page.size, offset = page.offset())
            .orderBy(NewsFeeds.date to SortOrder.DESC)
            .map {
                NewsFeed(
                    id = it[NewsFeeds.id].value,
                    userId = it[NewsFeeds.userId],
                    title = it[NewsFeeds.title],
                    content = it[NewsFeeds.content],
                    source = URL(it[NewsFeeds.sourceLink]),
                    updatedAt = it[NewsFeeds.date]
                )
            }
    }

}