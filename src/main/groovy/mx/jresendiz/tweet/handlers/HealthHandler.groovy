package mx.jresendiz.tweet.handlers

import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.client.HttpRequest
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import mx.jresendiz.tweet.messages.ResponseMessages


class HealthHandler implements Handler<RoutingContext> {

    Vertx vertx

    HealthHandler(Vertx vertxInstance) {
        vertx = vertxInstance
    }

    @Override
    void handle(RoutingContext routingContext) {
        JsonObject config = vertx.getOrCreateContext().config()
        JsonObject travisConfig = config.getJsonObject("travis")

        String travisToken = travisConfig.getString("token")
        String repositoryId = travisConfig.getLong("repositoryId")

        WebClientOptions webClientOptions = new WebClientOptions()
            .setSsl(false)
            .setLogActivity(true)
            .setConnectTimeout(5000)

        WebClient webClient = WebClient.create(vertx, webClientOptions)
        HttpRequest request = webClient.getAbs("https://api.travis-ci.org/repo/$repositoryId/builds")
        request.putHeader("Travis-API-Version", "3")
        request.putHeader("Authorization", "token $travisToken")
        request.addQueryParam("limit", "1")
        request.timeout(1000)

        request.send({ requestCompletionHandler ->
            if (requestCompletionHandler.succeeded()) {
                JsonObject serverJsonResponse = requestCompletionHandler.result().bodyAsJsonObject()
                routingContext.response()
                    .putHeader("content-type", "application/json")
                    .end(ResponseMessages.healthStatusResponse(serverJsonResponse).encodePrettily())
            } else {
                requestCompletionHandler.cause().printStackTrace()
                routingContext.response()
                    .putHeader("content-type", "application/json")
                    .end(ResponseMessages.serverError(requestCompletionHandler.cause().localizedMessage).encodePrettily())
            }
        })
    }
}
