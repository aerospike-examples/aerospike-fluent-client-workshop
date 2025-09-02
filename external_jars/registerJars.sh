#!/bin/bash

mvn install:install-file \
  -Dfile=aerospike-fluent-client-0.8.0-jar-with-dependencies.jar \
  -DgroupId=com.aerospike \
  -DartifactId=aerospike-fluent-client \
  -Dversion=0.8.0 \
  -Dpackaging=jar
