package com.custom.acl.web.demo.util

import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.hex
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


object SecurityUtils {
    /**
     * Hardcoded secret hash key used to hash the user's passwords, because they are persisted
     */
    @KtorExperimentalAPI
    val hashKey = hex("2819b57a376945c1968f45237589")

    /**
     * HMac SHA1 key spec for the password hashing.
     */
    @KtorExperimentalAPI
    val hmacKey =
        SecretKeySpec(hashKey, "HmacSHA1")

    /**
     * Method that hashes a [password] by using the globally defined secret key [hmacKey].
     */
    @KtorExperimentalAPI
    fun hash(password: String): String {
        val hmac = Mac.getInstance("HmacSHA1")
        hmac.init(hmacKey)
        return hex(hmac.doFinal(password.toByteArray(Charsets.UTF_8)))
    }
}