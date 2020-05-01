# custom-acl-system
Experimental implementation of role based access control

## user-management
 This module is core of ACL system for users and roles granted to them.
 It is based on [Nested Set Model](https://en.wikipedia.org/wiki/Nested_set_model) and [Exposed framework](https://github.com/JetBrains/Exposed).
 Also [HikariCP](https://github.com/brettwooldridge/HikariCP) is used.
 
## web-app-example
 Small News Feed application based on [Ktor](https://ktor.io/servers/) server application with authorization procedure based on user-management implementation.
 
## web-app-test
 Load test for web-app-example with different scenarios, which are executed simultaneously.
 It's implemented with [zerocode](https://github.com/authorjapps/zerocode) test framework