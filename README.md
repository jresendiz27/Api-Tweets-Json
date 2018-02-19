# Api-Tweets-Json

[![Build Status](https://travis-ci.org/jresendiz27/Api-Tweets-Json.svg?branch=master)](https://travis-ci.org/jresendiz27/Api-Tweets-Json)

Working with json files, geospatial databases and exposing an API

## Project requirements:

* JDK 8 or higher.
* Docker 1.8 or higher.
* Docker Compose 1.16 or higher.

## Compile the project

Use the gradle wrapper inside the repository. Consider the next command.

```bash
./gradlew clean
./gradlew compile
./gradlew tasks
```

The `./gradlew tasks` command, shows all the task related with the project.


## Run the project

Execute the `runProject.sh` bash script inside the repository.

## How does it works?

The project uses [Vert.x](http://vertx.io/) as the main framework for handling the application flow, requests and asynchronous pattern.
 
Also uses [Flyway](https://flywaydb.org/) to migrate the database and keeps the db integrity/incremental process.

The designed api is using [The OpenAPI specification](https://github.com/OAI/OpenAPI-Specification) in the version 3. 
The API definition is stored in other [repository](https://github.com/jresendiz27/Api-Tweets-Config) with the project configuration too.
In case the API detects a change in the configuration, the project reconfigures itself.

Here's the list of all the endpoints, in case you need further information, check the [API's contract](https://github.com/jresendiz27/Api-Tweets-Config/blob/master/api-contract.yml) 

There's an specific endpoint in the API to retrieve the contract, look for `/definition`.

All the API is versioned inside a [DockerFile](./Dockerfile) and handles the other containers using a [Docker Compose](./docker-compose.yml) file to create the links, send environment variables and share volumes with the containers if needed.

Also there's a [groovy script](.src/main/groovy/mx/jresendiz/tweet/scripts/TweetFileParser.groovy) that was used to read the information from the jsonl file provided and extracts the information from the file, creates a sql file to include in the migrations.

## Known Issues

The first time you run the project, it might get stuck because sometimes the web container starts before the database one, so no database connection is alive. The issue solves after the first time.
