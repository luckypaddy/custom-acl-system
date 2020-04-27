package com.custom.acl.web.demo

import org.jsmart.zerocode.core.domain.Scenario
import org.jsmart.zerocode.core.domain.TargetEnv
import org.jsmart.zerocode.core.runner.ZeroCodeUnitRunner
import org.junit.Test
import org.junit.runner.RunWith

@TargetEnv("local_host.properties")
@RunWith(ZeroCodeUnitRunner::class)
class NewsFeedAplicationTest {

    @Test
    @Scenario("scenarios/registerGetNewsFeeds.json")
    fun test1() {
    }

    @Test
    @Scenario("scenarios/registerAssignRoleGetUnpublished.json")
    fun test2() {
    }
//
//    @Test
//    @Scenario("scenarios/registerAssignRolePostFeedGetFeed.json")
//    fun test3() {
//    }
//
//    @Test
//    @Scenario("scenarios/registerAssignRandomRolePostFeedPublishFeed.json")
//    fun test4() {
//    }
//
//    @Test
//    @Scenario("scenarios/registerAssignRandomRolePostFeedDeleteFeed.json")
//    fun test5() {
//    }
//    @Test
//    @Scenario("scenarios/registerAssignNeededRoleDeleteNotExistingFeed.json")
//    fun test5() {
//    }
//    @Test
//    @Scenario("scenarios/registerAssignNeededRolePublishNotExistingFeed.json")
//    fun test5() {
//    }
//    @Test
//    @Scenario("scenarios/registerAssignNeededRolePublishNotExistingFeed.json")
//    fun test5() {
//    }

}