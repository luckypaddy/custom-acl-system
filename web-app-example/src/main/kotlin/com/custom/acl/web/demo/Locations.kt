package com.custom.acl.web.demo

import io.ktor.locations.Location

@Location("/login")
data class Login(val userName: String = "", val error: String = "")

@Location("/register")
data class Register(val userName: String = "", val error: String = "")

@Location("/change/password")
data class ChangePassword(val userName: String = "", val error: String = "")

@Location("/roles/assign")
data class AssignRoles(val userName: String = "", val error: String = "")

@Location("/news")
class News {

    @Location("/feed")
    data class Feed(val news: News, val page: Int = 1, val count: Int = 10)

    @Location("/post")
    data class Post(val news: News)

    @Location("/unpublished")
    data class Unpublished(val news: News, val page: Int = 1, val count: Int = 10)

    @Location("/{id}")
    data class Id(val news: News, val id: Int) {

        @Location("/edit")
        data class Edit(val newsId: Id)

        @Location("/delete")
        data class Delete(val newsId: Id)

        @Location("/publish")
        data class Publish(val newsId: Id)
    }

}
