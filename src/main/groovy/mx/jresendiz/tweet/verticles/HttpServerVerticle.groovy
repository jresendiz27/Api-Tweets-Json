package mx.jresendiz.tweet.verticles

import groovy.util.logging.Slf4j
import io.vertx.core.AbstractVerticle
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.api.contract.RouterFactoryOptions
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory
import mx.jresendiz.tweet.handlers.ApiTestHandler
import mx.jresendiz.tweet.handlers.HealthHandler
import mx.jresendiz.tweet.handlers.TweetCountHandler
import mx.jresendiz.tweet.handlers.UtilHandlers

@Slf4j
class HttpServerVerticle extends AbstractVerticle {
    void start() {
        JsonObject config = vertx.getOrCreateContext().config()
        String contractUrl = config.getString("contract")
        RouterFactoryOptions options = new RouterFactoryOptions()
        options.setNotImplementedFailureHandler(UtilHandlers.METHOD_NOT_IMPLEMENTED_HANDLER)
        OpenAPI3RouterFactory.create(vertx, contractUrl, { creationHandler ->
            if (creationHandler.succeeded()) {
                OpenAPI3RouterFactory routerFactory = creationHandler.result();
                setUpAPIHandlers(routerFactory)
                deployWebServer(routerFactory.getRouter(), config)
            } else {
                creationHandler.cause().printStackTrace()
            }
        });
    }

    void setUpAPIHandlers(OpenAPI3RouterFactory routerFactory) {
        routerFactory.addHandlerByOperationId("tweetCount", new TweetCountHandler(vertx))
        routerFactory.addHandlerByOperationId("runApiTest", new ApiTestHandler(vertx))
        routerFactory.addHandlerByOperationId("health", new HealthHandler(vertx))
        routerFactory.addHandlerByOperationId("definition", UtilHandlers.API_DEFINITION_HANDLER)
    }

    void deployWebServer(Router router, JsonObject config) {
        JsonObject remoteConfig = config.getJsonObject("server")

        HttpServerOptions httpServerOptions = new HttpServerOptions()
        httpServerOptions.port = remoteConfig.getInteger("port")
        httpServerOptions.host = remoteConfig.getString("host")
        httpServerOptions.logActivity = remoteConfig.getBoolean("logActivity")
        httpServerOptions.reusePort = remoteConfig.getBoolean("reusePort")

        vertx.createHttpServer(httpServerOptions)
            .exceptionHandler(UtilHandlers.EXCEPTION_HANDLER)
            .requestHandler(router.&accept)
            .listen(remoteConfig.getInteger("port"), UtilHandlers.LISTEN_HANDLER)
    }
}
