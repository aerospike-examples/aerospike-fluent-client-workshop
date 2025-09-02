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
3. Building the front end (should be built by default)
    ```bash
    cd website
    npm install
    npm run build
    ```
4. Building the Java application
    First, the `external_jars` must be installed into the local Maven
3. Create the containers:
    ```bash
    DOCKER_BUILDKIT=0 docker-compose up -d # using docker-compose standalone
    ```
    or
    ```bash
    DOCKER_BUILDKIT=0 docker compose up -d # using docker 
    ```
4. Load data into the database:
    ```bash
    docker exec -it -w /server aerospike-client python3 load_data.py
    ```
    >**Note**
    >
    >This will take some time. It's loading ~44,000 fashion items, creating an embedding for each image, and 100,000 customer profiles 
5. Access the site at http://localhost:8080
