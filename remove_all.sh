#!/usr/bin/env bash

function rmContainer {
    CONTAINER_NAME=$1
    docker rm -f $(docker ps -a | grep $CONTAINER_NAME | cut -c 1-12)
}

echo "Removing all running containers"
rmContainer lcag-dashboard ; \
    rmContainer lcag-sftp ; \
    rmContainer lcag-mysql ; \
    rmContainer lcag-mail