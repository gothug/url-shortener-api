URL shortener API
=================
Users are stored on another server, so while auth token is requested providing
the correct secret, API should always return token. If there's no one, create
it.

Build jar file
--------------

    sbt dist

Setup
-----
For the API to work you need to have a running postresql server on the same
machine on which the API service will be run.

You need also to create urlapi DB and urlapi_user user:

    CREATE DATABASE urlapi;
    CREATE USER urlapi_user WITH password 'qwerty';
    GRANT ALL privileges ON DATABASE urlapi TO urlapi_user;

Tear down
---------
    DROP DATABASE urlapi;
    DROP USER urlapi_user;

Run
---
To run service, unpack the built .zip file, cd to it and run:

    sh bin/url-shortener -DapplyEvolutions.default=true

Test API requests
-----------------
Authorize:

    curl "http://localhost:9000/token?userid=911&secret=AqDKS7EKnE5wq820"
    echo 'X-Auth-Token: ecotUjgKCLIORmSL-HDjDx25zk1MkHoP' > token.header

Make short url:

    curl -H "Content-Type: application/json" \
    -d '{"url":"www://google.com/taxi"}' \
    -H "$(cat token.header)" \
    "http://localhost:9000/link"

Make click:

    curl -H "Content-Type: application/json" \
    -d '{"referer":"www://ya.ru/images", "remote_ip": "73.23.123.42"}' \
    -H "$(cat token.header)" \
    "http://localhost:9000/link/L"

Get link info:

    curl -H "$(cat token.header)" "http://localhost:9000/link/L"
    curl -H "$(cat token.header)" "http://localhost:9000/link/E"
    curl -H "$(cat token.header)" "http://localhost:9000/link/FFFFFFFF"

Get links:

    curl -H "$(cat token.header)" "http://localhost:9000/link"
    curl -H "$(cat token.header)" "http://localhost:9000/link?limit=3&offset=0"

Get clicks:

    curl -H "$(cat token.header)" "http://localhost:9000/link/L/clicks"
    curl -H "$(cat token.header)" "http://localhost:9000/link/L/clicks?limit=30&offset=0"
