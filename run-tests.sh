#!/bin/bash

mysql.server start
./gradlew test
mysql.server stop
