#!/bin/bash

./standup_all.sh

echo "Running tests..."
mvn test

echo "Removing all running containers"
docker rm -f $(docker ps -a -q)