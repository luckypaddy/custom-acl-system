package com.custom.acl.core.jdbc.dao

import com.zaxxer.hikari.HikariConfig
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object TestConfig {
    fun testConfiguration(): HikariConfig {
        val config = HikariConfig()
        config.dataSourceClassName
        config.driverClassName = "org.h2.Driver"
        config.jdbcUrl = "jdbc:h2:mem:test"
        config.maximumPoolSize = 3
        config.isAutoCommit = false
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        return config
    }

    fun initDB(db: Database) {
        transaction(db) {
            SchemaUtils.create(HierarchicalRoles, PersistedUsers)
        }
    }
}