package mx.jresendiz.tweet.handlers

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.http.HttpServer
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import mx.jresendiz.tweet.enums.StatusCodes
import mx.jresendiz.tweet.messages.ResponseMessages

@Slf4j
@CompileStatic
class UtilHandlers {

    static Handler<RoutingContext> METHOD_NOT_IMPLEMENTED_HANDLER = { handler ->
        HttpServerResponse response = handler.response();

        response.putHeader("content-type", "application/json")
        response.statusCode = StatusCodes.NOT_IMPLEMENTED.value

        response.end(ResponseMessages.notImplemented().encodePrettily())
    }

    static Handler<RoutingContext> API_DEFINITION_HANDLER = { routingContext ->
        JsonObject config = routingContext.vertx().orCreateContext.config()
        String contractContent = new URL(config.getString("contract")).text
        routingContext.response()
            .putHeader("content-type", "text/plain")
            .end(contractContent)
    }

    static Handler<Throwable> EXCEPTION_HANDLER = { handler ->
        handler.printStackTrace()
    }

    static Handler<AsyncResult<HttpServer>> LISTEN_HANDLER = { handler ->
        if (handler.succeeded())
            log.info "Server deployed at {}", handler.result().actualPort()
    }
}
