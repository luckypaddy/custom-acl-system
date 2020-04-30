package com.custom.acl.web.demo.util

import com.custom.acl.core.jdbc.dao.HierarchicalRoles
import com.custom.acl.core.jdbc.dao.PersistedUsers
import com.custom.acl.core.jdbc.dao.UserRoles
import com.custom.acl.web.demo.model.database.NewsFeeds
import com.zaxxer.hikari.HikariConfig
import io.ktor.application.Application
import io.ktor.util.KtorExperimentalAPI
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Create [HikariConfig] from Application configuration
 *
 * @return
 */
@KtorExperimentalAPI
fun Application.hikariConfig(): HikariConfig {
    val config = HikariConfig()
    val databaseConfig = environment.config.config("hikaricp")

    val dataSourceClassName = databaseConfig.propertyOrNull("dataSourceClassName")?.getString()
    if (!dataSourceClassName.isNullOrBlank()) {
        config.dataSourceClassName = dataSourceClassName
        config.addDataSourceProperty("databaseName",databaseConfig.property("database").getString())
        config.addDataSourceProperty("portNumber",databaseConfig.property("port").getString().toInt())
        config.addDataSourceProperty("serverName",databaseConfig.property("host").getString())
    }

    val driverClassName = databaseConfig.propertyOrNull("driverClassName")?.getString()
    if (!driverClassName.isNullOrBlank()) {
        config.driverClassName = driverClassName
        val jdbcUrl = databaseConfig.property("jdbcUrl").getString()
        if (!jdbcUrl.isBlank()) config.jdbcUrl = jdbcUrl
    }


    val username = databaseConfig.propertyOrNull("username")?.getString()
    if (!username.isNullOrBlank()) config.username = username

    val password = databaseConfig.propertyOrNull("password")?.getString()
    if (!password.isNullOrBlank()) config.password = password

    val schema = databaseConfig.propertyOrNull("schema")?.getString()
    if (!password.isNullOrBlank()) config.schema = schema

    val transactionIsolation = databaseConfig.propertyOrNull("transactionIsolation")?.getString()
    if (!transactionIsolation.isNullOrBlank()) config.transactionIsolation = transactionIsolation

    config.isAutoCommit =
        databaseConfig.propertyOrNull("isAutoCommit")?.getString()?.toBoolean() ?: true
    config.maximumPoolSize =
        databaseConfig.propertyOrNull("maximumPoolSize")?.getString()?.toInt() ?: 5

    return config
}

/**
 * Check if database structure and create default admin user if needed
 *
 * @param adminName
 * @param adminPwdHash
 */
fun Database.checkAndInit(adminName: String, adminPwdHash: String) {
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