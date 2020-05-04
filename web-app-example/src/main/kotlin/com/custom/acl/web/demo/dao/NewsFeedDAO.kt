package com.custom.acl.web.demo.dao

import com.custom.acl.web.demo.model.NewsFeed
import java.net.URL
import java.time.LocalDateTime

/**
 * DAO Interface for news feed
 *
 */
interface NewsFeedDAO {
    /**
     * Create [NewsFeed]
     *
     * @param userId  identifier of user
     * @param title title of news
     * @param content content of news
     * @param source link of the source
     * @param date datetime when it was posted
     * @return [NewsFeed]
     */
    fun create(userId: String, title: String, content: String, source: URL, date: LocalDateTime = LocalDateTime.now()): NewsFeed

    /**
     * Update [NewsFeed] with specified id
     *
     * @param id identifier of News Feed
     * @param title new title
     * @param content new content
     * @param source new link of the source
     * @param date of update
     * @return true if update is successful otherwise false
     */
    fun update(id: Int, title: String, content: String, source: URL, date: LocalDateTime = LocalDateTime.now()): Boolean

    /**
     * Find [NewsFeed] with specified id
     *
     * @param id identifier of News Feed
     * @return [NewsFeed] or null if News Feed is not present
     */
    fun findById(id: Int): NewsFeed?

    /**
     * Mark news feed with specified Id as published
     *
     * @param id identifier of News Feed
     * @return true if operation is successful otherwise false
     */
    fun publish(id: Int): Boolean

    /**
     * Delete [NewsFeed] with specified id
     *
     * @param id identifier of News Feed
     * @return true if operation is successful otherwise false
     */
    fun delete(id: Int): Boolean

    /**
     * Find latest set of [NewsFeed]
     *
     * @param isPublished flag if search published news or not
     * @param page [Page] for pagination
     * @return [Collection] of [NewsFeed]
     */
    fun latest(isPublished: Boolean, page: Page = Page(10, 0)): Collection<NewsFeed>
}