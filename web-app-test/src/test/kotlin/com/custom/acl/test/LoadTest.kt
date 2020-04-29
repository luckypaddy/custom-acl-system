package com.custom.acl.test

import org.jsmart.zerocode.core.domain.LoadWith
import org.jsmart.zerocode.core.domain.TestMapping
import org.jsmart.zerocode.core.domain.TestMappings
import org.jsmart.zerocode.core.runner.parallel.ZeroCodeMultiLoadRunner
import org.junit.runner.RunWith

@LoadWith("load_test.properties")
@RunWith(ZeroCodeMultiLoadRunner::class)
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
class LoadTest