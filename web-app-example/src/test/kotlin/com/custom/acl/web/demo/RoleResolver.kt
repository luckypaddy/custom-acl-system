package com.custom.acl.web.demo

import kotlin.random.Random

class RoleResolver {

    companion object {
        const val ADMIN = "ADMIN"
        const val REVIEWR = "REVIEWER"
        const val USER = "USER"
        val unpublishedExpect = arrayOf(
            ADMIN to 200,
            REVIEWR to 200,
            USER to 403
        )
        val publishExpect = arrayOf(
            ADMIN to 200,
            REVIEWR to 200,
            USER to 403
        )
        val deleteExpect = arrayOf(
            ADMIN to 200,
            REVIEWR to 403,
            USER to 403
        )
        val getExpect = arrayOf(
            ADMIN to 200,
            REVIEWR to 200,
            USER to 403
        )


    }
    fun generateRoleForUnpublished(): Map<String,Any> {
        val index = Random.nextInt(unpublishedExpect.size) - 1
        return mapOf(
            "role" to unpublishedExpect[index].first,
            "status" to unpublishedExpect[index].second
        )
    }
    fun generateRoleForPublish(): Map<String,Any> {
        val index = Random.nextInt(publishExpect.size) - 1
        return mapOf(
            "role" to publishExpect[index].first,
            "status" to publishExpect[index].second
        )
    }
    fun generateRoleForDelete(): Map<String,Any> {
        val index = Random.nextInt(deleteExpect.size) - 1
        return mapOf(
            "role" to deleteExpect[index].first,
            "status" to deleteExpect[index].second
        )
    }
    fun generateRoleForGet(): Map<String,Any> {
        val index = Random.nextInt(getExpect.size) - 1
        return mapOf(
            "role" to getExpect[index].first,
            "status" to getExpect[index].second
        )
    }
}