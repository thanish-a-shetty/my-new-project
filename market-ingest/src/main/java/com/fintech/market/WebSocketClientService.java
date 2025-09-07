package com.fintech.market;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class WebSocketClientService implements WebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketClientService.class);
    
    @Autowired
    private PricePublisher pricePublisher;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private MockDataService mockDataService;
    
    @Value("${market.ws.url}")
    private String marketWsUrl;
    
    @Value("${market.ws.key:}")
    private String marketWsKey;
    
    private WebSocketSession webSocketSession;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final ConcurrentHashMap<String, Long> lastPublishTime = new ConcurrentHashMap<>();
    private final long RATE_LIMIT_MS = 100; // 100ms throttle per symbol
    
    // Adapter pattern implementation
    private MarketAdapter marketAdapter;
    
    public WebSocketClientService() {
        // Initialize with mock adapter by default
        this.marketAdapter = new MockMarketAdapter();
    }
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("WebSocket connection established: {}", session.getId());
        this.webSocketSession = session;
        
        // Initialize adapter based on URL
        initializeAdapter();
        
        // Start heartbeat/ping mechanism
        startHeartbeat();
    }
    
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage) {
            String payload = ((TextMessage) message).getPayload();
            logger.debug("Received message: {}", payload);
            
            // Parse and publish tick data
            parseAndPublishTick(payload);
        }
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("WebSocket transport error: {}", exception.getMessage(), exception);
        scheduleReconnection();
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        logger.warn("WebSocket connection closed: {} - {}", closeStatus.getCode(), closeStatus.getReason());
        scheduleReconnection();
    }
    
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
    
    /**
     * Initialize market adapter based on URL configuration
     */
    private void initializeAdapter() {
        if (marketWsUrl.startsWith("mock://")) {
            logger.info("Using MockMarketAdapter for local testing");
            this.marketAdapter = new MockMarketAdapter();
        } else {
            logger.info("Using ProviderMarketAdapter for live data");
            this.marketAdapter = new ProviderMarketAdapter(marketWsKey);
        }
    }
    
    /**
     * Parse tick data and publish to STOMP topic
     * @param tickData JSON tick data
     */
    public void parseTick(String tickData) {
        try {
            JsonNode tick = objectMapper.readTree(tickData);
            
            String symbol = tick.get("symbol").asText();
            double price = tick.get("price").asDouble();
            long timestamp = tick.get("ts").asLong();
            
            // Rate limiting per symbol
            if (isRateLimited(symbol)) {
                logger.debug("Rate limited for symbol: {}", symbol);
                return;
            }
            
            // Create price data object
            PriceData priceData = new PriceData(symbol, price, timestamp);
            
            // Publish via STOMP
            pricePublisher.publishPrice(priceData);
            
            // Update rate limit timestamp
            lastPublishTime.put(symbol, System.currentTimeMillis());
            
            logger.debug("Published price for {}: {}", symbol, price);
            
        } catch (Exception e) {
            logger.error("Error parsing tick data: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Parse and publish tick data (internal method)
     */
    private void parseAndPublishTick(String tickData) {
        parseTick(tickData);
    }
    
    /**
     * Check if symbol is rate limited
     */
    private boolean isRateLimited(String symbol) {
        Long lastTime = lastPublishTime.get(symbol);
        if (lastTime == null) {
            return false;
        }
        return (System.currentTimeMillis() - lastTime) < RATE_LIMIT_MS;
    }
    
    /**
     * Start heartbeat mechanism
     */
    private void startHeartbeat() {
        scheduler.scheduleAtFixedRate(() -> {
            if (webSocketSession != null && webSocketSession.isOpen()) {
                try {
                    webSocketSession.sendMessage(new TextMessage("ping"));
                } catch (IOException e) {
                    logger.error("Error sending heartbeat: {}", e.getMessage());
                }
            }
        }, 30, 30, TimeUnit.SECONDS);
    }
    
    /**
     * Schedule reconnection with exponential backoff
     */
    private void scheduleReconnection() {
        scheduler.schedule(() -> {
            try {
                logger.info("Attempting to reconnect to market data feed...");
                connectToMarketData();
            } catch (Exception e) {
                logger.error("Reconnection failed: {}", e.getMessage());
                // Schedule another reconnection attempt
                scheduleReconnection();
            }
        }, 5, TimeUnit.SECONDS);
    }
    
    /**
     * Connect to market data WebSocket
     */
    public void connectToMarketData() throws Exception {
        if (marketWsUrl.startsWith("mock://")) {
            logger.info("Mock mode - starting mock data generation");
            startMockDataGeneration();
            return;
        }
        
        WebSocketClient client = new StandardWebSocketClient();
        URI uri = new URI(marketWsUrl);
        
        // Add authentication headers if key is provided
        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        if (marketWsKey != null && !marketWsKey.isEmpty()) {
            headers.add("Authorization", "Bearer " + marketWsKey);
        }
        
        client.doHandshake(this, headers, uri).get();
    }
    
    /**
     * Start mock data generation for testing
     */
    private void startMockDataGeneration() {
        if (!mockDataService.isMockDataEnabled()) {
            logger.info("Mock data generation is disabled");
            return;
        }
        
        scheduler.scheduleAtFixedRate(() -> {
            try {
                String mockTick = mockDataService.generateMockTick();
                parseAndPublishTick(mockTick);
            } catch (Exception e) {
                logger.error("Error generating mock tick: {}", e.getMessage());
            }
        }, 0, mockDataService.getIntervalMs(), TimeUnit.MILLISECONDS);
        
        logger.info("Started mock data generation with interval: {}ms", mockDataService.getIntervalMs());
    }
    
    /**
     * Disconnect from market data
     */
    public void disconnect() {
        if (webSocketSession != null && webSocketSession.isOpen()) {
            try {
                webSocketSession.close();
            } catch (IOException e) {
                logger.error("Error closing WebSocket connection: {}", e.getMessage());
            }
        }
    }
    
    /**
     * Market adapter interface
     */
    public interface MarketAdapter {
        void authenticate();
        String getConnectionUrl();
    }
    
    /**
     * Mock market adapter for local testing
     */
    public static class MockMarketAdapter implements MarketAdapter {
        @Override
        public void authenticate() {
            // No authentication needed for mock
        }
        
        @Override
        public String getConnectionUrl() {
            return "mock://localhost";
        }
    }
    
    /**
     * Provider market adapter for live data
     * TODO: Add provider-specific authentication
     */
    public static class ProviderMarketAdapter implements MarketAdapter {
        private final String apiKey;
        
        public ProviderMarketAdapter(String apiKey) {
            this.apiKey = apiKey;
        }
        
        @Override
        public void authenticate() {
            // TODO: Implement provider-specific authentication
            // This could include:
            // - OAuth2 flow
            // - API key validation
            // - JWT token generation
            // - WebSocket handshake authentication
        }
        
        @Override
        public String getConnectionUrl() {
            // TODO: Return actual provider WebSocket URL
            return "wss://api.provider.com/ws";
        }
    }
    
    /**
     * Price data model
     */
    public static class PriceData {
        private final String symbol;
        private final double price;
        private final long timestamp;
        
        public PriceData(String symbol, double price, long timestamp) {
            this.symbol = symbol;
            this.price = price;
            this.timestamp = timestamp;
        }
        
        // Getters
        public String getSymbol() { return symbol; }
        public double getPrice() { return price; }
        public long getTimestamp() { return timestamp; }
        
        @Override
        public String toString() {
            return String.format("PriceData{symbol='%s', price=%.2f, timestamp=%d}", 
                               symbol, price, timestamp);
        }
    }
}
