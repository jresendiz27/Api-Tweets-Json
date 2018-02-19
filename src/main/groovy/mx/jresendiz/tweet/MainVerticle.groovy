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
import org.flywaydb.core.Flyway

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
            .put("path", "~/.local-config")
            .put("filesets",
            new JsonArray().add(new JsonObject().put("pattern", "*.json"))));

        ConfigStoreOptions environmentStore = new ConfigStoreOptions()
            .setType("env");

        ConfigRetrieverOptions options = new ConfigRetrieverOptions()
            .setScanPeriod(5000)
            .addStore(gitStore)
            .addStore(environmentStore);

        ConfigRetriever retriever = ConfigRetriever.create(vertx, options);

        retriever.getConfig({ jsonConfig ->
            JsonObject remoteConfig = adaptConfiguration((Map) jsonConfig.result())

            deploymentOptions.setConfig(remoteConfig)

            executeDatabaseMigrations(remoteConfig)

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

    void executeDatabaseMigrations(JsonObject remoteConfig) {
        Flyway flyway = new Flyway();
        JsonObject databaseConnection = remoteConfig.getJsonObject("database");
        JsonObject flywayConfiguration = remoteConfig.getJsonObject("flyway")
        String jdbcUrl = "jdbc:postgresql://${databaseConnection.getString("host")}:${databaseConnection.getInteger("port")}/${databaseConnection.getString("database")}"

        flyway.setDataSource(
            jdbcUrl,
            databaseConnection.getString("username"),
            databaseConnection.getString("password"));
        flyway.setBaselineOnMigrate(true)
        flyway.setValidateOnMigrate(true)
        flyway.setLocations(flywayConfiguration.getString("locations"))
        flyway.repair()
        flyway.migrate()
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

    /*
    Parses the configuration from database.host=foo to database { host:foo }
    * */

    private JsonObject adaptConfiguration(Map rawConfiguration) {
        JsonObject parsedConfiguration = new JsonObject()
        parsedConfiguration.put("database", new JsonObject())
        parsedConfiguration.put("travis", new JsonObject())
        parsedConfiguration.put("server", new JsonObject())
        parsedConfiguration.put("flyway", new JsonObject())
        rawConfiguration.each { key, value ->
            List<String> splittedKey = key.split("\\.")
            JsonObject configuration = parsedConfiguration.getJsonObject(splittedKey[0])
            if(configuration) {
                configuration.put(splittedKey[1], value)
            }
        }
        parsedConfiguration.put("contract", rawConfiguration.get("contract"))
        return parsedConfiguration
    }
}
