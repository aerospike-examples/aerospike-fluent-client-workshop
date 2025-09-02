package com.aerospike.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.AerospikeException;
import com.aerospike.client.Bin;
import com.aerospike.client.Host;
import com.aerospike.client.Key;
import com.aerospike.client.Operation;
import com.aerospike.client.Record;
import com.aerospike.client.Value;
import com.aerospike.client.cdt.CTX;
import com.aerospike.client.cdt.ListOperation;
import com.aerospike.client.cdt.ListOrder;
import com.aerospike.client.cdt.ListPolicy;
import com.aerospike.client.cdt.ListWriteFlags;
import com.aerospike.client.cdt.MapOperation;
import com.aerospike.client.cdt.MapOrder;
import com.aerospike.client.cdt.MapPolicy;
import com.aerospike.client.cdt.MapReturnType;
import com.aerospike.client.cdt.MapWriteFlags;
import com.aerospike.client.exp.Exp;
import com.aerospike.client.exp.Expression;
import com.aerospike.client.policy.ClientPolicy;
import com.aerospike.client.policy.QueryPolicy;
import com.aerospike.client.policy.WritePolicy;
import com.aerospike.client.query.Filter;
import com.aerospike.client.query.IndexType;
import com.aerospike.client.query.RecordSet;
import com.aerospike.client.query.Statement;
import com.aerospike.config.ClientConfiguration;

/**
 * Old Client implementation of KeyValue operations
 * Uses traditional Aerospike client configuration
 * Active when 'old-client' profile is enabled
 */
@Service
@Profile("old-client")
public class KeyValueServiceOldClient implements KeyValueServiceInterface {

    private static final String NAMESPACE = "test";
    private static final String PRODUCT_SET = "products";
    private static final String CARTS_SET = "shopping_carts";
    private static final String CATEGORY_SET = "cat_index";
    private static final String CATEGORY_KEY = "product_meta";

    private final AerospikeClient aerospikeClient;

    public KeyValueServiceOldClient(ClientConfiguration config) {
        ClientPolicy clientPolicy = new ClientPolicy();
        aerospikeClient = new AerospikeClient(clientPolicy, new Host(config.getHostname(), config.getPort()));
    }

    /**
     * Cleanup method called when the service is destroyed
     * Properly closes the AerospikeClient connection
     */
    @PreDestroy
    public void cleanup() {
        if (aerospikeClient != null) {
            aerospikeClient.close();
        }
    }

    public void clearAllData() {
        aerospikeClient.truncate(null, NAMESPACE, PRODUCT_SET, null);
        aerospikeClient.truncate(null, NAMESPACE, CARTS_SET, null);
        aerospikeClient.delete(null, new Key(NAMESPACE, CATEGORY_SET, CATEGORY_KEY));
    }
    
    /**
     * Key-Value lookup of a specified product
     * Gets the product record and returns the record bins
     * 
     * @param productId Product identifier
     * @return Map containing the product data
     */
    public Optional<Map<String, Object>> getProduct(String productId) {
        Key key = new Key(NAMESPACE, PRODUCT_SET, productId);
        Record record = aerospikeClient.get(null, key);
    
        return Optional.ofNullable(record.bins);
    }

    /**
     * Secondary index query on a specified index and filter
     * Gets the first N records of the secondary index query
     * Returns a list of dictionaries, each containing a record's bins, and the query execution time
     * 
     * @param index Index name to query
     * @param filterValue Filter value to match
     * @param count Maximum number of records to return
     * @return QueryResult containing products list and execution time
     */
    public KeyValueServiceInterface.QueryResult query(String index, String filterValue, int count) {
        long startTime = System.currentTimeMillis();
        
        Statement statement = new Statement();
        statement.setNamespace(NAMESPACE);
        statement.setSetName(PRODUCT_SET);
        statement.setFilter(Filter.equal(index, filterValue));
        statement.setMaxRecords(count);
        
        List<String> binNames = new ArrayList<>();
        binNames.add("id");
        binNames.add("name");
        binNames.add("images");
        binNames.add("brandName");
        statement.setBinNames(binNames.toArray(new String[0]));
        
        QueryPolicy queryPolicy = aerospikeClient.copyQueryPolicyDefault();
        RecordSet recordSet = aerospikeClient.query(queryPolicy, statement);
        
        List<Map<String, Object>> products = new ArrayList<>();
        while (recordSet.next()) {
            Record record = recordSet.getRecord();
            products.add(record.bins);
        }
        
        return new KeyValueServiceInterface.QueryResult(products, System.currentTimeMillis() - startTime);
    }

