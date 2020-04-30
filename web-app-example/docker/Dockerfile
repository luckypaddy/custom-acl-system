FROM openjdk:8-jre-alpine
VOLUME /tmp
ADD @artifact.jar.name@ app.jar
ADD application_docker.conf application_docker.conf
EXPOSE 8080
ENTRYPOINT ["java", "-server", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-XX:InitialRAMFraction=2", "-XX:MinRAMFraction=2", "-XX:MaxRAMFraction=2", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-XX:+UseStringDeduplication", "-jar", "/app.jar","-config=/application_docker.conf"]

