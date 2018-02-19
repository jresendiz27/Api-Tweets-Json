package mx.jresendiz.tweet.handlers

import groovy.util.logging.Slf4j
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.client.HttpRequest
import io.vertx.ext.web.client.WebClient
import mx.jresendiz.tweet.messages.ResponseMessages

@Slf4j
class ApiTestHandler implements Handler<RoutingContext> {
    Vertx vertx

    ApiTestHandler(Vertx vertxInstance) {
        vertx = vertxInstance
    }

    @Override
    void handle(RoutingContext routingContext) {
        JsonObject config = vertx.getOrCreateContext().config()
        JsonObject serverConfig = config.getJsonObject("server")
        Integer serverPort = serverConfig.getInteger("port")
        WebClient webClient = WebClient.create(vertx)
        HttpRequest request = webClient.get(serverPort, "localhost", "/tweet")
        request.addQueryParam("latitude", "90")
        request.addQueryParam("longitude", "90")

        request.send({ requestCompletionHandler ->
            if (requestCompletionHandler.succeeded()) {
                log.info(requestCompletionHandler.result().toString())
                JsonObject serverJsonResponse = requestCompletionHandler.result().bodyAsJsonObject()
                serverJsonResponse.put("automatic", true)
                serverJsonResponse.put("date", new Date().toString())
                routingContext.response()
                    .putHeader("content-type", "application/json")
                    .end(serverJsonResponse.encodePrettily())
            } else {
                requestCompletionHandler.cause().printStackTrace()
                routingContext.response()
                    .putHeader("content-type", "application/json")
                    .end(ResponseMessages.serverError(requestCompletionHandler.cause().localizedMessage).encodePrettily())
            }
        })
    }
}
