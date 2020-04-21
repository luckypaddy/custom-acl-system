package com.custom.acl.web.demo.util

import com.custom.acl.core.jdbc.dao.HierarchicalRoles
import com.custom.acl.core.jdbc.dao.PersistedUsers
import com.custom.acl.core.jdbc.dao.UserRoles
import com.zaxxer.hikari.HikariConfig
import io.ktor.application.Application
import io.ktor.util.KtorExperimentalAPI
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

@KtorExperimentalAPI
fun hikariConfig(app: Application): HikariConfig {
    val config = HikariConfig()

    //TODO rework it with reflection+delegation and application.environment.config.config("hikaricp")
    val dataSourceClassName = app.environment.config.propertyOrNull("hikaricp.dataSourceClassName")?.getString()
    if (!dataSourceClassName.isNullOrBlank()) config.dataSourceClassName = dataSourceClassName

    val driverClassName = app.environment.config.propertyOrNull("hikaricp.driverClassName")?.getString()
    if (!driverClassName.isNullOrBlank()) config.driverClassName = driverClassName

    val jdbcUrl = app.environment.config.propertyOrNull("hikaricp.jdbcUrl")?.getString()
    if (!jdbcUrl.isNullOrBlank()) config.jdbcUrl = jdbcUrl

    val username = app.environment.config.propertyOrNull("hikaricp.username")?.getString()
    if (!username.isNullOrBlank()) config.username = username

    val password = app.environment.config.propertyOrNull("hikaricp.password")?.getString()
    if (!password.isNullOrBlank()) config.password = password

    val schema = app.environment.config.propertyOrNull("hikaricp.schema")?.getString()
    if (!password.isNullOrBlank()) config.schema = schema

    val transactionIsolation = app.environment.config.propertyOrNull("hikaricp.transactionIsolation")?.getString()
    if (!transactionIsolation.isNullOrBlank()) config.transactionIsolation = transactionIsolation

    config.isAutoCommit =
        app.environment.config.propertyOrNull("hikaricp.isAutoCommit")?.getString()?.toBoolean() ?: true
    config.maximumPoolSize =
        app.environment.config.propertyOrNull("hikaricp.maximumPoolSize")?.getString()?.toInt() ?: 5

    return config
}

fun checkAndInit(database: Database, adminName: String, adminPwdHash: String) {
    transaction(database) {
        SchemaUtils.createMissingTablesAndColumns(
            HierarchicalRoles,
            PersistedUsers,
            UserRoles
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