#!/usr/bin/env bash

./standup_all.sh

echo "Running tests..."
./mvnw test
ERROR_CODE=$?

echo "Removing all running containers"

$(dirname -- "$(readlink -f -- "${BASH_SOURCE[0]}")")/remove_all.sh

exit $ERROR_CODE