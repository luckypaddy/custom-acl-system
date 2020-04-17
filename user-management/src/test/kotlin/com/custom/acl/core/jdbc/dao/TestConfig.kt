package com.custom.acl.core.jdbc.dao

import com.zaxxer.hikari.HikariConfig

object TestConfig {
    fun testConfiguration(): HikariConfig {
        val config = HikariConfig()
        config.dataSourceClassName
        config.driverClassName = "org.h2.Driver"
        config.jdbcUrl = "jdbc:h2:mem:test"
        config.maximumPoolSize = 3
        config.isAutoCommit = true
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        return config
    }
}