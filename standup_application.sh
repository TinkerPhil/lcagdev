#!/usr/bin/env bash

echo "Standing up lcag automation application"
docker run -d \
    -e "SPRING_PROFILES_ACTIVE=prod" \
    -e "DASHBOARD_USERNAME=admin" \
    -e "DASHBOARD_PASSWORD=lcag" \
    -e "RETRIEVE_MAIL_INITIAL_DELAY_MILLISECONDS=1000" \
    -e "RETRIEVE_MAIL_REFRESH_INTERVAL_MILLISECONDS=1000" \
	-e "SMTP_HOST=lcag-mail" \
	-e "SMTP_PORT=3025" \
	-e "SMTP_USERNAME=lcag-testing@lcag.com" \
	-e 'SMTP_PASSWORD=password' \
	-e "IMAP_HOST=lcag-mail" \
	-e "IMAP_PORT=3143" \
	-e "IMAP_USERNAME=lcag-testing@lcag.com" \
	-e 'IMAP_PASSWORD=password' \
	-e 'IMAP_CONNECTION_PROTOCOL=imap' \
	-e "MYBB_FORUM_DATABASE_URL=jdbc:mysql://lcag-mysql/mybb" \
	-e "MYBB_FORUM_DATABASE_USERNAME=root" \
	-e "MYBB_FORUM_DATABASE_PASSWORD=p@ssword" \
	-e "EMAIL_SOURCE_URL=https://docs.google.com/document/d/1MKM84drgdaWRKWQo0HE-BV-NhZfzQH_cBF2wXvbsd4I/export?format=html" \
	-e "EMAIL_ATTACHMENT_ID=14OA5QRGYYdKaKSmUTorEaY8h1hwu5t7j" \
	-e "EMAIL_PROCESSED_FOLDER_NAME=History" \
	-e "BCC_RECIPIENTS=test@bcc.com" \
	-e "EMAIL_FROM_NAME=LCAG" \
	-e "EMAIL_SUBJECT=LCAG Enquiry" \
	-e "VIRTUAL_PORT=8282" \
	-e "SERVER_PORT=8282" \
	-e JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,address=5005,server=y,suspend=n" \
	--name lcag-application \
    --network lcag-automation-network \
    -p 8282:8282 -p 5005:5005 \
    -t dockernovinet/lcag-automation

echo "Waiting for application status url to respond with 200"
while [[ "$(curl -s -o /dev/null -w ''%{http_code}'' localhost:8282/status)" != "200" ]]; do sleep 5; done