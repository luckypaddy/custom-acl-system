package com.custom.acl.web.demo

import io.ktor.sessions.SessionStorage
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel

class CustomSessionStorage : SessionStorage {
    override suspend fun invalidate(id: String) {
        TODO("Not yet implemented")
    }

    override suspend fun <R> read(id: String, consumer: suspend (ByteReadChannel) -> R): R {
        TODO("Not yet implemented")
    }

    override suspend fun write(id: String, provider: suspend (ByteWriteChannel) -> Unit) {
        TODO("Not yet implemented")
    }

}
