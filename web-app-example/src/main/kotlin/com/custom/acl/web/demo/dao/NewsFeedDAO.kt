package com.custom.acl.web.demo.dao

import com.custom.acl.web.demo.model.NewsFeed
import java.net.URL
import java.time.LocalDateTime

interface NewsFeedDAO {
    fun create(
        userId: String,
        title: String,
        content: String,
        source: URL,
        date: LocalDateTime = LocalDateTime.now()
    ): NewsFeed

    fun update(
        id: Int,
        title: String,
        content: String,
        source: URL,
        date: LocalDateTime = LocalDateTime.now()
    ): Boolean

    fun findById(id: Int): NewsFeed?

    fun publish(id: Int): Boolean

    fun delete(id: Int): Boolean

    fun latest(isPublished: Boolean, page: Page = Page(10, 0)): Collection<NewsFeed>
}