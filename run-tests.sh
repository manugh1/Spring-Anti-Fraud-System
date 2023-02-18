#!/bin/bash

# Perform automated tests with a mysql database
mysql.server start
./gradlew test
mysql.server stop
