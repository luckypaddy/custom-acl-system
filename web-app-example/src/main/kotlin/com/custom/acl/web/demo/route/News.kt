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
import org.kodein.di.generic.instance
import org.kodein.di.ktor.kodein
import kotlin.math.max

@KtorExperimentalLocationsAPI
fun Route.newsFeed() {
    get<News.Feed> { feed ->
        val newFeedDao by kodein().instance<NewsFeedDAO>()
        val latest = newFeedDao.latest(true, Page(size = feed.count, number = max(0, feed.page - 1)))
        call.respond(HttpStatusCode.OK, latest)
    }
}

fun Route.postNew() {
    post<News.Post> {
        val session = call.sessions.get<CustomUserSession>()
            ?: throw ValidationException("User session is missing")

        val (title, content, source) = call.receive<ProcessNewsFeedRequest>()
        val newFeedDao by kodein().instance<NewsFeedDAO>()

        val newsFeed = newFeedDao.create(session.userId, title, content, source)

        call.respond(HttpStatusCode.Created, newsFeed)
    }
}

fun Route.unpublishedFeeds() {
    get<News.Unpublished> { feed ->
        val newFeedDao by kodein().instance<NewsFeedDAO>()
        val latest = newFeedDao.latest(false, Page(size = feed.count, number = max(0, feed.page - 1)))
        call.respond(HttpStatusCode.OK, latest)
    }
}

fun Route.editFeed() {
    put<News.Id.Edit> { edit ->
        val id = edit.newsId.id
        val (title, content, source) = call.receive<ProcessNewsFeedRequest>()
        val newFeedDao by kodein().instance<NewsFeedDAO>()

        val isUpdated = newFeedDao.update(id, title, content, source)
        when {
            isUpdated -> call.respond(HttpStatusCode.OK, jsonMessage("Feed with id $id is updated"))
            else -> throw ValidationException("News with id $id are not found")
        }
    }
}

fun Route.publishFeed() {
    post<News.Id.Publish> { edit ->
        val newFeedDao by kodein().instance<NewsFeedDAO>()

        val id = edit.newsId.id
        val isPublished = newFeedDao.publish(id)

        when {
            isPublished -> call.respond(HttpStatusCode.OK, jsonMessage("Feed with id $id is published"))
            else -> throw ValidationException("News with id $id are not found")
        }
    }
}

fun Route.deleteFeed() {
    delete<News.Id.Delete> { delete ->
        val newFeedDao by kodein().instance<NewsFeedDAO>()
        val id = delete.newsId.id

        val isDeleted = newFeedDao.delete(id)

        when {
            isDeleted -> call.respond(HttpStatusCode.OK, jsonMessage("Feed with id $id is deleted"))
            else -> throw ValidationException("News with id $id are not found")
        }
    }
}

fun Route.viewFeed(){
    get<News.Id> {newsId ->
        val newFeedDao by kodein().instance<NewsFeedDAO>()
        val id = newsId.id

        when (val feed = newFeedDao.findById(id)){
            null ->throw ValidationException("News with id $id are not found")
            else ->call.respond(HttpStatusCode.OK, feed)
        }
    }
}
