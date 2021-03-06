package com.custom.acl.web.demo.route

import com.custom.acl.web.demo.News
import com.custom.acl.web.demo.auth.CustomUserSession
import com.custom.acl.web.demo.dao.NewsFeedDAO
import com.custom.acl.web.demo.dao.Page
import com.custom.acl.web.demo.exception.ValidationException
import com.custom.acl.web.demo.jsonMessage
import com.custom.acl.web.demo.model.ProcessNewsFeedRequest
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.*
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.kodein.di.generic.instance
import org.kodein.di.ktor.kodein
import kotlin.math.max

/**
 * Route for getting news feeds
 *
 */
@KtorExperimentalLocationsAPI
fun Route.newsFeed() {
    get<News.Feed> { feed ->
        val newFeedDao by kodein().instance<NewsFeedDAO>()
        val latest = withContext(Dispatchers.Default) {
            newFeedDao.latest(true, Page(size = feed.count, number = max(0, feed.page - 1)))
        }
        call.respond(HttpStatusCode.OK, latest)
    }
}

/**
 * Route for posting news
 *
 */
fun Route.postNew() {
    post<News.Post> {
        val session = call.sessions.get<CustomUserSession>()
            ?: throw ValidationException("User session is missing")

        val (title, content, source) = call.receive<ProcessNewsFeedRequest>()
        val newFeedDao by kodein().instance<NewsFeedDAO>()

        val newsFeed = withContext(Dispatchers.Default) {
            newFeedDao.create(session.userId, title, content, source)
        }
        call.respond(HttpStatusCode.Created, newsFeed)
    }
}

/**
 * Route for getting unpublished news
 *
 */
fun Route.unpublishedFeeds() {
    get<News.Unpublished> { feed ->
        val newFeedDao by kodein().instance<NewsFeedDAO>()
        val latest = withContext(Dispatchers.Default) {
            newFeedDao.latest(false, Page(size = feed.count, number = max(0, feed.page - 1)))
        }
        call.respond(HttpStatusCode.OK, latest)
    }
}

/**
 * Route to edit posted news
 *
 */
fun Route.editFeed() {
    put<News.Id.Edit> { edit ->
        val id = edit.newsId.id
        val (title, content, source) = call.receive<ProcessNewsFeedRequest>()
        val newFeedDao by kodein().instance<NewsFeedDAO>()

        val isUpdated = withContext(Dispatchers.Default) {
            newFeedDao.update(id, title, content, source)
        }
        when {
            isUpdated -> call.respond(HttpStatusCode.OK, jsonMessage("Feed with id $id is updated"))
            else -> call.respond(HttpStatusCode.NotFound, jsonMessage("News with id $id are not found"))
        }
    }
}

/**
 * Route to publish news
 *
 */
fun Route.publishFeed() {
    put<News.Id.Publish> { edit ->
        val newFeedDao by kodein().instance<NewsFeedDAO>()

        val id = edit.newsId.id
        val isPublished = withContext(Dispatchers.Default) {
            newFeedDao.publish(id)
        }
        when {
            isPublished -> call.respond(HttpStatusCode.OK, jsonMessage("Feed with id $id is published"))
            else -> call.respond(HttpStatusCode.NotFound, jsonMessage("News with id $id are not found"))
        }
    }

}

/**
 * Route to delete news from feed
 *
 */
fun Route.deleteFeed() {
    delete<News.Id.Delete> { delete ->
        val newFeedDao by kodein().instance<NewsFeedDAO>()
        val id = delete.newsId.id


        val isDeleted = withContext(Dispatchers.Default) { newFeedDao.delete(id) }

        when {
            isDeleted -> call.respond(HttpStatusCode.OK, jsonMessage("Feed with id $id is deleted"))
            else -> call.respond(HttpStatusCode.NotFound, jsonMessage("News with id $id are not found"))
        }
    }
}

/**
 * Route to get single feed by its Id
 *
 */
fun Route.viewFeed() {
    get<News.Id> { newsId ->
        val newFeedDao by kodein().instance<NewsFeedDAO>()
        val id = newsId.id
        val feed = withContext(Dispatchers.Default) { newFeedDao.findById(id) }

        when (feed) {
            null -> call.respond(HttpStatusCode.NotFound, jsonMessage("News with id $id are not found"))
            else -> call.respond(HttpStatusCode.OK, feed)
        }
    }
}
