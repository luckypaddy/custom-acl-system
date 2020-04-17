package com.custom.acl.core.jdbc.utils

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database

/**
 * Wrapper for setting up connection to DB with Hikari CP
 */
object DatabaseFactory {

    /**
     * Connect to DB with Hikari CP
     *
     * @param config [HikariConfig] configuration of db connection
     * @return [Database] with [HikariDataSource]
     */
    fun connectToDb(config: HikariConfig): Database {
        config.validate()
        return Database.connect(HikariDataSource(config))
    }
}