    /**
     * Get all categories from the category metadata record
     * 
     * @return List of category names
     */
    public List<String> getCategories() {
        Key key = new Key(NAMESPACE, CATEGORY_SET, CATEGORY_KEY);
        Record categories = aerospikeClient.operate(null, key, MapOperation.getByKeyRange("categories", Value.get("A"), Value.get("Z"), MapReturnType.KEY));
        return (List<String>) categories.getList("categories");
    }

    /**
     * Get all article types from the category metadata record
     * 
     * @return List of article type names
     */
    public List<String> getArticleTypes() {
        Key key = new Key(NAMESPACE, CATEGORY_SET, CATEGORY_KEY);
        Record categories = aerospikeClient.get(null, key, "articleTypes");
        return (List<String>) categories.getList("articleTypes");
    }
    
    /**
     * Get all usage types from the category metadata record
     * 
     * @return List of usage names
     */
    public List<String> getUsage() {
        Key key = new Key(NAMESPACE, CATEGORY_SET, CATEGORY_KEY);
        Record categories = aerospikeClient.get(null, key, "usage");
        return ((List<String>) categories.getList("usage"))
                .stream()
                .filter(cat -> !cat.isEmpty() && !cat.equals("NA"))
                .toList();
    }

    /**
     * Get all brand names from the category metadata record
     * 
     * @return List of brand names
     */
    public List<String> getBrandNames() {
        Key key = new Key(NAMESPACE, CATEGORY_SET, CATEGORY_KEY);
        Record categories = aerospikeClient.get(null, key, "brandNames");
        return ((List<String>) categories.getList("brandNames"))
                .stream()
                .filter(brand -> !brand.isEmpty() && !brand.equals("NA"))
                .toList();
    }

    /**
     * Load categories and article types to a meta record
     * 
     * @param category Category name
     * @param subCategory Subcategory name
     * @param articleType Article type
     * @param usage Usage description
     * @param brandName Brand name
     */
    public void loadCategories(String category, String subCategory, String articleType, String usage, String brandName) {
        Key key = new Key(NAMESPACE, CATEGORY_SET, CATEGORY_KEY);
        
        // Define map policy for writing and order
        MapPolicy mapPolicy = new MapPolicy(
            MapOrder.KEY_ORDERED,
            MapWriteFlags.CREATE_ONLY | MapWriteFlags.NO_FAIL
        );
        
        // Define list policy for writing and order
        ListPolicy listPolicy = new ListPolicy(
            ListOrder.ORDERED,
            ListWriteFlags.ADD_UNIQUE | ListWriteFlags.NO_FAIL
        );
        
        // Define the operations to perform on the map and list bins
        Operation[] ops = {
            // Adds new "category" key to the map with an empty map as the value
            MapOperation.put(mapPolicy, "categories", Value.get(category), Value.get(new HashMap<>())),
            
            // Using the category as context, increment the subCategory count
            MapOperation.increment(mapPolicy, "categories", Value.get(subCategory), Value.get(1), 
                CTX.mapKey(Value.get(category))),
            
            // Append items to the "articleTypes", "usage", and "brandNames" lists
            ListOperation.append(listPolicy, "articleTypes", Value.get(articleType)),
            ListOperation.append(listPolicy, "usage", Value.get(usage)),
            ListOperation.append(listPolicy, "brandNames", Value.get(brandName))
        };
        
        // Perform the operations
        aerospikeClient.operate(null, key, ops);
    }

