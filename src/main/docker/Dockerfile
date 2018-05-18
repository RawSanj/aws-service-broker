FROM openjdk:8-jre-alpine
VOLUME /tmp
ADD *.jar app.jar
RUN sh -c 'touch /app.jar'
ENV JAVA_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap"
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /app.jar" ]