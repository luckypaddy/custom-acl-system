package com.custom.acl.core.jdbc.dao

import com.custom.acl.core.jdbc.utils.DatabaseFactory
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

abstract class DatabaseTestsBase {
    val database: Database = DatabaseFactory.connectToDb(TestConfig.testConfiguration())

    fun withTables(vararg tables: Table, statement: Transaction.() -> Unit) {
        transaction(database) {

            SchemaUtils.create(*tables)
            statement()
        }
        transaction {
            SchemaUtils.drop(*tables)
        }

    }
}
