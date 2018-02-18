package mx.jresendiz.tweet.handlers

import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.ext.web.RoutingContext
import mx.jresendiz.tweet.messages.ResponseMessages


class HealthHandler implements Handler<RoutingContext> {

    Vertx vertx

    HealthHandler(Vertx vertxInstance) {
        vertx = vertxInstance
    }
    // TODO implement me ...
    // TODO including server statistics
    // TODO includes TravisCi integration
    @Override
    void handle(RoutingContext routingContext) {
        routingContext.response()
            .putHeader("content-type", "application/json")
            .end(ResponseMessages.serverMessage(200, "health", "health")
            .encodePrettily())
    }
}
