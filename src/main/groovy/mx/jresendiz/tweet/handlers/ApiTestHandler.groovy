package mx.jresendiz.tweet.handlers

import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.ext.web.RoutingContext
import mx.jresendiz.tweet.messages.ResponseMessages

class ApiTestHandler implements Handler<RoutingContext> {
    Vertx vertx

    ApiTestHandler(Vertx vertxInstance) {
        vertx = vertxInstance
    }

    // TODO implement me
    @Override
    void handle(RoutingContext routingContext) {

        routingContext.response()
            .putHeader("content-type", "application/json")
            .end(ResponseMessages.serverMessage(200, "runApiTest", "runApiTest")
            .encodePrettily())

    }
}
