package mx.jresendiz.tweet

import groovy.util.logging.Slf4j
import io.vertx.config.ConfigRetriever
import io.vertx.config.ConfigRetrieverOptions
import io.vertx.config.ConfigStoreOptions
import io.vertx.core.AbstractVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import mx.jresendiz.tweet.handlers.UtilHandlers
import mx.jresendiz.tweet.messages.LoggingMessages

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

@Slf4j
class MainVerticle extends AbstractVerticle {
    ConcurrentMap<String, Boolean> webVerticlesIds = new ConcurrentHashMap<>()

    void start() throws Exception {
        setupVertx()

        DeploymentOptions deploymentOptions = new DeploymentOptions()
        deploymentOptions.setInstances(2)
        deploymentOptions.setWorker(false)

        ConfigStoreOptions gitStore = new ConfigStoreOptions()
            .setType("git")
            .setConfig(new JsonObject()
            .put("url", "https://github.com/jresendiz27/Api-Tweets-Config.git")
            .put("path", "build")
            .put("filesets",
            new JsonArray().add(new JsonObject().put("pattern", "*.json"))));

        ConfigRetrieverOptions options = new ConfigRetrieverOptions()
            .setScanPeriod(5000)
            .addStore(gitStore);

        ConfigRetriever retriever = ConfigRetriever.create(vertx, options);

        retriever.getConfig({ jsonConfig ->
            JsonObject jsonObject = jsonConfig.result()
            deploymentOptions.setConfig(jsonObject)
            deployWebVerticles(deploymentOptions)
        })

        retriever.listen({ listener ->
            if (listener) {
                JsonObject jsonObject = listener.newConfiguration as JsonObject
                undeployAllVerticles()
                deploymentOptions.setConfig(jsonObject)
                deployWebVerticles(deploymentOptions)
            }
        })
    }

    private void setupVertx() {
        System.setProperty("vertx.logger-delegate-factory-class-name",
            "io.vertx.core.logging.SLF4JLogDelegateFactory");
        vertx.exceptionHandler(UtilHandlers.EXCEPTION_HANDLER)
    }

    private void undeployAllVerticles() {
        webVerticlesIds.each { key, value ->
            vertx.undeploy(key)
            log.info(LoggingMessages.VERTICLE_UNDEPLOYED.replace("@VerticleId", key))
        }
        webVerticlesIds.clear()
    }

    private void deployWebVerticles(DeploymentOptions deploymentOptions) {
        vertx.deployVerticle("groovy:mx.jresendiz.tweet.verticles.HttpServerVerticle", deploymentOptions, { handler ->
            if (handler.succeeded()) {
                String verticleId = handler.result()
                webVerticlesIds.put(verticleId, true)
                log.info(LoggingMessages.VERTICLE_DEPLOYED.replace("@VerticleId", verticleId))
            }
        })
    }
}
