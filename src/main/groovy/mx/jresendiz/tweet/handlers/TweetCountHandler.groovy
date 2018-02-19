package mx.jresendiz.tweet.handlers

import groovy.util.logging.Slf4j
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.asyncsql.AsyncSQLClient
import io.vertx.ext.asyncsql.PostgreSQLClient
import io.vertx.ext.sql.SQLConnection
import io.vertx.ext.web.RoutingContext
import mx.jresendiz.tweet.enums.StatusCodes
import mx.jresendiz.tweet.messages.ResponseMessages

@Slf4j
class TweetCountHandler implements Handler<RoutingContext> {

    static String COUNT_TWEETS_IN_AREA = "SELECT count(id) FROM tweets_information WHERE st_dwithin(geog,(st_geographyfromtext('POINT(@Longitude @Latitude)')), @Distance)";
    static final Double SEARCH_RADIUS_IN_METERS = 1000

    Vertx vertx

    TweetCountHandler(Vertx vertxInstance) {
        vertx = vertxInstance
    }

    @Override
    void handle(RoutingContext routingContext) {
        JsonObject config = vertx.orCreateContext.config()
        JsonObject database = config.getJsonObject("database")
        AsyncSQLClient postgreSQLClient = PostgreSQLClient.createShared(vertx, database)
        postgreSQLClient.getConnection({ handler ->
            if (handler.succeeded()) {
                SQLConnection connection = handler.result()
                Double latitude = Double.parseDouble(routingContext.queryParams().get("latitude"))
                Double longitude = Double.parseDouble(routingContext.queryParams().get("longitude"))

                JsonArray executeParams = new JsonArray()
                executeParams.add(longitude)
                executeParams.add(latitude)
                executeParams.add(SEARCH_RADIUS_IN_METERS)

                String finalQuery = COUNT_TWEETS_IN_AREA
                    .replace("@Longitude", "${longitude}")
                    .replace("@Latitude", "${latitude}")
                    .replace("@Distance", "${SEARCH_RADIUS_IN_METERS}")

                connection.query(finalQuery, { resultHandler ->
                    if (resultHandler.succeeded()) {
                        Long tweetCount = resultHandler.result().results[0][0] as Long
                        routingContext.response().end(ResponseMessages.tweeetCount(tweetCount).encodePrettily())
                    } else {
                        resultHandler.cause().printStackTrace()
                        routingContext
                            .response()
                            .end(ResponseMessages.serverError(resultHandler.cause().localizedMessage).encodePrettily())
                    }
                }).close()
            } else {
                routingContext
                    .response()
                    .setStatusCode(StatusCodes.SERVER_ERROR.value)
                    .end(ResponseMessages.serverError(handler.cause().localizedMessage).encodePrettily())
            }
        })
    }
}
