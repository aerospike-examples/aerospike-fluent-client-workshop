# Aerospike Retail Demo

A demo retail website powered by Aerospike, showcasing Key-Value operations with a modern Spring Boot + React architecture.

## Project Structure

```
retail-demo/
├── spring-server/          # Spring Boot backend (Java 21)
├── website/                # React frontend (Vite)
├── external_jars/          # External JAR files (tracked in Git)
├── data/                   # Sample data files
├── config/                 # Configuration files
  ├── aerospike/              # Aerospike configuration
└── .gitignore              # Multi-module gitignore
```

## Technologies

- **Backend**: Spring Boot 3.2, Java 21, Aerospike Client 9.1.0
- **Frontend**: React 18, Vite 5, React Router DOM
- **Database**: Aerospike (Key-Value operations, Secondary Indexes)
- **Build**: Maven (Java), npm/yarn (React)

To run locally:

1. Download the [kaggle fashion dataset](https://www.kaggle.com/datasets/paramaggarwal/fashion-product-images-dataset)
    1. Place the contents of the `/images/` directory in the `data/images/` directory.
    2. Place the contents of the `/styles/` directory in the `data/styles/` directory.
2. Replace the `config/aerospike/features.replace.conf` and `config/vector/features.replace.conf` with a valid Aerospike feature key file.
3. Building the Java application
    First, the `external_jars` must be installed into the local Maven
    ```bash
    cd external_jars
    ./registerJars.sh
    ```
4. __OPTIONAL__ Building the front end (should be built by default, so only do this step if /spring-server/src/main/resources/static/index.html does not exist)
    ```bash
    cd website
    npm install
    npm run build
    ```
5. Build the application: 
    ```bash
    cd spring-server
    mvn clean package -DskipTests
    ```

6. Run the application
    Once this is done, start the Java application against your database 
    ```bash
    java -jar target/retail-demo-spring-1.0.0.jar
    ```

    If you have Aerospike running on a different port (default `3000`) or host (default `localhost`) then you can specify these arguments:
    ```bash
    java -jar target/retail-demo-spring-1.0.0.jar --aerospike.host=10.0.0.1 --aerospike.port=3100
    ```

7. Create the indexes and load the product data into Aerospike:
    ```
    cd data
    curl -X POST "http://localhost:8080/rest/v1/data/create-indexes"
    curl -X POST "http://localhost:8080/rest/v1/data/load?dataPath=`pwd`"
    ```

8. Point a browser at `localhost:8080` and you should be good to go! 
