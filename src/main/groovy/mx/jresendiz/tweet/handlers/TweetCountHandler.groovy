package mx.jresendiz.tweet.handlers

import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.asyncsql.AsyncSQLClient
import io.vertx.ext.asyncsql.PostgreSQLClient
import io.vertx.ext.sql.SQLConnection
import io.vertx.ext.web.RoutingContext


class TweetCountHandler implements Handler<RoutingContext> {

    Vertx vertx

    TweetCountHandler(Vertx vertxInstance) {
        vertx = vertxInstance
    }

    @Override
    void handle(RoutingContext routingContext) {
        JsonObject config = vertx.orCreateContext.config().getJsonObject("database")
        AsyncSQLClient postgreSQLClient = PostgreSQLClient.createShared(vertx, config)
        postgreSQLClient.getConnection({ handler ->
            SQLConnection connection = handler.result()
            // TODO create database
            // TODO verify indexes ...
            // TODO connect with Twitter API ...
            connection.queryWithParams("select count(id) from tweets where position in range...", new JsonArray(), { resultHandler ->
                resultHandler.result().getResults().each {
                    routingContext.response().end()
                }
            }).close()
        })
    }
}
