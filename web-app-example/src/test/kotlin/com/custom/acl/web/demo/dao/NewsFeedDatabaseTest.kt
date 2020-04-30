package com.custom.acl.web.demo.dao

import com.custom.acl.core.jdbc.utils.DatabaseFactory
import com.custom.acl.web.demo.model.database.NewsFeeds
import com.zaxxer.hikari.HikariConfig
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.net.URL
import java.time.LocalDateTime

internal class NewsFeedDatabaseTest {
    private val database: Database
    private val newsFeedDAO: NewsFeedDatabase

    init {
        val config = HikariConfig()
        config.dataSourceClassName
        config.driverClassName = "org.h2.Driver"
        config.jdbcUrl = "jdbc:h2:mem:test"
        config.maximumPoolSize = 3
        config.isAutoCommit = true
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        database = DatabaseFactory.connectToDb(config)
        newsFeedDAO = NewsFeedDatabase(database)
    }

    companion object {
        const val USER_ID = "userId"
        const val TITLE = "title"
        const val CONTENT = "content"
        val URL = URL("http://custom.acl.com")
    }

    @BeforeEach
    fun createTable() {
        transaction(database) {
            SchemaUtils.create(NewsFeeds)
        }

    }

    @AfterEach
    fun dropTable() {
        transaction {
            SchemaUtils.drop(NewsFeeds)
        }
    }

    private fun createNewsFeedTableEntry(
        userId: String,
        title: String,
        content: String,
        source: URL,
        date: LocalDateTime = LocalDateTime.now(),
        isPublished: Boolean = false
    ): Int = transaction(database) {
        NewsFeeds.insertAndGetId {
            it[NewsFeeds.userId] = userId
            it[NewsFeeds.title] = title
            it[NewsFeeds.content] = content
            it[NewsFeeds.sourceLink] = source.toString()
            it[NewsFeeds.date] = date
            it[NewsFeeds.isPublished] = isPublished
        }.value
    }


    @Test
    fun `create news feed`() {
        newsFeedDAO.create(USER_ID, TITLE, CONTENT, URL)
        transaction(database) {
            val newsList = NewsFeeds.selectAll().toList()
            assert(newsList.size == 1) { "Only one news feed should be created" }
            assert(newsList[0][NewsFeeds.userId] == USER_ID) { "User should be the same" }
            assert(newsList[0][NewsFeeds.title] == TITLE) { "Title should be the same" }
            assert(newsList[0][NewsFeeds.content] == CONTENT) { "Content should be the same" }
            assert(newsList[0][NewsFeeds.isPublished] == false) { "Newly created feed should not be published" }
            assert(URL(newsList[0][NewsFeeds.sourceLink]) == URL) { "Source link should be the same" }
        }
    }

    @Test
    fun `update existing news feed`() {
        val id = createNewsFeedTableEntry(
            userId = USER_ID,
            title = "wrong title",
            content = "wrong content",
            source = URL("https://google.com")
        )

        val status = newsFeedDAO.update(id, TITLE, CONTENT, URL)
        assert(status == true) { "Update of news feed should return TRUE" }

        transaction(database) {
            val newsList = NewsFeeds.selectAll().toList()
            assert(newsList.size == 1) { "Only one news feed should be created" }
            assert(newsList[0][NewsFeeds.userId] == USER_ID) { "User should be the same" }
            assert(newsList[0][NewsFeeds.title] == TITLE) { "Title should be the same" }
            assert(newsList[0][NewsFeeds.content] == CONTENT) { "Content should be the same" }
            assert(newsList[0][NewsFeeds.isPublished] == false) { "Newly created feed should not be published" }
            assert(URL(newsList[0][NewsFeeds.sourceLink]) == URL) { "Source link should be the same" }
        }
    }

    @Test
    fun `update not existing news feed`() {
        val id = createNewsFeedTableEntry(
            userId = USER_ID,
            title = "wrong title",
            content = "wrong content",
            source = URL("https://google.com")
        )

        val status = newsFeedDAO.update(id + 1, TITLE, CONTENT, URL)
        assert(status == false) { "Update of news feed should return FALSE" }

        transaction(database) {
            val newsList = NewsFeeds.selectAll().toList()
            assert(newsList.size == 1) { "Only one news feed should be present" }
            assert(newsList[0][NewsFeeds.userId] == USER_ID) { "User should be the same" }
            assert(newsList[0][NewsFeeds.title] != TITLE) { "Title should not be the same" }
            assert(newsList[0][NewsFeeds.content] != CONTENT) { "Content should not be the same" }
            assert(URL(newsList[0][NewsFeeds.sourceLink]) != URL) { "Source link should not be the same" }
        }
    }

