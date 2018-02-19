FROM java:8-jdk-alpine
MAINTAINER  Juan Alberto Reséndiz Arteaga <jresendiz27@gmail.com>
ENV VERTICLE_FILE tweet-1.0.0-SNAPSHOT-fat.jar
ENV VERTICLE_HOME /usr/verticles
EXPOSE 8080
COPY build/libs/$VERTICLE_FILE $VERTICLE_HOME/
WORKDIR $VERTICLE_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec java -jar $VERTICLE_FILE"]
