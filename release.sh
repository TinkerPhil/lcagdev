#!/usr/bin/env bash

docker network create lcag-automation-network || true && ./mvnw clean verify && docker push dockernovinet/lcag-automation