package com.aerospike.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Interface for KeyValue operations
 * Allows multiple implementations to be swapped via Spring profiles
 */
public interface KeyValueServiceInterface {

    void clearAllData();

    /**
     * Key-Value lookup of a specified product
     * @param productId Product identifier
     * @return Optional containing the product data
     */
    Optional<Map<String, Object>> getProduct(String productId);

    /**
     * Performs a secondary index query on the specified index using the provided filter value
     * @param index Index name to query
     * @param filterValue Value to filter on
     * @param count Maximum number of results to return
     * @return QueryResult containing products and execution time
     */
    QueryResult query(String index, String filterValue, int count);

    /**
     * Loads categories, subcategories, article types, usage, and brand names into metadata
     * @param category Category name
     * @param subCategory Subcategory name
     * @param articleType Article type
     * @param usage Usage type
     * @param brandName Brand name
     */
    void loadCategories(String category, String subCategory, String articleType, String usage, String brandName);

    /**
     * Creates a secondary index on a string bin
     * @param binName Name of the bin to index
     * @param indexName Name of the index to create
     */
    void createStringIndex(String binName, String indexName);

    /**
     * Stores a product record in Aerospike
     * @param product Product data
     * @param productId Product identifier
     */
    void storeProduct(Map<String, Object> product, String productId);

    /**
     * Gets a specific attribute from a product
     * @param productId Product identifier
     * @param attribute Attribute name
     * @return Attribute value
     */
    Object getProductAttribute(String productId, String attribute);

    /**
     * Gets all available categories
     * @return List of category names
     */
    List<String> getCategories();

    /**
     * Gets all available article types
     * @return List of article type names
     */
    List<String> getArticleTypes();

    /**
     * Gets all available usage types
     * @return List of usage names
     */
    List<String> getUsage();

    /**
     * Gets all available brand names
     * @return List of brand names
     */
    List<String> getBrandNames();

    /**
     * Performs advanced search with multiple filters
     * @param category Category filter
     * @param articleType Article type filter
     * @param usage Usage filter
     * @param brandName Brand name filter
     * @param searchText Text search filter
     * @param count Maximum number of results
     * @return QueryResult containing matching products
     */
    QueryResult advancedSearch(String category, String articleType, String usage, String brandName, String searchText, int count);

    /**
     * Gets the total number of products in the database
     * @return Total product count
     */
    int getProductCount();

    // Cart operations
    /**
     * Gets the shopping cart for a user
     * @param userId User identifier
     * @return CartResponse containing cart items and total
     */
    CartResponse getCart(String userId);

    /**
     * Adds an item to the shopping cart
     * @param userId User identifier
     * @param productId Product identifier
     * @param quantity Quantity to add
     * @return CartResponse with updated cart
     */
    CartResponse addToCart(String userId, String productId, int quantity);

    /**
     * Updates the quantity of an item in the cart
     * @param userId User identifier
     * @param productId Product identifier
     * @param quantity New quantity
     * @return CartResponse with updated cart
     */
    CartResponse updateCartItem(String userId, String productId, int quantity);

    /**
     * Removes an item from the cart
     * @param userId User identifier
     * @param productId Product identifier
     * @return CartResponse with updated cart
     */
    CartResponse removeFromCart(String userId, String productId);

    /**
     * Clears all items from the cart
     * @param userId User identifier
     * @return CartResponse with empty cart
     */
    CartResponse clearCart(String userId);

    /**
     * Result wrapper for query operations
     */
    class QueryResult {
        private final List<Map<String, Object>> products;
        private final Long timeMs;

        public QueryResult(List<Map<String, Object>> products, Long timeMs) {
            this.products = products;
            this.timeMs = timeMs;
        }

        public List<Map<String, Object>> getProducts() {
            return products;
        }

        public Long getTimeMs() {
            return timeMs;
        }
    }

    /**
     * Response wrapper for cart operations
     */
    class CartResponse {
        private final List<Map<String, Object>> items;
        private final double total;

        public CartResponse(List<Map<String, Object>> items, double total) {
            this.items = items;
            this.total = total;
        }

        public List<Map<String, Object>> getItems() {
            return items;
        }

        public double getTotal() {
            return total;
        }

        public int getItemCount() {
            return items.stream().mapToInt(item -> {
                Object quantityObj = item.get("quantity");
                return quantityObj instanceof Number ? ((Number) quantityObj).intValue() : 0;
            }).sum();
        }
    }
}
