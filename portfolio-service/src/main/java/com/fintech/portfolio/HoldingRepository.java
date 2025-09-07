package com.fintech.portfolio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HoldingRepository extends JpaRepository<Holding, Long> {

    /**
     * Find all holdings for a specific user
     * @param userId the user ID to search for
     * @return list of holdings for the user
     */
    List<Holding> findByUserId(Long userId);

    /**
     * Find holdings by user ID and symbol
     * @param userId the user ID
     * @param symbol the symbol to search for
     * @return list of holdings matching user and symbol
     */
    List<Holding> findByUserIdAndSymbol(Long userId, String symbol);

    /**
     * Find a specific holding by user ID and symbol
     * @param userId the user ID
     * @param symbol the symbol
     * @return optional holding if found
     */
    Optional<Holding> findFirstByUserIdAndSymbol(Long userId, String symbol);

    /**
     * Get total quantity for a user's symbol
     * @param userId the user ID
     * @param symbol the symbol
     * @return total quantity
     */
    @Query("SELECT COALESCE(SUM(h.quantity), 0) FROM Holding h WHERE h.userId = :userId AND h.symbol = :symbol")
    Double getTotalQuantityByUserIdAndSymbol(@Param("userId") Long userId, @Param("symbol") String symbol);

    /**
     * Get weighted average price for a user's symbol
     * @param userId the user ID
     * @param symbol the symbol
     * @return weighted average price
     */
    @Query("SELECT COALESCE(SUM(h.quantity * h.averagePrice) / SUM(h.quantity), 0) FROM Holding h WHERE h.userId = :userId AND h.symbol = :symbol")
    Double getWeightedAveragePriceByUserIdAndSymbol(@Param("userId") Long userId, @Param("symbol") String symbol);

    /**
     * Get total realized P&L for a user
     * @param userId the user ID
     * @return total realized P&L
     */
    @Query("SELECT COALESCE(SUM(h.realizedPnl), 0) FROM Holding h WHERE h.userId = :userId")
    Double getTotalRealizedPnlByUserId(@Param("userId") Long userId);

    /**
     * Get distinct symbols for a user
     * @param userId the user ID
     * @return list of distinct symbols
     */
    @Query("SELECT DISTINCT h.symbol FROM Holding h WHERE h.userId = :userId")
    List<String> getDistinctSymbolsByUserId(@Param("userId") Long userId);

    /**
     * Check if user has any holdings
     * @param userId the user ID
     * @return true if user has holdings, false otherwise
     */
    boolean existsByUserId(Long userId);

    /**
     * Count holdings for a user
     * @param userId the user ID
     * @return number of holdings
     */
    long countByUserId(Long userId);

    /**
     * Delete all holdings for a user
     * @param userId the user ID
     */
    void deleteByUserId(Long userId);
}
