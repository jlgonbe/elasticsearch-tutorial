FROM frolvlad/alpine-oraclejdk8:slim
VOLUME /tmp
ADD elasticsearch-test-1.0-SNAPSHOT.jar app.jar
EXPOSE 8086:8086
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /app.jar" ]