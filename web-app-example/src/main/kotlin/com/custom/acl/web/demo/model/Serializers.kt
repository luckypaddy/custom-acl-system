package com.custom.acl.web.demo.model

import kotlinx.serialization.*
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Serializer(forClass = LocalDateTime::class)
object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
//    override val descriptor: SerialDescriptor =
//        PrimitiveDescriptor("com.custom.acl.web.demo.model.LocalDateTimeSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(value.format(DateTimeFormatter.ISO_DATE_TIME))
    }

    override fun deserialize(decoder: Decoder): LocalDateTime {
        return LocalDateTime.parse(decoder.decodeString(), DateTimeFormatter.ISO_DATE_TIME)
    }
}


@Serializer(forClass = URL::class)
object URLSerializer : KSerializer<URL> {
    override fun deserialize(decoder: Decoder): URL {
        return URL(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: URL) {
        encoder.encodeString(value.toString())
    }
}