# Web application example

This is simple "News feed" application build with [Ktor](https://ktor.io) framework.
Rest API documentation is present via OAS3 definition(api.yaml file). It can be accessed via paths: '/' and '/index.html'  
To run it there are options:
### Run with embedded H2 database via gradle
To do it just run

    ./gradlew custom-acl-system:web-app-example run
    
    
### Run FatJar with embedded H2 database
Build project:

    ./gradlew custom-acl-system:web-app-example build
    
After that in 'build/libs' execute (version might be different):
 
    java -jar build\libs\web-app-example-1.0-SNAPSHOT.jar
    
### Run in Docker with PostgreSQL
Build project and copy processed resources:

    ./gradlew custom-acl-system:web-app-example build copyProcessedResources

After that in 'build/artifacts' execute:

     docker-compose up -d --build

Note! By default it exposes application on port 8080 and PostgreSQL on port 5432. If 
they are already binded please adjust 'docker-compose.yml' and 'Dockerfile' in 'docker' folder.

Default usernames and passwords are described in 'build.gradle.kts' at "processResources" task.
They can be changed, replaced with placeholders or external properties.