#!/usr/bin/env bash

DASHBOARD_HOST=${1:-"localhost"}
DASHBOARD_PORT=${2:-"8282"}

./standup_supporting_containers.sh

./standup_application.sh ${DASHBOARD_HOST} ${DASHBOARD_PORT}

echo "Waiting for application status url to respond with 200. Status url: http://${DASHBOARD_HOST}:${DASHBOARD_PORT}/status"
while [[ "$(curl -s -o /dev/null -w ''%{http_code}'' ${DASHBOARD_HOST}:${DASHBOARD_PORT}/status)" != "200" ]]; do sleep 5; done