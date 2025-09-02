package com.aerospike;

import com.aerospike.client.*;
import com.aerospike.client.cdt.*;
import com.aerospike.client.policy.WritePolicy;
import com.aerospike.client.policy.MapPolicy;
import com.aerospike.client.policy.ListPolicy;

import java.util.HashMap;
import java.util.Map;

/**
 * Java equivalent of the Python key_value.py module
 * Provides functionality for loading categories and managing product data in Aerospike
 */
public class KeyValue {
    
    private static final String NAMESPACE = "retail-vector";
    private static final String SET_NAME = "products";
    
    /**
     * Add categories and article types to a meta record
     * 
     * @param client Aerospike client instance
     * @param cat Category name
     * @param subCat Subcategory name
     * @param artType Article type
     * @param usage Usage description
     */
    public static void loadCategories(IAerospikeClient client, String cat, String subCat, String artType, String usage) {
        // Create Aerospike key
        // Key consists of namespace, set, and user-defined key
        Key key = new Key(NAMESPACE, "cat_index", "product_meta");
        
        // Define map policy for writing and order
        // Only creates a key if it does not exist
        // Fails gracefully if the key does exist
        // Maintains map order by key
        MapPolicy mapPolicy = new MapPolicy(
            MapWriteFlags.CREATE_ONLY | MapWriteFlags.NO_FAIL,
            MapOrder.KEY_ORDERED
        );
        
        // Define list policy for writing and order
        // Only appends a value if it does not exist
        // Fails gracefully if the value does exist
        // Maintains list order by value
        ListPolicy listPolicy = new ListPolicy(
            ListWriteFlags.ADD_UNIQUE | ListWriteFlags.NO_FAIL,
            ListOrder.ORDERED
        );
        
        // Define the operations to perform on the map and list bins
        Operation[] ops = {
            // Adds new "category" key to the map with an empty map as the value
            // Policy dictates it will only add the key if it does not exist
            MapOperation.put("categories", Value.get(cat), Value.get(new HashMap<>()), mapPolicy),
            
            // Using the category as context, this finds the "subCategory" key in the "category" map
            // and increments its value by 1. If the "subCategory" key doesn't exist, it creates it
            // initializing it with a value of 1
            MapOperation.increment("categories", Value.get(subCat), Value.get(1), 
                CTX.mapKey(Value.get(cat))),
            
            // These both append items to the "articleTypes" and "usage" lists
            // Policy dictates these will only append if the value doesn't exist
            ListOperation.append("articleTypes", Value.get(artType), listPolicy),
            ListOperation.append("usage", Value.get(usage), listPolicy)
        };
        
        // Perform the operations
        client.operate(null, key, ops);
    }
    
    /**
     * Creates a string secondary index on the specified bin
     * Returns if it already exists
     * 
     * @param client Aerospike client instance
     * @param binName Name of the bin to index
     * @param indexName Name of the index to create
     */
    public static void createStringSindex(IAerospikeClient client, String binName, String indexName) {
        try {
            client.createIndex(null, NAMESPACE, SET_NAME, indexName, binName, IndexType.STRING);
        } catch (AerospikeException e) {
            // Index already exists or other error - return gracefully
            return;
        }
    }
    
    /**
     * Get a product by its ID
     * 
     * @param client Aerospike client instance
     * @param productId Product identifier
     * @return Map containing the product data
     */
    public static Map<String, Object> getProduct(IAerospikeClient client, String productId) {
        Key key = new Key(NAMESPACE, SET_NAME, productId);
        Record record = client.get(null, key);
        
        if (record != null) {
            return record.bins;
        }
        return null;
    }
    
    /**
     * Get a specific attribute of a product
     * 
     * @param client Aerospike client instance
     * @param productId Product identifier
     * @param attribute Name of the attribute to retrieve
     * @return The attribute value as a string
     */
    public static String getProductAttribute(IAerospikeClient client, String productId, String attribute) {
        Key key = new Key(NAMESPACE, SET_NAME, productId);
        Record record = client.get(null, key, attribute);
        
        if (record != null && record.bins.containsKey(attribute)) {
            Object value = record.bins.get(attribute);
            return value != null ? value.toString() : null;
        }
        return null;
    }
} 