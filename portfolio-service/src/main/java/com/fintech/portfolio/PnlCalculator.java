package com.fintech.portfolio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PnlCalculator {

    private static final Logger logger = LoggerFactory.getLogger(PnlCalculator.class);

    @Autowired
    private PriceCacheService priceCacheService;

    /**
     * Calculate P&L for a holding
     * @param holding the holding to calculate P&L for
     * @return PnlCalculation with unrealized and realized P&L
     */
    public PnlCalculation calculatePnl(Holding holding) {
        try {
            // Get current price from cache
            Double currentPrice = priceCacheService.getCurrentPrice(holding.getSymbol());
            
            // Calculate unrealized P&L: (currentPrice - avgPrice) * qty
            Double unrealizedPnl = calculateUnrealizedPnl(holding, currentPrice);
            
            // Get realized P&L from holding
            Double realizedPnl = holding.getRealizedPnl() != null ? holding.getRealizedPnl() : 0.0;
            
            // Calculate total P&L
            Double totalPnl = unrealizedPnl + realizedPnl;
            
            // Calculate current value
            Double currentValue = calculateCurrentValue(holding, currentPrice);
            
            return new PnlCalculation(
                holding.getId(),
                holding.getSymbol(),
                holding.getQuantity(),
                holding.getAveragePrice(),
                currentPrice,
                unrealizedPnl,
                realizedPnl,
                totalPnl,
                currentValue
            );
            
        } catch (Exception e) {
            logger.error("Error calculating P&L for holding {}: {}", holding.getId(), e.getMessage());
            return createErrorPnlCalculation(holding);
        }
    }

    /**
     * Calculate unrealized P&L
     * @param holding the holding
     * @param currentPrice the current market price
     * @return unrealized P&L
     */
    public Double calculateUnrealizedPnl(Holding holding, Double currentPrice) {
        if (currentPrice == null) {
            logger.warn("Current price is null for symbol: {}", holding.getSymbol());
            return 0.0;
        }
        
        if (holding.getQuantity() == null || holding.getQuantity() == 0) {
            logger.warn("Quantity is null or zero for holding: {}", holding.getId());
            return 0.0;
        }
        
        if (holding.getAveragePrice() == null) {
            logger.warn("Average price is null for holding: {}", holding.getId());
            return 0.0;
        }
        
        // P&L formula: (currentPrice - avgPrice) * qty
        return (currentPrice - holding.getAveragePrice()) * holding.getQuantity();
    }

    /**
     * Calculate current value of holding
     * @param holding the holding
     * @param currentPrice the current market price
     * @return current value
     */
    public Double calculateCurrentValue(Holding holding, Double currentPrice) {
        if (currentPrice == null || holding.getQuantity() == null) {
            return 0.0;
        }
        
        return currentPrice * holding.getQuantity();
    }

    /**
     * Calculate total portfolio value
     * @param holdings list of holdings
     * @return total portfolio value
     */
    public Double calculateTotalPortfolioValue(java.util.List<Holding> holdings) {
        return holdings.stream()
                .mapToDouble(holding -> {
                    Double currentPrice = priceCacheService.getCurrentPrice(holding.getSymbol());
                    return calculateCurrentValue(holding, currentPrice);
                })
                .sum();
    }

    /**
     * Calculate allocation percentage for a holding
     * @param holding the holding
     * @param totalPortfolioValue total portfolio value
     * @return allocation percentage
     */
    public Double calculateAllocationPercentage(Holding holding, Double totalPortfolioValue) {
        if (totalPortfolioValue == null || totalPortfolioValue == 0) {
            return 0.0;
        }
        
        Double currentPrice = priceCacheService.getCurrentPrice(holding.getSymbol());
        Double currentValue = calculateCurrentValue(holding, currentPrice);
        
        return (currentValue / totalPortfolioValue) * 100.0;
    }

    /**
     * Create error P&L calculation when calculation fails
     * @param holding the holding
     * @return error P&L calculation
     */
    private PnlCalculation createErrorPnlCalculation(Holding holding) {
        return new PnlCalculation(
            holding.getId(),
            holding.getSymbol(),
            holding.getQuantity(),
            holding.getAveragePrice(),
            null, // currentPrice
            0.0,  // unrealizedPnl
            holding.getRealizedPnl() != null ? holding.getRealizedPnl() : 0.0,
            holding.getRealizedPnl() != null ? holding.getRealizedPnl() : 0.0,
            0.0   // currentValue
        );
    }

    /**
     * P&L calculation result model
     */
    public static class PnlCalculation {
        private Long holdingId;
        private String symbol;
        private Double quantity;
        private Double averagePrice;
        private Double currentPrice;
        private Double unrealizedPnl;
        private Double realizedPnl;
        private Double totalPnl;
        private Double currentValue;

        public PnlCalculation() {}

        public PnlCalculation(Long holdingId, String symbol, Double quantity, Double averagePrice,
                             Double currentPrice, Double unrealizedPnl, Double realizedPnl,
                             Double totalPnl, Double currentValue) {
            this.holdingId = holdingId;
            this.symbol = symbol;
            this.quantity = quantity;
            this.averagePrice = averagePrice;
            this.currentPrice = currentPrice;
            this.unrealizedPnl = unrealizedPnl;
            this.realizedPnl = realizedPnl;
            this.totalPnl = totalPnl;
            this.currentValue = currentValue;
        }

        // Getters and setters
        public Long getHoldingId() { return holdingId; }
        public void setHoldingId(Long holdingId) { this.holdingId = holdingId; }
        
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        
        public Double getQuantity() { return quantity; }
        public void setQuantity(Double quantity) { this.quantity = quantity; }
        
        public Double getAveragePrice() { return averagePrice; }
        public void setAveragePrice(Double averagePrice) { this.averagePrice = averagePrice; }
        
        public Double getCurrentPrice() { return currentPrice; }
        public void setCurrentPrice(Double currentPrice) { this.currentPrice = currentPrice; }
        
        public Double getUnrealizedPnl() { return unrealizedPnl; }
        public void setUnrealizedPnl(Double unrealizedPnl) { this.unrealizedPnl = unrealizedPnl; }
        
        public Double getRealizedPnl() { return realizedPnl; }
        public void setRealizedPnl(Double realizedPnl) { this.realizedPnl = realizedPnl; }
        
        public Double getTotalPnl() { return totalPnl; }
        public void setTotalPnl(Double totalPnl) { this.totalPnl = totalPnl; }
        
        public Double getCurrentValue() { return currentValue; }
        public void setCurrentValue(Double currentValue) { this.currentValue = currentValue; }

        @Override
        public String toString() {
            return String.format("PnlCalculation{holdingId=%d, symbol='%s', qty=%.2f, avgPrice=%.2f, " +
                               "currentPrice=%.2f, unrealizedPnl=%.2f, realizedPnl=%.2f, " +
                               "totalPnl=%.2f, currentValue=%.2f}",
                               holdingId, symbol, quantity, averagePrice, currentPrice,
                               unrealizedPnl, realizedPnl, totalPnl, currentValue);
        }
    }
}
