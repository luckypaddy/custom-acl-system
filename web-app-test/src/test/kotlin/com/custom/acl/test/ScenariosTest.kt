package com.custom.acl.test

import org.jsmart.zerocode.core.domain.Scenario
import org.jsmart.zerocode.core.domain.TargetEnv
import org.jsmart.zerocode.core.runner.ZeroCodeUnitRunner
import org.junit.Test
import org.junit.runner.RunWith

@TargetEnv("disable_report.properties")
@RunWith(ZeroCodeUnitRunner::class)
class ScenariosTest {

    @Test
    @Scenario("scenarios/registerGetNewsFeeds.json")
    fun testGetNewsFeed() {
    }

    @Test
    @Scenario("scenarios/registerAssignRoleGetUnpublished.json")
    fun testGetUnpublshedFeeds() {
    }

    @Test
    @Scenario("scenarios/registerAssignRolePostFeedGetFeed.json")
    fun testGetFeedById() {
    }


    @Test
    @Scenario("scenarios/registerAssignRandomRolePostFeedPublishFeed.json")
    fun testPublishFeed() {
    }

    @Test
    @Scenario("scenarios/registerAssignRandomRolePostFeedEditFeed.json")
    fun testEditFeed() {
    }

    @Test
    @Scenario("scenarios/registerAssignRandomRolePostFeedDeleteFeed.json")
    fun testDeleteFeed() {
    }

    @Test
    @Scenario("scenarios/registerAssignNeededRoleDeleteNotExistingFeed.json")
    fun testDeleteNotExistingFeed() {
    }

    @Test
    @Scenario("scenarios/registerAssignNeededRoleEditNotExistingFeed.json")
    fun testEditNotExistingFeed() {
    }

}
