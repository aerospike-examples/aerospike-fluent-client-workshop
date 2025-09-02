# Java KeyValue Implementation

This directory contains the Java equivalent of the Python `key_value.py` module from the retail demo.

## Overview

The `KeyValue` class provides the same functionality as the Python module:
- Loading categories and article types into Aerospike
- Creating string secondary indexes
- Retrieving products and product attributes

## Files

- `src/main/java/com/aerospike/KeyValue.java` - Main Java class with all functionality
- `src/test/java/com/aerospike/KeyValueTest.java` - Example usage and test class
- `pom.xml` - Maven configuration with Aerospike Java client dependency

## Dependencies

The project uses Maven to manage dependencies:
- **Aerospike Java Client** (version 6.1.0) - For connecting to and operating on Aerospike
- **JUnit Jupiter** (version 5.9.2) - For testing (test scope)

## Building the Project

```bash
cd graph
mvn clean compile
```

## Running Tests

```bash
mvn test
```

## Usage Example

```java
import com.aerospike.client.AerospikeClient;
import com.aerospike.client.Host;
import com.aerospike.client.policy.ClientPolicy;
import com.aerospike.KeyValue;

// Create client
ClientPolicy clientPolicy = new ClientPolicy();
AerospikeClient client = new AerospikeClient(clientPolicy, new Host("localhost", 3000));

// Load categories
KeyValue.loadCategories(client, "Clothing", "Shirts", "Casual", "Everyday wear");

// Create indexes
KeyValue.createStringSindex(client, "category", "idx_category");

// Get product
Map<String, Object> product = KeyValue.getProduct(client, "product_123");

// Get specific attribute
String category = KeyValue.getProductAttribute(client, "product_123", "category");

// Close client
client.close();
```

## Key Differences from Python Version

1. **Static Methods**: All methods are static, similar to the Python module functions
2. **Exception Handling**: Uses Java exception handling patterns
3. **Type Safety**: Leverages Java's type system for better compile-time safety
4. **Documentation**: Uses JavaDoc for comprehensive documentation
5. **Null Safety**: Includes null checks and proper error handling

## Configuration

The class uses the same constants as the Python version:
- **Namespace**: `retail-vector`
- **Set Name**: `products`
- **Category Index Set**: `cat_index`

## Error Handling

The Java implementation includes proper error handling:
- Graceful handling of existing indexes in `createStringSindex`
- Null checks for product retrieval
- Proper client resource management

## Compatibility

This Java implementation is designed to work with the same Aerospike database schema as the Python version, ensuring seamless integration with existing data. 