package com.fintech.market;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
public class MockDataService {

    private static final Logger logger = LoggerFactory.getLogger(MockDataService.class);
    
    @Value("${mock.data.enabled:true}")
    private boolean mockDataEnabled;
    
    @Value("${mock.data.symbols:AAPL,GOOGL,MSFT,TSLA,AMZN}")
    private String[] symbols;
    
    @Value("${mock.data.interval:1000}")
    private long intervalMs;
    
    @Value("${mock.data.price-range.min:50.0}")
    private double minPrice;
    
    @Value("${mock.data.price-range.max:500.0}")
    private double maxPrice;
    
    private final Random random = new Random();
    private final List<String> symbolList;
    
    public MockDataService() {
        this.symbolList = Arrays.asList(symbols);
    }
    
    /**
     * Generate mock tick data
     * @return JSON string with mock tick data
     */
    public String generateMockTick() {
        String symbol = symbolList.get(random.nextInt(symbolList.size()));
        double price = minPrice + (maxPrice - minPrice) * random.nextDouble();
        long timestamp = System.currentTimeMillis();
        
        return String.format(
            "{\"symbol\":\"%s\",\"price\":%.2f,\"ts\":%d}",
            symbol, price, timestamp
        );
    }
    
    /**
     * Generate mock tick data for specific symbol
     * @param symbol the symbol to generate data for
     * @return JSON string with mock tick data
     */
    public String generateMockTick(String symbol) {
        double price = minPrice + (maxPrice - minPrice) * random.nextDouble();
        long timestamp = System.currentTimeMillis();
        
        return String.format(
            "{\"symbol\":\"%s\",\"price\":%.2f,\"ts\":%d}",
            symbol, price, timestamp
        );
    }
    
    /**
     * Check if mock data is enabled
     * @return true if mock data is enabled
     */
    public boolean isMockDataEnabled() {
        return mockDataEnabled;
    }
    
    /**
     * Get list of mock symbols
     * @return list of symbols
     */
    public List<String> getSymbols() {
        return symbolList;
    }
    
    /**
     * Get mock data interval
     * @return interval in milliseconds
     */
    public long getIntervalMs() {
        return intervalMs;
    }
}
