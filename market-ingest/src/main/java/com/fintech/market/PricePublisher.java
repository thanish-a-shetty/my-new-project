package com.fintech.market;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class PricePublisher {

    private static final Logger logger = LoggerFactory.getLogger(PricePublisher.class);
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    /**
     * Publish price data to STOMP topic
     * @param priceData the price data to publish
     */
    public void publishPrice(WebSocketClientService.PriceData priceData) {
        try {
            String topic = "/topic/price/" + priceData.getSymbol();
            
            // Create price message
            PriceMessage priceMessage = new PriceMessage(
                priceData.getSymbol(),
                priceData.getPrice(),
                priceData.getTimestamp(),
                System.currentTimeMillis()
            );
            
            // Publish to STOMP topic
            messagingTemplate.convertAndSend(topic, priceMessage);
            
            logger.debug("Published price to topic {}: {}", topic, priceMessage);
            
        } catch (Exception e) {
            logger.error("Error publishing price for symbol {}: {}", 
                        priceData.getSymbol(), e.getMessage(), e);
        }
    }
    
    /**
     * Publish price data with custom topic
     * @param topic the STOMP topic to publish to
     * @param priceData the price data to publish
     */
    public void publishPrice(String topic, WebSocketClientService.PriceData priceData) {
        try {
            PriceMessage priceMessage = new PriceMessage(
                priceData.getSymbol(),
                priceData.getPrice(),
                priceData.getTimestamp(),
                System.currentTimeMillis()
            );
            
            messagingTemplate.convertAndSend(topic, priceMessage);
            
            logger.debug("Published price to custom topic {}: {}", topic, priceMessage);
            
        } catch (Exception e) {
            logger.error("Error publishing price to topic {}: {}", 
                        topic, e.getMessage(), e);
        }
    }
    
    /**
     * Publish error message to error topic
     * @param symbol the symbol that had an error
     * @param errorMessage the error message
     */
    public void publishError(String symbol, String errorMessage) {
        try {
            String topic = "/topic/error/" + symbol;
            ErrorMessage errorMsg = new ErrorMessage(symbol, errorMessage, System.currentTimeMillis());
            
            messagingTemplate.convertAndSend(topic, errorMsg);
            
            logger.warn("Published error to topic {}: {}", topic, errorMsg);
            
        } catch (Exception e) {
            logger.error("Error publishing error message for symbol {}: {}", 
                        symbol, e.getMessage(), e);
        }
    }
    
    /**
     * Publish connection status
     * @param status the connection status
     * @param message additional status message
     */
    public void publishConnectionStatus(String status, String message) {
        try {
            String topic = "/topic/status/connection";
            StatusMessage statusMsg = new StatusMessage(status, message, System.currentTimeMillis());
            
            messagingTemplate.convertAndSend(topic, statusMsg);
            
            logger.info("Published connection status: {}", statusMsg);
            
        } catch (Exception e) {
            logger.error("Error publishing connection status: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Price message model for STOMP
     */
    public static class PriceMessage {
        private String symbol;
        private double price;
        private long marketTimestamp;
        private long publishTimestamp;
        
        public PriceMessage() {}
        
        public PriceMessage(String symbol, double price, long marketTimestamp, long publishTimestamp) {
            this.symbol = symbol;
            this.price = price;
            this.marketTimestamp = marketTimestamp;
            this.publishTimestamp = publishTimestamp;
        }
        
        // Getters and setters
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        
        public long getMarketTimestamp() { return marketTimestamp; }
        public void setMarketTimestamp(long marketTimestamp) { this.marketTimestamp = marketTimestamp; }
        
        public long getPublishTimestamp() { return publishTimestamp; }
        public void setPublishTimestamp(long publishTimestamp) { this.publishTimestamp = publishTimestamp; }
        
        @Override
        public String toString() {
            return String.format("PriceMessage{symbol='%s', price=%.2f, marketTs=%d, publishTs=%d}", 
                               symbol, price, marketTimestamp, publishTimestamp);
        }
    }
    
    /**
     * Error message model for STOMP
     */
    public static class ErrorMessage {
        private String symbol;
        private String errorMessage;
        private long timestamp;
        
        public ErrorMessage() {}
        
        public ErrorMessage(String symbol, String errorMessage, long timestamp) {
            this.symbol = symbol;
            this.errorMessage = errorMessage;
            this.timestamp = timestamp;
        }
        
        // Getters and setters
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        
        @Override
        public String toString() {
            return String.format("ErrorMessage{symbol='%s', error='%s', timestamp=%d}", 
                               symbol, errorMessage, timestamp);
        }
    }
    
    /**
     * Status message model for STOMP
     */
    public static class StatusMessage {
        private String status;
        private String message;
        private long timestamp;
        
        public StatusMessage() {}
        
        public StatusMessage(String status, String message, long timestamp) {
            this.status = status;
            this.message = message;
            this.timestamp = timestamp;
        }
        
        // Getters and setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        
        @Override
        public String toString() {
            return String.format("StatusMessage{status='%s', message='%s', timestamp=%d}", 
                               status, message, timestamp);
        }
    }
}