    /**
     * Create a string secondary index on the specified bin
     * Equivalent to the Python create_string_sindex function
     * 
     * @param binName Name of the bin to index
     * @param indexName Name of the index to create
     */
    public void createStringIndex(String binName, String indexName) {
        try {
            aerospikeClient.createIndex(null, NAMESPACE, PRODUCT_SET, indexName, binName, IndexType.STRING);
        } catch (AerospikeException e) {
            // Index already exists or other error - fail gracefully
            System.out.println("Index " + indexName + " already exists or failed to create: " + e.getMessage());
        }
    }

    /**
     * Store a product record in Aerospike
     * 
     * @param product Product data map
     * @param productId Product identifier
     */
    public void storeProduct(Map<String, Object> product, String productId) {
        Key key = new Key(NAMESPACE, PRODUCT_SET, productId);
        WritePolicy writePolicy = aerospikeClient.copyWritePolicyDefault();
        
        aerospikeClient.put(writePolicy, key, getBins(product));
    }

//    /**
//     * Get a specific attribute of a product
//     * Equivalent to the Python get_product_attribute function
//     * 
//     * @param productId Product identifier
//     * @param attribute Name of the attribute to retrieve
//     * @return The attribute value
//     */
    public Object getProductAttribute(String productId, String attribute) {
        Key key = new Key(NAMESPACE, PRODUCT_SET, productId);
        Record record = aerospikeClient.get(null, key, attribute);
        
        if (record != null && record.bins.containsKey(attribute)) {
            return record.bins.get(attribute);
        }
        return null;
    }

    /**
     * Convert a product map to Aerospike bins
     */
    private Bin[] getBins(Map<String, Object> product) {
        List<Bin> bins = new ArrayList<>();
        
        for (Map.Entry<String, Object> entry : product.entrySet()) {
            bins.add(new Bin(entry.getKey(), Value.get(entry.getValue())));
        }
        
        return bins.toArray(new Bin[0]);
    }

    private String asNonNullString(String orig) {
        if (orig == null ) {
            return "";
        }
        return orig;
    }
    
    /**
     * Advanced search with multiple filters
     * 
     * @param category Category filter (optional)
     * @param articleType Article type filter (optional)
     * @param usage Usage filter (optional)
     * @param brandName Brand name filter (optional)
     * @param searchText Text search query (optional)
     * @param count Maximum number of results
     * @return QueryResult containing filtered products
     */
    public KeyValueServiceInterface.QueryResult advancedSearch(String category, String articleType, String usage, String brandName, String searchText, int count) {
        long startTime = System.currentTimeMillis();
        
        Map<String, String> indexes = Map.of(
                "category", asNonNullString(category), 
                "articleType", asNonNullString(articleType), 
                "usage", asNonNullString(usage), 
                "brandName", asNonNullString(brandName));
        
        String indexField = null;
        List<Exp> expParts = new ArrayList<>();
        for (Entry<String, String> entry : indexes.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                if (indexField == null) {
                    indexField = entry.getKey();
                }
                else {
                    expParts.add(Exp.eq(Exp.stringBin(entry.getKey()),Exp.val(entry.getValue())));
                }
            }
        }

        // Server cannot handle "Exp.and()" nor "Exp.and(value1)
        expParts.add(Exp.val(true));
        expParts.add(Exp.val(true));

        Expression filterExp = Exp.build(Exp.and(expParts.toArray(Exp[]::new)));
        Filter filter = indexField == null ? null : Filter.equal(indexField, indexes.get(indexField));

        Statement statement = new Statement();
        statement.setNamespace(NAMESPACE);
        statement.setSetName(PRODUCT_SET);
        statement.setFilter(filter);
        statement.setMaxRecords(count);
        
