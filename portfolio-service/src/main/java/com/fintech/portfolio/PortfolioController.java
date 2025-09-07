package com.fintech.portfolio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/portfolio")
@CrossOrigin(origins = "*") // REVIEW: production hardening required - configure specific origins
public class PortfolioController {

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private PnlCalculator pnlCalculator;

    /**
     * Get portfolio valuation for a user
     * GET /api/portfolio/{userId}/valuation
     */
    @GetMapping("/{userId}/valuation")
    public ResponseEntity<PortfolioValuation> getPortfolioValuation(@PathVariable Long userId) {
        try {
            PortfolioValuation valuation = portfolioService.calculatePortfolioValuation(userId);
            return ResponseEntity.ok(valuation);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PortfolioValuation(userId, 0.0, 0.0, 0.0, List.of()));
        }
    }

    /**
     * Get all holdings for a user
     * GET /api/portfolio/{userId}/holdings
     */
    @GetMapping("/{userId}/holdings")
    public ResponseEntity<List<Holding>> getHoldings(@PathVariable Long userId) {
        try {
            List<Holding> holdings = portfolioService.getHoldingsByUserId(userId);
            return ResponseEntity.ok(holdings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get a specific holding by ID
     * GET /api/portfolio/{userId}/holdings/{holdingId}
     */
    @GetMapping("/{userId}/holdings/{holdingId}")
    public ResponseEntity<Holding> getHolding(@PathVariable Long userId, @PathVariable Long holdingId) {
        try {
            Optional<Holding> holding = portfolioService.getHoldingById(holdingId);
            if (holding.isPresent() && holding.get().getUserId().equals(userId)) {
                return ResponseEntity.ok(holding.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create a new holding
     * POST /api/portfolio/{userId}/holdings
     */
    @PostMapping("/{userId}/holdings")
    public ResponseEntity<Holding> createHolding(@PathVariable Long userId, @RequestBody CreateHoldingRequest request) {
        try {
            Holding holding = new Holding();
            holding.setUserId(userId);
            holding.setSymbol(request.getSymbol());
            holding.setQuantity(request.getQuantity());
            holding.setAveragePrice(request.getAveragePrice());
            holding.setRealizedPnl(0.0); // Initialize with zero

            Holding savedHolding = portfolioService.createHolding(holding);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedHolding);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Update an existing holding
     * PUT /api/portfolio/{userId}/holdings/{holdingId}
     */
    @PutMapping("/{userId}/holdings/{holdingId}")
    public ResponseEntity<Holding> updateHolding(@PathVariable Long userId, @PathVariable Long holdingId, 
                                               @RequestBody UpdateHoldingRequest request) {
        try {
            Optional<Holding> existingHolding = portfolioService.getHoldingById(holdingId);
            if (existingHolding.isEmpty() || !existingHolding.get().getUserId().equals(userId)) {
                return ResponseEntity.notFound().build();
            }

            Holding holding = existingHolding.get();
            holding.setQuantity(request.getQuantity());
            holding.setAveragePrice(request.getAveragePrice());
            holding.setRealizedPnl(request.getRealizedPnl());

            Holding updatedHolding = portfolioService.updateHolding(holding);
            return ResponseEntity.ok(updatedHolding);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Delete a holding
     * DELETE /api/portfolio/{userId}/holdings/{holdingId}
     */
    @DeleteMapping("/{userId}/holdings/{holdingId}")
    public ResponseEntity<Void> deleteHolding(@PathVariable Long userId, @PathVariable Long holdingId) {
        try {
            Optional<Holding> existingHolding = portfolioService.getHoldingById(holdingId);
            if (existingHolding.isEmpty() || !existingHolding.get().getUserId().equals(userId)) {
                return ResponseEntity.notFound().build();
            }

            portfolioService.deleteHolding(holdingId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Add realized P&L (for manual sells)
     * POST /api/portfolio/{userId}/holdings/{holdingId}/realized-pnl
     */
    @PostMapping("/{userId}/holdings/{holdingId}/realized-pnl")
    public ResponseEntity<Holding> addRealizedPnl(@PathVariable Long userId, @PathVariable Long holdingId,
                                                 @RequestBody AddRealizedPnlRequest request) {
        try {
            Optional<Holding> existingHolding = portfolioService.getHoldingById(holdingId);
            if (existingHolding.isEmpty() || !existingHolding.get().getUserId().equals(userId)) {
                return ResponseEntity.notFound().build();
            }

            Holding holding = existingHolding.get();
            holding.setRealizedPnl(holding.getRealizedPnl() + request.getRealizedPnl());

            Holding updatedHolding = portfolioService.updateHolding(holding);
            return ResponseEntity.ok(updatedHolding);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Get P&L calculation for a specific holding
     * GET /api/portfolio/{userId}/holdings/{holdingId}/pnl
     */
    @GetMapping("/{userId}/holdings/{holdingId}/pnl")
    public ResponseEntity<PnlCalculation> getHoldingPnl(@PathVariable Long userId, @PathVariable Long holdingId) {
        try {
            Optional<Holding> holding = portfolioService.getHoldingById(holdingId);
            if (holding.isEmpty() || !holding.get().getUserId().equals(userId)) {
                return ResponseEntity.notFound().build();
            }

            PnlCalculation pnl = pnlCalculator.calculatePnl(holding.get());
            return ResponseEntity.ok(pnl);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Request/Response DTOs
    public static class CreateHoldingRequest {
        private String symbol;
        private Double quantity;
        private Double averagePrice;

        // Getters and setters
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        public Double getQuantity() { return quantity; }
        public void setQuantity(Double quantity) { this.quantity = quantity; }
        public Double getAveragePrice() { return averagePrice; }
        public void setAveragePrice(Double averagePrice) { this.averagePrice = averagePrice; }
    }

    public static class UpdateHoldingRequest {
        private Double quantity;
        private Double averagePrice;
        private Double realizedPnl;

        // Getters and setters
        public Double getQuantity() { return quantity; }
        public void setQuantity(Double quantity) { this.quantity = quantity; }
        public Double getAveragePrice() { return averagePrice; }
        public void setAveragePrice(Double averagePrice) { this.averagePrice = averagePrice; }
        public Double getRealizedPnl() { return realizedPnl; }
        public void setRealizedPnl(Double realizedPnl) { this.realizedPnl = realizedPnl; }
    }

    public static class AddRealizedPnlRequest {
        private Double realizedPnl;

        // Getters and setters
        public Double getRealizedPnl() { return realizedPnl; }
        public void setRealizedPnl(Double realizedPnl) { this.realizedPnl = realizedPnl; }
    }
}
