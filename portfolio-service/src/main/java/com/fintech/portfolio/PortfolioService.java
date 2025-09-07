package com.fintech.portfolio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PortfolioService {

    private static final Logger logger = LoggerFactory.getLogger(PortfolioService.class);

    @Autowired
    private HoldingRepository holdingRepository;

    @Autowired
    private PnlCalculator pnlCalculator;

    @Autowired
    private PriceCacheService priceCacheService;

    /**
     * Calculate portfolio valuation for a user
     * @param userId the user ID
     * @return PortfolioValuation with holdings and totals
     */
    public PortfolioValuation calculatePortfolioValuation(Long userId) {
        try {
            List<Holding> holdings = holdingRepository.findByUserId(userId);
            
            if (holdings.isEmpty()) {
                return new PortfolioValuation(userId, 0.0, 0.0, 0.0, List.of());
            }

            // Calculate total portfolio value
            Double totalPortfolioValue = pnlCalculator.calculateTotalPortfolioValue(holdings);
            
            // Calculate total unrealized P&L
            Double totalUnrealizedPnl = holdings.stream()
                    .mapToDouble(holding -> {
                        Double currentPrice = priceCacheService.getCurrentPrice(holding.getSymbol());
                        return pnlCalculator.calculateUnrealizedPnl(holding, currentPrice);
                    })
                    .sum();
            
            // Calculate total realized P&L
            Double totalRealizedPnl = holdings.stream()
                    .mapToDouble(holding -> holding.getRealizedPnl() != null ? holding.getRealizedPnl() : 0.0)
                    .sum();
            
            // Create holding valuations with allocation percentages
            List<HoldingValuation> holdingValuations = holdings.stream()
                    .map(holding -> createHoldingValuation(holding, totalPortfolioValue))
                    .collect(Collectors.toList());
            
            return new PortfolioValuation(
                userId,
                totalPortfolioValue,
                totalUnrealizedPnl,
                totalRealizedPnl,
                holdingValuations
            );
            
        } catch (Exception e) {
            logger.error("Error calculating portfolio valuation for user {}: {}", userId, e.getMessage());
            return new PortfolioValuation(userId, 0.0, 0.0, 0.0, List.of());
        }
    }

    /**
     * Create holding valuation with current price and allocation
     * @param holding the holding
     * @param totalPortfolioValue total portfolio value
     * @return HoldingValuation
     */
    private HoldingValuation createHoldingValuation(Holding holding, Double totalPortfolioValue) {
        // REVIEW: caching vs direct lookup - consider implementing price caching strategy
        Double currentPrice = priceCacheService.getCurrentPrice(holding.getSymbol());
        Double currentValue = pnlCalculator.calculateCurrentValue(holding, currentPrice);
        Double unrealizedPnl = pnlCalculator.calculateUnrealizedPnl(holding, currentPrice);
        Double allocationPercentage = pnlCalculator.calculateAllocationPercentage(holding, totalPortfolioValue);
        
        return new HoldingValuation(
            holding.getId(),
            holding.getSymbol(),
            holding.getQuantity(),
            holding.getAveragePrice(),
            currentPrice,
            currentValue,
            unrealizedPnl,
            holding.getRealizedPnl() != null ? holding.getRealizedPnl() : 0.0,
            allocationPercentage
        );
    }

    /**
     * Get all holdings for a user
     * @param userId the user ID
     * @return list of holdings
     */
    public List<Holding> getHoldingsByUserId(Long userId) {
        return holdingRepository.findByUserId(userId);
    }

    /**
     * Get a holding by ID
     * @param holdingId the holding ID
     * @return optional holding
     */
    public Optional<Holding> getHoldingById(Long holdingId) {
        return holdingRepository.findById(holdingId);
    }

    /**
     * Create a new holding
     * @param holding the holding to create
     * @return created holding
     */
    public Holding createHolding(Holding holding) {
        // Validate holding data
        validateHolding(holding);
        
        return holdingRepository.save(holding);
    }

    /**
     * Update an existing holding
     * @param holding the holding to update
     * @return updated holding
     */
    public Holding updateHolding(Holding holding) {
        // Validate holding data
        validateHolding(holding);
        
        return holdingRepository.save(holding);
    }

    /**
     * Delete a holding
     * @param holdingId the holding ID to delete
     */
    public void deleteHolding(Long holdingId) {
        holdingRepository.deleteById(holdingId);
    }

    /**
     * Validate holding data
     * @param holding the holding to validate
     */
    private void validateHolding(Holding holding) {
        if (holding.getUserId() == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (holding.getSymbol() == null || holding.getSymbol().trim().isEmpty()) {
            throw new IllegalArgumentException("Symbol cannot be null or empty");
        }
        if (holding.getQuantity() == null || holding.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (holding.getAveragePrice() == null || holding.getAveragePrice() <= 0) {
            throw new IllegalArgumentException("Average price must be positive");
        }
        if (holding.getRealizedPnl() == null) {
            holding.setRealizedPnl(0.0);
        }
    }

    /**
     * Portfolio valuation model
     */
    public static class PortfolioValuation {
        private Long userId;
        private Double totalValue;
        private Double totalUnrealizedPnl;
        private Double totalRealizedPnl;
        private List<HoldingValuation> holdings;

        public PortfolioValuation() {}

        public PortfolioValuation(Long userId, Double totalValue, Double totalUnrealizedPnl,
                                 Double totalRealizedPnl, List<HoldingValuation> holdings) {
            this.userId = userId;
            this.totalValue = totalValue;
            this.totalUnrealizedPnl = totalUnrealizedPnl;
            this.totalRealizedPnl = totalRealizedPnl;
            this.holdings = holdings;
        }

        // Getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public Double getTotalValue() { return totalValue; }
        public void setTotalValue(Double totalValue) { this.totalValue = totalValue; }
        
        public Double getTotalUnrealizedPnl() { return totalUnrealizedPnl; }
        public void setTotalUnrealizedPnl(Double totalUnrealizedPnl) { this.totalUnrealizedPnl = totalUnrealizedPnl; }
        
        public Double getTotalRealizedPnl() { return totalRealizedPnl; }
        public void setTotalRealizedPnl(Double totalRealizedPnl) { this.totalRealizedPnl = totalRealizedPnl; }
        
        public List<HoldingValuation> getHoldings() { return holdings; }
        public void setHoldings(List<HoldingValuation> holdings) { this.holdings = holdings; }
    }

    /**
     * Holding valuation model
     */
    public static class HoldingValuation {
        private Long holdingId;
        private String symbol;
        private Double quantity;
        private Double averagePrice;
        private Double currentPrice;
        private Double currentValue;
        private Double unrealizedPnl;
        private Double realizedPnl;
        private Double allocationPercentage;

        public HoldingValuation() {}

        public HoldingValuation(Long holdingId, String symbol, Double quantity, Double averagePrice,
                               Double currentPrice, Double currentValue, Double unrealizedPnl,
                               Double realizedPnl, Double allocationPercentage) {
            this.holdingId = holdingId;
            this.symbol = symbol;
            this.quantity = quantity;
            this.averagePrice = averagePrice;
            this.currentPrice = currentPrice;
            this.currentValue = currentValue;
            this.unrealizedPnl = unrealizedPnl;
            this.realizedPnl = realizedPnl;
            this.allocationPercentage = allocationPercentage;
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
        
        public Double getCurrentValue() { return currentValue; }
        public void setCurrentValue(Double currentValue) { this.currentValue = currentValue; }
        
        public Double getUnrealizedPnl() { return unrealizedPnl; }
        public void setUnrealizedPnl(Double unrealizedPnl) { this.unrealizedPnl = unrealizedPnl; }
        
        public Double getRealizedPnl() { return realizedPnl; }
        public void setRealizedPnl(Double realizedPnl) { this.realizedPnl = realizedPnl; }
        
        public Double getAllocationPercentage() { return allocationPercentage; }
        public void setAllocationPercentage(Double allocationPercentage) { this.allocationPercentage = allocationPercentage; }
    }
}
