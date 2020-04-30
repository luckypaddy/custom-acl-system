package com.custom.acl.web.demo

import com.custom.acl.core.jdbc.dao.RoleHierarchyDAO
import com.custom.acl.core.jdbc.dao.UserManagementDAO
import com.custom.acl.core.role.Role
import com.custom.acl.core.user.User
import com.custom.acl.web.demo.auth.CustomUserSession
import com.custom.acl.web.demo.dao.NewsFeedDAO
import com.custom.acl.web.demo.dao.Page
import com.custom.acl.web.demo.model.*
import io.ktor.application.Application
import io.ktor.http.*
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.ktor.sessions.SessionSerializerReflection
import io.ktor.sessions.header
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.jetbrains.exposed.sql.Database
import org.junit.jupiter.api.Test
import org.kodein.di.generic.bind
import org.kodein.di.generic.singleton
import java.net.URL
import java.time.Instant
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class NewsFeedApplicationTest {
    companion object {
        const val SESSION_HEADER = "Test-Session"
    }

    private val db = mockk<Database>(relaxed = true)
    private val userManagementDAO = mockk<UserManagementDAO>(relaxed = true)
    private val roleHierarchyDAO = mockk<RoleHierarchyDAO>(relaxed = true)
    private val newsFeedDAO = mockk<NewsFeedDAO>(relaxed = true)
    private val mapper = Json(JsonConfiguration.Default)
    private val sessionSerializer = SessionSerializerReflection(CustomUserSession::class)

    private fun mockDI(): Application.() -> Unit {
        return {
            demoApp(
                {
                    bind<Database>("mainDatabase") with singleton { db }
                    bind<UserManagementDAO>() with singleton { userManagementDAO }
                    bind<RoleHierarchyDAO>() with singleton { roleHierarchyDAO }
                    bind<NewsFeedDAO>() with singleton { newsFeedDAO }
                },
                {
                    header<CustomUserSession>(SESSION_HEADER)
                }
            )
        }
    }

    @Test
    fun `login valid user`() {
        val roles = listOf(Role("USER"))
        every { userManagementDAO.find(any<String>(), any<String>()) } returns User(
            "user",
            "password",
            roles
        )
        every { roleHierarchyDAO.effectiveRoles(roles) } returns roles

        withTestApplication(mockDI()) {
            handleRequest(HttpMethod.Post, "/login") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                val credentials = UserCredentials("user", "password")
                setBody(mapper.stringify(UserCredentials.serializer(), credentials))
            }
                .apply {
                    assertTrue { response.status()!!.isSuccess() }
                    assertTrue { response.headers.contains(SESSION_HEADER) }
                    println("HEADER: ${response.headers[SESSION_HEADER]}")
                }
        }
    }

    @Test
    fun `login with invalid username`() {
        withTestApplication(mockDI()) {
            handleRequest(HttpMethod.Post, "/login") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                val credentials = UserCredentials("usr", "password")
                setBody(mapper.stringify(UserCredentials.serializer(), credentials))
            }
                .apply {
                    assertTrue { response.status() == HttpStatusCode.BadRequest }
                    assertTrue { response.content?.contains("Invalid username or password") ?: false }
                }
        }
    }

    @Test
    fun `login with invalid password`() {
        withTestApplication(mockDI()) {
            handleRequest(HttpMethod.Post, "/login") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                val credentials = UserCredentials("user", "pass")
                setBody(mapper.stringify(UserCredentials.serializer(), credentials))
            }
                .apply {
                    assertTrue { response.status() == HttpStatusCode.BadRequest }
                    assertTrue { response.content?.contains("Invalid username or password") ?: false }
                }
        }
    }

    @Test
    fun `login valid not existing user`() {
        every { userManagementDAO.find(any<String>(), any<String>()) } returns null

        withTestApplication(mockDI()) {
            handleRequest(HttpMethod.Post, "/login") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                val credentials = UserCredentials("user", "password")
                setBody(mapper.stringify(UserCredentials.serializer(), credentials))
            }
                .apply {
                    assertTrue { response.status() == HttpStatusCode.BadRequest }
                    assertTrue { response.content?.contains("Invalid username or password") ?: false }
                }
        }
    }

    @Test
    fun `request assign roles to user`() {
        val role = Role("SOME_ROLE")
        every { userManagementDAO.assingRoles("userId", role) } returns User("1", "1", listOf(role))
        withTestApplication(mockDI()) {
            handleRequest(HttpMethod.Put, "/roles/assign") {
                val session = sessionSerializer.serialize(
                    CustomUserSession(
                        userId = "admin",
                        timestamp = Instant.now().epochSecond,
                        roles = listOf("ADMIN")
                    )
                )
                addHeader(SESSION_HEADER, session)
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                val rolesAssignRequest = RolesAssignRequest("userId", listOf("SOME_ROLE"))
                setBody(mapper.stringify(RolesAssignRequest.serializer(), rolesAssignRequest))
            }
                .apply {
                    assertEquals(HttpStatusCode.OK, response.status(), "Code should 200")
                    assertTrue { response.content?.contains("User's roles were successfully updated") ?: false }
                }
        }
    }

    @Test
    fun `request assign roles to not existing  user`() {
        val role = Role("SOME_ROLE")
        every { userManagementDAO.assingRoles("userId", role) } returns null
        withTestApplication(mockDI()) {
            handleRequest(HttpMethod.Put, "/roles/assign") {
                val session = sessionSerializer.serialize(
                    CustomUserSession(
                        userId = "admin",
                        timestamp = Instant.now().epochSecond,
                        roles = listOf("ADMIN")
                    )
                )
                addHeader(SESSION_HEADER, session)
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                val rolesAssignRequest = RolesAssignRequest(
                    "userId", listOf("SOME_ROLE")
                )
                setBody(mapper.stringify(RolesAssignRequest.serializer(), rolesAssignRequest))
            }
                .apply {
                    assertEquals(HttpStatusCode.NotFound, response.status(), "Code should 200")
                    assertTrue { response.content?.contains("User userId not found") ?: false }
                }
        }
    }


    @Test
    fun `request assign roles with insufficient permissions`() {
        withTestApplication(mockDI()) {
            handleRequest(HttpMethod.Put, "/roles/assign") {
                val session = sessionSerializer.serialize(
                    CustomUserSession(
                        userId = "userId",
                        timestamp = Instant.now().epochSecond,
                        roles = listOf("REVIEWER")
                    )
                )
                addHeader(SESSION_HEADER, session)
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                val rolesAssignRequest = RolesAssignRequest("user", listOf("SOME_ROLE"))
                setBody(mapper.stringify(RolesAssignRequest.serializer(), rolesAssignRequest))
            }
                .apply {
                    assertEquals(HttpStatusCode.Forbidden, response.status(), "Code should 403")
                    assertTrue { response.content?.contains("Insufficient roles for operation") ?: false }
                }
        }
    }

    @Test
    fun `register user`() {
        withTestApplication(mockDI()) {
            handleRequest(HttpMethod.Put, "/roles/assign") {
                val session = sessionSerializer.serialize(
                    CustomUserSession(
                        userId = "userId",
                        timestamp = Instant.now().epochSecond,
                        roles = listOf("REVIEWER")
                    )
                )
                addHeader(SESSION_HEADER, session)
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                val rolesAssignRequest = RolesAssignRequest("user", listOf("SOME_ROLE"))
                setBody(mapper.stringify(RolesAssignRequest.serializer(), rolesAssignRequest))
            }
                .apply {
                    assertEquals(HttpStatusCode.Forbidden, response.status(), "Code should 403")
                    assertTrue { response.content?.contains("Insufficient roles for operation") ?: false }
                }
        }
    }

    @Test
    fun `register valid user`() {
        every { userManagementDAO.find("user") } returns null
        every { userManagementDAO.createUser(any<User>()) } returns User("user", "password", listOf(Role("USER")))
        withTestApplication(mockDI()) {
            handleRequest(HttpMethod.Post, "/register") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                val registration = RegistrationRequest("user", "password")
                setBody(mapper.stringify(RegistrationRequest.serializer(), registration))
            }
                .apply {
                    assertTrue { response.status() == HttpStatusCode.Created }
                    assertTrue {
                        response.content?.contains("User was successfully created. Now you can login.") ?: false
                    }
                }
        }
    }

    @Test
    fun `register user with already present session`() {
        withTestApplication(mockDI()) {
            handleRequest(HttpMethod.Post, "/register") {
                val session = sessionSerializer.serialize(
                    CustomUserSession(
                        userId = "userId",
                        timestamp = Instant.now().epochSecond,
                        roles = listOf("REVIEWER")
                    )
                )
                addHeader(SESSION_HEADER, session)
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                val registration = RegistrationRequest("user", "password")
                setBody(mapper.stringify(RegistrationRequest.serializer(), registration))
            }
                .apply {
                    assertTrue { response.status() == HttpStatusCode.NoContent }
                    assertTrue { response.content?.contains("User is already registered") ?: false }
                }
        }
    }

    @Test
    fun `change password for user`() {
        val user = User(
            "userId",
            "oldpassword",
            listOf(Role("REVIEWER"))
        )
        every { userManagementDAO.find(any<String>(), any<String>()) } returns user
        every { userManagementDAO.updatePassword(user, "newpassword") } returns user.copy(passwordHash = "newpassword")
        withTestApplication(mockDI()) {
            handleRequest(HttpMethod.Put, "/change/password") {
                val session = sessionSerializer.serialize(
                    CustomUserSession(
                        userId = "userId",
                        timestamp = Instant.now().epochSecond,
                        roles = listOf("USER", "REVIEWER", "ADMIN")
                    )
                )
                addHeader(SESSION_HEADER, session)
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                val passwordChangeRequest = PasswordChangeRequest("oldpassword", "newpassword")
                setBody(mapper.stringify(PasswordChangeRequest.serializer(), passwordChangeRequest))
            }
                .apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertTrue { response.content?.contains("Password was updated") ?: false }
                }
        }
    }

    @Test
    fun `get news feeds`() {
        every { newsFeedDAO.latest(true, any<Page>()) } returns listOf(
            NewsFeed(
                1, "1", "1", "1", LocalDateTime.now(),
                URL("http://some.source.com")
            )
        )
        withTestApplication(mockDI()) {
            handleRequest(HttpMethod.Get, "/news/feed").apply {
                assertTrue { response.status()!!.isSuccess() }
                assertTrue { response.content!!.contains("http://some.source.com") }
            }
        }
    }

    @Test
    fun `get unpublished news feeds`() {
        every { newsFeedDAO.latest(false, any<Page>()) } returns listOf(
            NewsFeed(
                1, "1", "1", "1", LocalDateTime.now(),
                URL("http://some.source.com")
            )
        )
        withTestApplication(mockDI()) {
            handleRequest(HttpMethod.Get, "/news/unpublished") {
                val session = sessionSerializer.serialize(
                    CustomUserSession(
                        userId = "userId",
                        timestamp = Instant.now().epochSecond,
                        roles = listOf("REVIEWER")
                    )
                )
                addHeader(SESSION_HEADER, session)
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertTrue { response.status()!!.isSuccess() }
                assertTrue { response.content!!.contains("http://some.source.com") }
            }
        }
    }

    @Test
    fun `create news feed`() {
        val url = URL("https://goggle.com")
        every { newsFeedDAO.create("userId", "title", "content", url, any()) } returns NewsFeed(
            id = 1,
            userId = "userId",
            title = "title",
            content = "content",
            source = url,
            updatedAt = LocalDateTime.now()
        )
        withTestApplication(mockDI()) {
            handleRequest(HttpMethod.Post, "/news/post") {
                val session = sessionSerializer.serialize(
                    CustomUserSession(
                        userId = "userId",
                        timestamp = Instant.now().epochSecond,
                        roles = listOf("USER")
                    )
                )
                addHeader(SESSION_HEADER, session)
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                val request = ProcessNewsFeedRequest("title", "content", url)
                setBody(mapper.stringify(ProcessNewsFeedRequest.serializer(), request))
            }
                .apply {
                    assertEquals(HttpStatusCode.Created, response.status(), "Code should 201")
                    assertTrue { response.content?.contains("https://goggle.com") ?: false }
                }
        }
    }

    @Test
    fun `get news feed by id`() {
        val url = URL("https://goggle.com")
        every { newsFeedDAO.findById(1) } returns NewsFeed(
            id = 1,
            userId = "userId",
            title = "title",
            content = "content",
            source = url,
            updatedAt = LocalDateTime.now()
        )
        withTestApplication(mockDI()) {
            handleRequest(HttpMethod.Get, "/news/1") {
                val session = sessionSerializer.serialize(
                    CustomUserSession(
                        userId = "userId",
                        timestamp = Instant.now().epochSecond,
                        roles = listOf("REVIEWER")
                    )
                )
                addHeader(SESSION_HEADER, session)
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }
                .apply {
                    assertEquals(HttpStatusCode.OK, response.status(), "Code should 200")
                    assertTrue { response.content?.contains("https://goggle.com") ?: false }
                }
        }
    }

    @Test
    fun `edit news feed`() {
        val url = URL("https://goggle.com")
        every { newsFeedDAO.update(1, "title", "content", url, any()) } returns true
        withTestApplication(mockDI()) {
            handleRequest(HttpMethod.Put, "/news/1/edit") {
                val session = sessionSerializer.serialize(
                    CustomUserSession(
                        userId = "userId",
                        timestamp = Instant.now().epochSecond,
                        roles = listOf("REVIEWER")
                    )
                )
                addHeader(SESSION_HEADER, session)
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                val request = ProcessNewsFeedRequest("title", "content", url)
                setBody(mapper.stringify(ProcessNewsFeedRequest.serializer(), request))
            }
                .apply {
                    assertEquals(HttpStatusCode.OK, response.status(), "Code should 200")
                    assertTrue { response.content?.contains("Feed with id 1 is updated") ?: false }
                }
        }
    }

    @Test
    fun `publish news feed`() {
        every { newsFeedDAO.publish(1) } returns true
        withTestApplication(mockDI()) {
            handleRequest(HttpMethod.Put, "/news/1/publish") {
                val session = sessionSerializer.serialize(
                    CustomUserSession(
                        userId = "userId",
                        timestamp = Instant.now().epochSecond,
                        roles = listOf("REVIEWER")
                    )
                )
                addHeader(SESSION_HEADER, session)
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }
                .apply {
                    assertEquals(HttpStatusCode.OK, response.status(), "Code should 200")
                    assertTrue { response.content?.contains("Feed with id 1 is published") ?: false }
                }
        }
    }

    @Test
    fun `delete news feed by id`() {
        every { newsFeedDAO.delete(1) } returns true
        withTestApplication(mockDI()) {
            handleRequest(HttpMethod.Delete, "/news/1/delete") {
                val session = sessionSerializer.serialize(
                    CustomUserSession(
                        userId = "Admin",
                        timestamp = Instant.now().epochSecond,
                        roles = listOf("ADMIN")
                    )
                )
                addHeader(SESSION_HEADER, session)
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }
                .apply {
                    assertEquals(HttpStatusCode.OK, response.status(), "Code should 200")
                    assertTrue { response.content?.contains("Feed with id 1 is deleted") ?: false }
                }
        }
    }
}