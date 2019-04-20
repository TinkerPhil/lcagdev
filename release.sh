#!/usr/bin/env bash

function rmContainer {
    CONTAINER_NAME=$1
    docker rm -f $(docker ps -a | grep $CONTAINER_NAME | cut -c 1-12)
}


rmContainer lcag-application ; \
    rmContainer lcag-sftp ; \
    rmContainer lcag-mysql ; \
    rmContainer lcag-mail ; \
    docker network create lcag-automation-network || true && \
    ./mvnw clean verify && \
    docker push dockernovinet/lcag-automation