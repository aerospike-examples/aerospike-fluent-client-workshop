package com.aerospike;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.AerospikeException;
import com.aerospike.client.Host;
import com.aerospike.client.policy.ClientPolicy;

import java.util.Map;

/**
 * Example usage of the KeyValue class
 * Demonstrates how to use the Java equivalent of the Python key_value.py module
 */
public class KeyValueTest {
    
    public static void main(String[] args) {
        // Example configuration - adjust as needed for your environment
        String host = "localhost";
        int port = 3000;
        
        // Create Aerospike client
        ClientPolicy clientPolicy = new ClientPolicy();
        AerospikeClient client = new AerospikeClient(clientPolicy, new Host(host, port));
        
        try {
            // Example 1: Load categories
            System.out.println("Loading categories...");
            KeyValue.loadCategories(client, "Clothing", "Shirts", "Casual", "Everyday wear");
            KeyValue.loadCategories(client, "Clothing", "Pants", "Formal", "Business attire");
            System.out.println("Categories loaded successfully");
            
            // Example 2: Create string indexes
            System.out.println("Creating indexes...");
            KeyValue.createStringSindex(client, "category", "idx_category");
            KeyValue.createStringSindex(client, "brand", "idx_brand");
            System.out.println("Indexes created successfully");
            
            // Example 3: Get product (assuming product exists)
            System.out.println("Retrieving product...");
            Map<String, Object> product = KeyValue.getProduct(client, "product_123");
            if (product != null) {
                System.out.println("Product found: " + product);
            } else {
                System.out.println("Product not found");
            }
            
            // Example 4: Get specific product attribute
            System.out.println("Retrieving product attribute...");
            String category = KeyValue.getProductAttribute(client, "product_123", "category");
            if (category != null) {
                System.out.println("Product category: " + category);
            } else {
                System.out.println("Product category not found");
            }
            
        } catch (AerospikeException e) {
            System.err.println("Aerospike error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close the client
            client.close();
        }
    }
} 