        List<String> binNames = new ArrayList<>();
        binNames.add("id");
        binNames.add("name");
        binNames.add("images");
        binNames.add("brandName");
        statement.setBinNames(binNames.toArray(new String[0]));
        
        QueryPolicy queryPolicy = aerospikeClient.copyQueryPolicyDefault();
        queryPolicy.filterExp = filterExp;
        
        RecordSet recordSet = aerospikeClient.query(queryPolicy, statement);
        
        List<Map<String, Object>> products = new ArrayList<>();
        while (recordSet.next()) {
            Record record = recordSet.getRecord();
            products.add(record.bins);
        }
        
        long endTime = System.currentTimeMillis() - startTime;
        
        return new KeyValueServiceInterface.QueryResult(products, endTime);
    }

    public int getProductCount() {
        Statement stmt = new Statement();
        stmt.setNamespace(NAMESPACE);
        stmt.setSetName(PRODUCT_SET);
        QueryPolicy qp = aerospikeClient.copyQueryPolicyDefault();
        qp.setIncludeBinData(false);
        
        int count = 0;
        RecordSet results = aerospikeClient.query(qp, stmt);
        while (results.next()) {
            count++;
        }
        return count;
    }

    // Cart operations
    public KeyValueServiceInterface.CartResponse getCart(String userId) {
        try {
            Key key = new Key(NAMESPACE, CARTS_SET, userId);
            Record record = aerospikeClient.get(null, key);
            
            if (record == null) {
                return new KeyValueServiceInterface.CartResponse(new ArrayList<>(), 0.0);
            }
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) record.getValue("items");
            if (items == null) {
                items = new ArrayList<>();
            }
            
            double total = calculateCartTotal(items);
            return new KeyValueServiceInterface.CartResponse(items, total);
        } catch (Exception e) {
            System.err.println("Error getting cart: " + e.getMessage());
            return new KeyValueServiceInterface.CartResponse(new ArrayList<>(), 0.0);
        }
    }

    public KeyValueServiceInterface.CartResponse addToCart(String userId, String productId, int quantity) {
        try {
            // Get product details first
            Optional<Map<String, Object>> productOptional = getProduct(productId);
            
            Map<String, Object> product = productOptional
                    .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

            Key key = new Key(NAMESPACE, CARTS_SET, userId);
            
            // Create cart item
            Map<String, Object> cartItem = new HashMap<>();
            cartItem.put("productId", productId);
            cartItem.put("name", product.get("name"));
            cartItem.put("price", product.get("price"));
            cartItem.put("brandName", product.get("brandName"));
            cartItem.put("quantity", quantity);
            
            // Get product image
            String image = extractProductImage(product);
            if (image != null) {
                cartItem.put("image", image);
            }

            // Check if item already exists in cart
            Record existingRecord = aerospikeClient.get(null, key);
            List<Map<String, Object>> items = new ArrayList<>();
            
            if (existingRecord != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> existingItems = (List<Map<String, Object>>) existingRecord.getValue("items");
                if (existingItems != null) {
                    items = new ArrayList<>(existingItems);
                }
            }

            // Find existing item or add new one
            boolean found = false;
            for (Map<String, Object> item : items) {
                if (productId.equals(item.get("productId"))) {
                    int currentQty = ((Number) item.get("quantity")).intValue();
                    item.put("quantity", currentQty + quantity);
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                items.add(cartItem);
            }

            // Save updated cart
            WritePolicy writePolicy = new WritePolicy();
            Bin itemsBin = new Bin("items", Value.get(items));
            aerospikeClient.put(writePolicy, key, itemsBin);

            double total = calculateCartTotal(items);
            return new KeyValueServiceInterface.CartResponse(items, total);
        } catch (Exception e) {
            System.err.println("Error adding to cart: " + e.getMessage());
            throw new RuntimeException("Failed to add item to cart: " + e.getMessage());
        }
    }

    public KeyValueServiceInterface.CartResponse updateCartItem(String userId, String productId, int quantity) {
        try {
            Key key = new Key(NAMESPACE, CARTS_SET, userId);
            Record record = aerospikeClient.get(null, key);
            
            if (record == null) {
                return new KeyValueServiceInterface.CartResponse(new ArrayList<>(), 0.0);
            }
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) record.getValue("items");
            if (items == null) {
                items = new ArrayList<>();
            }

            // Update or remove item
            items = new ArrayList<>(items); // Make mutable copy
            if (quantity <= 0) {
                items.removeIf(item -> productId.equals(item.get("productId")));
            } else {
                for (Map<String, Object> item : items) {
                    if (productId.equals(item.get("productId"))) {
                        item.put("quantity", quantity);
                        break;
                    }
                }
            }

            // Save updated cart
            WritePolicy writePolicy = new WritePolicy();
            Bin itemsBin = new Bin("items", Value.get(items));
            aerospikeClient.put(writePolicy, key, itemsBin);

            double total = calculateCartTotal(items);
            return new KeyValueServiceInterface.CartResponse(items, total);
        } catch (Exception e) {
            System.err.println("Error updating cart item: " + e.getMessage());
            throw new RuntimeException("Failed to update cart item: " + e.getMessage());
        }
    }

    public KeyValueServiceInterface.CartResponse removeFromCart(String userId, String productId) {
        return updateCartItem(userId, productId, 0);
    }

    public KeyValueServiceInterface.CartResponse clearCart(String userId) {
        try {
            Key key = new Key(NAMESPACE, CARTS_SET, userId);
            WritePolicy writePolicy = new WritePolicy();
            Bin itemsBin = new Bin("items", Value.get(new ArrayList<>()));
            aerospikeClient.put(writePolicy, key, itemsBin);
            
            return new KeyValueServiceInterface.CartResponse(new ArrayList<>(), 0.0);
        } catch (Exception e) {
            System.err.println("Error clearing cart: " + e.getMessage());
            throw new RuntimeException("Failed to clear cart: " + e.getMessage());
        }
    }

    /**
     * Extracts product image URL from the product data structure
     * Tries search/resolutions/125X161 first, then front/resolutions/125X161
     */
    private String extractProductImage(Map<String, Object> product) {
        @SuppressWarnings("unchecked")
        Map<String, Object> images = (Map<String, Object>) product.get("images");
        if (images == null) {
            return null;
        }

        // Try search -> resolutions -> 125X161
        String image = getImageFromPath(images, "search", "resolutions", "125X161");
        if (image != null) {
            return image;
        }

        // Fallback: try front -> resolutions -> 125X161
        return getImageFromPath(images, "front", "resolutions", "125X161");
    }

    /**
     * Helper method to safely navigate nested map structure for image extraction
     */
    @SuppressWarnings("unchecked")
    private String getImageFromPath(Map<String, Object> root, String... path) {
        Map<String, Object> current = root;
        
        // Navigate through all path segments except the last one
        for (int i = 0; i < path.length - 1; i++) {
            Object next = current.get(path[i]);
            if (!(next instanceof Map)) {
                return null;
            }
            current = (Map<String, Object>) next;
        }
        
        // Get the final value
        Object result = current.get(path[path.length - 1]);
        return result instanceof String ? (String) result : null;
    }

    private double calculateCartTotal(List<Map<String, Object>> items) {
        return items.stream()
                .mapToDouble(item -> {
                    Object priceObj = item.get("price");
                    Object quantityObj = item.get("quantity");
                    
                    double price = 0.0;
                    int quantity = 0;
                    
                    if (priceObj instanceof Number) {
                        price = ((Number) priceObj).doubleValue();
                    }
                    
                    if (quantityObj instanceof Number) {
                        quantity = ((Number) quantityObj).intValue();
                    }
                    
                    return price * quantity;
                })
                .sum();
    }
    
} 