package com.fintech.portfolio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class PriceCacheService {

    private static final Logger logger = LoggerFactory.getLogger(PriceCacheService.class);
    
    // In-memory cache for current prices
    // REVIEW: caching vs direct lookup - consider implementing Redis or database cache
    private final Map<String, Double> priceCache = new ConcurrentHashMap<>();
    
    // Mock prices for testing
    private final Map<String, Double> mockPrices = Map.of(
        "AAPL", 150.25,
        "GOOGL", 2800.50,
        "MSFT", 300.75,
        "TSLA", 250.00,
        "AMZN", 3200.00,
        "BTC-USD", 45000.00,
        "ETH-USD", 3000.00
    );

    /**
     * Get current price for a symbol
     * @param symbol the symbol to get price for
     * @return current price or null if not found
     */
    public Double getCurrentPrice(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            logger.warn("Symbol is null or empty");
            return null;
        }
        
        // First check cache
        Double cachedPrice = priceCache.get(symbol.toUpperCase());
        if (cachedPrice != null) {
            logger.debug("Retrieved price from cache for symbol: {} = {}", symbol, cachedPrice);
            return cachedPrice;
        }
        
        // Fallback to mock prices for development
        Double mockPrice = mockPrices.get(symbol.toUpperCase());
        if (mockPrice != null) {
            logger.debug("Retrieved mock price for symbol: {} = {}", symbol, mockPrice);
            // Cache the mock price
            priceCache.put(symbol.toUpperCase(), mockPrice);
            return mockPrice;
        }
        
        logger.warn("No price found for symbol: {}", symbol);
        return null;
    }

    /**
     * Update price in cache
     * @param symbol the symbol
     * @param price the new price
     */
    public void updatePrice(String symbol, Double price) {
        if (symbol == null || symbol.trim().isEmpty()) {
            logger.warn("Cannot update price: symbol is null or empty");
            return;
        }
        
        if (price == null || price <= 0) {
            logger.warn("Cannot update price: price is null or non-positive for symbol: {}", symbol);
            return;
        }
        
        priceCache.put(symbol.toUpperCase(), price);
        logger.debug("Updated price in cache for symbol: {} = {}", symbol, price);
    }

    /**
     * Remove price from cache
     * @param symbol the symbol to remove
     */
    public void removePrice(String symbol) {
        if (symbol != null) {
            priceCache.remove(symbol.toUpperCase());
            logger.debug("Removed price from cache for symbol: {}", symbol);
        }
    }

    /**
     * Clear all cached prices
     */
    public void clearCache() {
        priceCache.clear();
        logger.info("Cleared all cached prices");
    }

    /**
     * Get cache size
     * @return number of cached prices
     */
    public int getCacheSize() {
        return priceCache.size();
    }

    /**
     * Check if price is cached
     * @param symbol the symbol to check
     * @return true if cached, false otherwise
     */
    public boolean isPriceCached(String symbol) {
        return symbol != null && priceCache.containsKey(symbol.toUpperCase());
    }

    /**
     * Get all cached symbols
     * @return set of cached symbols
     */
    public java.util.Set<String> getCachedSymbols() {
        return priceCache.keySet();
    }

    /**
     * Batch update prices
     * @param prices map of symbol to price
     */
    public void updatePrices(Map<String, Double> prices) {
        if (prices == null || prices.isEmpty()) {
            return;
        }
        
        prices.forEach((symbol, price) -> {
            if (symbol != null && price != null && price > 0) {
                priceCache.put(symbol.toUpperCase(), price);
            }
        });
        
        logger.debug("Batch updated {} prices in cache", prices.size());
    }

    /**
     * Get price with fallback to default value
     * @param symbol the symbol
     * @param defaultValue default value if price not found
     * @return current price or default value
     */
    public Double getCurrentPriceOrDefault(String symbol, Double defaultValue) {
        Double price = getCurrentPrice(symbol);
        return price != null ? price : defaultValue;
    }
}