    @Test
    fun `find existing news feed by id`() {
        val id = createNewsFeedTableEntry(
            userId = USER_ID,
            title = TITLE,
            content = CONTENT,
            source = URL
        )

        val newsFeed = newsFeedDAO.findById(id) ?: fail { "News feed should be found" }
        assert(newsFeed.id == id) { "Id should be the same" }
        assert(newsFeed.userId == USER_ID) { "User id should be the same" }
        assert(newsFeed.title == TITLE) { "Title should be the same" }
        assert(newsFeed.content == CONTENT) { "Content should be the same" }
        assert(newsFeed.source == URL) { "Source link should be the same" }
    }

    @Test
    fun `find not existing news feed by id`() {
        val id = createNewsFeedTableEntry(
            userId = USER_ID,
            title = TITLE,
            content = CONTENT,
            source = URL
        )

        val newsFeed = newsFeedDAO.findById(id + 1)
        assert(newsFeed == null) { "No news feed should be found" }
    }

    @Test
    fun `publish existing news feed`() {
        val id = createNewsFeedTableEntry(
            userId = USER_ID,
            title = TITLE,
            content = CONTENT,
            source = URL
        )

        val status = newsFeedDAO.publish(id)

        assert(status == true) { "Publish should return TRUE " }

        transaction(database) {
            val result = NewsFeeds.selectAll().toList()
            assert(result.size == 1) { "Only 1 row should be found" }
            assert(result[0][NewsFeeds.id].value == id) { "Id should be the same" }
            assert(result[0][NewsFeeds.isPublished] == true) { "Id should be the same" }
        }
    }

    @Test
    fun `publish not existing news feed`() {
        val id = createNewsFeedTableEntry(
            userId = USER_ID,
            title = TITLE,
            content = CONTENT,
            source = URL
        )

        val status = newsFeedDAO.publish(id + 1)

        assert(status == false) { "Publish should return FALSE " }

        transaction(database) {
            val result = NewsFeeds.select {
                NewsFeeds.isPublished eq true
            }.toList()
            assert(result.isEmpty()) { "Only 1 row should be found" }
        }
    }

    @Test
    fun `delete existing feed`() {
        val id = createNewsFeedTableEntry(
            userId = USER_ID,
            title = TITLE,
            content = CONTENT,
            source = URL
        )

        val status = newsFeedDAO.delete(id)

        assert(status == true) { "Delete should return TRUE " }
        transaction(database) {
            val result = NewsFeeds.selectAll().toList()
            assert(result.isEmpty()) { "No row should be found" }
        }
    }

    @Test
    fun `delete not existing feed`() {
        val id = createNewsFeedTableEntry(
            userId = USER_ID,
            title = TITLE,
            content = CONTENT,
            source = URL
        )

        val status = newsFeedDAO.delete(id + 1)

        assert(status == false) { "Publish should return FALSE" }
        transaction(database) {
            val result = NewsFeeds.selectAll().toList()
            assert(!result.isEmpty()) { "Rows should be found" }
        }
    }

    @Test
    fun `latest with only not published news`() {
        repeat(11) {
            createNewsFeedTableEntry(
                userId = USER_ID,
                title = TITLE,
                content = CONTENT,
                source = URL,
                isPublished = false
            )
        }
        assert(newsFeedDAO.latest(isPublished = true).isEmpty()) { "List of latest published news should be empty" }
        assert(newsFeedDAO.latest(isPublished = false).size == 10) {
            "List of latest not published news should have dafault size"
        }
    }

    @Test
    fun `latest with only published news`() {
        repeat(11) {
            createNewsFeedTableEntry(
                userId = USER_ID,
                title = TITLE,
                content = CONTENT,
                source = URL,
                isPublished = true
            )
        }
        assert(
            newsFeedDAO.latest(isPublished = false).isEmpty()
        ) { "List of latest not published news should be empty" }
        assert(newsFeedDAO.latest(isPublished = true).size == 10) {
            "List of latest published news should have default size"
        }
    }

    @Test
    fun `latest with mix of published and not published news`() {
        repeat(12) {
            createNewsFeedTableEntry(
                userId = USER_ID,
                title = TITLE,
                content = CONTENT,
                source = URL,
                isPublished = true
            )
        }
        repeat(13) {
            createNewsFeedTableEntry(
                userId = USER_ID,
                title = TITLE,
                content = CONTENT,
                source = URL,
                isPublished = false
            )
        }
        assert(
            newsFeedDAO.latest(isPublished = false, page = Page(20, 0)).size == 13
        ) { "List of latest not published news should have correct size" }
        assert(newsFeedDAO.latest(isPublished = true, page = Page(20, 0)).size == 12) {
            "List of latest published news should have correct size"
        }
    }

}