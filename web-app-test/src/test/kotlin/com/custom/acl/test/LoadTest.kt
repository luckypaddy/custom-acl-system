package com.custom.acl.test

import org.jsmart.zerocode.core.domain.LoadWith
import org.jsmart.zerocode.core.domain.TestMapping
import org.jsmart.zerocode.core.domain.TestMappings
import org.jsmart.zerocode.jupiter.extension.ParallelLoadExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@LoadWith("load_test.properties")
@ExtendWith(ParallelLoadExtension::class)
class LoadTest {
    @Test
    @TestMappings(
        TestMapping(testClass = ScenariosTest::class, testMethod = "testGetNewsFeed"),
        TestMapping(testClass = ScenariosTest::class, testMethod = "testGetUnpublshedFeeds"),
        TestMapping(testClass = ScenariosTest::class, testMethod = "testGetFeedById"),
        TestMapping(testClass = ScenariosTest::class, testMethod = "testPublishFeed"),
        TestMapping(testClass = ScenariosTest::class, testMethod = "testEditFeed"),
        TestMapping(testClass = ScenariosTest::class, testMethod = "testDeleteFeed"),
        TestMapping(testClass = ScenariosTest::class, testMethod = "testDeleteNotExistingFeed"),
        TestMapping(testClass = ScenariosTest::class, testMethod = "testEditNotExistingFeed")
    )
    fun test() {
    }
}