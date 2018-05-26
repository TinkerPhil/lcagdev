#!/usr/bin/env bash

echo "Standing up greenmail"
docker run -d -t -i \
    --name lcag-mail \
    --network lcag-automation-network \
    -p 3025:3025 \
    -p 3110:3110 \
    -p 3143:3143 \
    -p 3465:3465 \
    -p 3993:3993 \
    -p 3995:3995 \
    greenmail/standalone:1.5.7

echo "Standing up mysql"
docker run -d \
    --name lcag-mysql \
    --network lcag-automation-network \
    -p 4306:3306 \
    -e MYSQL_ROOT_PASSWORD=p@ssword \
    -e MYSQL_DATABASE=mybb \
    -e MYSQL_USER=user \
    -e MYSQL_PASSWORD=p@ssword \
    mysql:latest

echo "standing up ftp server"
docker run -d \
    --name lcag-ftp \
    --network lcag-automation-network \
    -p 11020:20 \
    -p 11021:21 \
    -p 47400-47470:47400-47470 \
    -e FTP_USER=ftpuser \
    -e FTP_PASS=ftppassword \
    -e PASV_ADDRESS=127.0.0.1 \
    bogem/ftp