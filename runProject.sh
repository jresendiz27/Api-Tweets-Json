#!/usr/bin/env bash
clear
echo ">> Cleaning project"
./gradlew clean -q
echo ">> Creating fat jar ..."
./gradlew shadowJar -q
echo ">> Building docker images ..."
docker-compose -f docker-compose.yml build --no-cache
echo ">> Running api-tweet-json ..."
docker-compose -f docker-compose.yml up
