package com.custom.acl.web.demo.util

import com.custom.acl.core.jdbc.dao.HierarchicalRoles
import com.custom.acl.core.jdbc.dao.PersistedUsers
import com.custom.acl.core.jdbc.dao.UserRoles
import com.custom.acl.web.demo.model.entity.NewsFeeds
import com.zaxxer.hikari.HikariConfig
import io.ktor.application.Application
import io.ktor.util.KtorExperimentalAPI
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

@KtorExperimentalAPI
fun Application.hikariConfig(): HikariConfig {
    val config = HikariConfig()

    //TODO rework it with reflection+delegation and application.environment.config.config("hikaricp")
    val dataSourceClassName = environment.config.propertyOrNull("hikaricp.dataSourceClassName")?.getString()
    if (!dataSourceClassName.isNullOrBlank()) config.dataSourceClassName = dataSourceClassName

    val driverClassName = environment.config.propertyOrNull("hikaricp.driverClassName")?.getString()
    if (!driverClassName.isNullOrBlank()) config.driverClassName = driverClassName

    val jdbcUrl = environment.config.propertyOrNull("hikaricp.jdbcUrl")?.getString()
    if (!jdbcUrl.isNullOrBlank()) config.jdbcUrl = jdbcUrl

    val username = environment.config.propertyOrNull("hikaricp.username")?.getString()
    if (!username.isNullOrBlank()) config.username = username

    val password = environment.config.propertyOrNull("hikaricp.password")?.getString()
    if (!password.isNullOrBlank()) config.password = password

    val schema = environment.config.propertyOrNull("hikaricp.schema")?.getString()
    if (!password.isNullOrBlank()) config.schema = schema

    val transactionIsolation = environment.config.propertyOrNull("hikaricp.transactionIsolation")?.getString()
    if (!transactionIsolation.isNullOrBlank()) config.transactionIsolation = transactionIsolation

    config.isAutoCommit =
        environment.config.propertyOrNull("hikaricp.isAutoCommit")?.getString()?.toBoolean() ?: true
    config.maximumPoolSize =
        environment.config.propertyOrNull("hikaricp.maximumPoolSize")?.getString()?.toInt() ?: 5

    return config
}

fun Database.checkAndInit(
    adminName: String,
    adminPwdHash: String
) {
    transaction(this) {
        SchemaUtils.create(
            HierarchicalRoles,
            PersistedUsers,
            UserRoles,
            NewsFeeds
        )

        val countRoles = HierarchicalRoles.selectAll().count()
        val countAdminUser =
            PersistedUsers.select { PersistedUsers.username eq adminName }
                .count()

        if ((countRoles == 0L) && (countAdminUser == 0L)) {
            val userId = HierarchicalRoles.insertAndGetId {
                it[parentId] = null
                it[identity] = "USER"
                it[left] = 1
                it[right] = 6
            }
            val reviewerId = HierarchicalRoles.insertAndGetId {
                it[parentId] = userId.value
                it[identity] = "REVIEWER"
                it[left] = 2
                it[right] = 5
            }
            val adminId = HierarchicalRoles.insertAndGetId {
                it[parentId] = reviewerId.value
                it[identity] = "ADMIN"
                it[left] = 3
                it[right] = 4
            }

            val adminUser = PersistedUsers.insertAndGetId {
                it[username] = adminName
                it[passwordHash] = adminPwdHash
            }

            UserRoles.insert {
                it[role] = adminId
                it[user] = adminUser
            }
        }
    }
}