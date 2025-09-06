To create the indexes and load the product data into Aerospike:
```
curl -X POST "http://localhost:8080/rest/v1/data/create-indexes"
curl -X POST "http://localhost:8080/rest/v1/data/load?dataPath=`pwd`"
```
