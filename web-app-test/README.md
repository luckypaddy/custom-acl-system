# Web app load test

Load test is implemented with [zerocode](https://github.com/authorjapps/zerocode) test framework

Test scenarios are described in 'src/test/resources/scenarios' folder, more details on Zerocode [UserGuide](https://github.com/authorjapps/zerocode/wiki/Getting-Started).

Load test configuration is present 'src/test/resources/load_test.properties', for more [details](https://github.com/authorjapps/zerocode/wiki/Load-or-Performance-Testing-(IDE-based)#how-to-run-tests-in-parallel-in-context-of-one-or-more-scenarios-).

Before running test please make sure 'web-app-example' is running. To run test just execute:

    ./gradlew custom-acl-system:web-app-example test -Pload.test.host=<web-app-example_hostname> -Pload.test.admin.user=<admin_user_of_app> -Pload.test.admin.password=<admin_password_of_app>
    
OR set these values in 'gradle.properties' file:

        load.test.host=http://localhost:8080
        load.test.admin.user=Admin
        load.test.admin.password=securedpwd
        
AND run:

    ./gradlew custom-acl-system:web-app-example test
    
Test result will be present in 'web-app-test/target' folder (target part is harcoded in library)
