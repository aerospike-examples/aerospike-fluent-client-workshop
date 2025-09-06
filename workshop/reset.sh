#!/bin/bash

cp KeyValueServiceNewClient.java ../spring-server/src/main/java/com/aerospike/service/KeyValueServiceNewClient.java
aql -h localhost:3100 -c "truncate test.shopping_carts"
