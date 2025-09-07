package com.fintech.sip;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SipRepository extends JpaRepository<Sip, Long> {

    /**
     * Find all due SIPs (next due date is today or in the past)
     * @return list of due SIPs
     */
    @Query("SELECT s FROM Sip s WHERE s.nextDueDate <= :currentDate AND s.status = 'ACTIVE'")
    List<Sip> findDueSips(@Param("currentDate") LocalDateTime currentDate);

    /**
     * Find all due SIPs using current timestamp
     * @return list of due SIPs
     */
    @Query("SELECT s FROM Sip s WHERE s.nextDueDate <= CURRENT_TIMESTAMP AND s.status = 'ACTIVE'")
    List<Sip> findDueSips();

    /**
     * Find all active SIPs
     * @return list of active SIPs
     */
    List<Sip> findByStatus(SipStatus status);

    /**
     * Find all active SIPs
     * @return list of active SIPs
     */
    @Query("SELECT s FROM Sip s WHERE s.status = 'ACTIVE'")
    List<Sip> findActiveSips();

    /**
     * Find SIPs by user ID
     * @param userId the user ID
     * @return list of SIPs for the user
     */
    List<Sip> findByUserId(Long userId);

    /**
     * Find active SIPs by user ID
     * @param userId the user ID
     * @return list of active SIPs for the user
     */
    @Query("SELECT s FROM Sip s WHERE s.userId = :userId AND s.status = 'ACTIVE'")
    List<Sip> findActiveSipsByUserId(@Param("userId") Long userId);

    /**
     * Find SIPs by user ID and status
     * @param userId the user ID
     * @param status the SIP status
     * @return list of SIPs matching criteria
     */
    List<Sip> findByUserIdAndStatus(Long userId, SipStatus status);

    /**
     * Find SIPs by frequency
     * @param frequency the SIP frequency
     * @return list of SIPs with the frequency
     */
    List<Sip> findByFrequency(SipFrequency frequency);

    /**
     * Find SIPs due within a specific date range
     * @param startDate start of date range
     * @param endDate end of date range
     * @return list of SIPs due in the range
     */
    @Query("SELECT s FROM Sip s WHERE s.nextDueDate BETWEEN :startDate AND :endDate AND s.status = 'ACTIVE'")
    List<Sip> findSipsDueBetween(@Param("startDate") LocalDateTime startDate, 
                                @Param("endDate") LocalDateTime endDate);

    /**
     * Count active SIPs by user
     * @param userId the user ID
     * @return count of active SIPs
     */
    @Query("SELECT COUNT(s) FROM Sip s WHERE s.userId = :userId AND s.status = 'ACTIVE'")
    long countActiveSipsByUserId(@Param("userId") Long userId);

    /**
     * Count SIPs by status
     * @param status the SIP status
     * @return count of SIPs with the status
     */
    long countByStatus(SipStatus status);

    /**
     * Find SIPs that haven't been processed for a long time
     * @param cutoffDate cutoff date for last processed
     * @return list of stale SIPs
     */
    @Query("SELECT s FROM Sip s WHERE s.lastProcessedAt < :cutoffDate AND s.status = 'ACTIVE'")
    List<Sip> findStaleSips(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find SIPs by symbol
     * @param symbol the investment symbol
     * @return list of SIPs for the symbol
     */
    List<Sip> findBySymbol(String symbol);

    /**
     * Find SIPs by user and symbol
     * @param userId the user ID
     * @param symbol the investment symbol
     * @return list of SIPs matching criteria
     */
    List<Sip> findByUserIdAndSymbol(Long userId, String symbol);

    /**
     * Get total SIP amount by user
     * @param userId the user ID
     * @return total SIP amount
     */
    @Query("SELECT COALESCE(SUM(s.amount), 0) FROM Sip s WHERE s.userId = :userId AND s.status = 'ACTIVE'")
    Double getTotalSipAmountByUserId(@Param("userId") Long userId);

    /**
     * Get total SIP amount by symbol
     * @param symbol the investment symbol
     * @return total SIP amount
     */
    @Query("SELECT COALESCE(SUM(s.amount), 0) FROM Sip s WHERE s.symbol = :symbol AND s.status = 'ACTIVE'")
    Double getTotalSipAmountBySymbol(@Param("symbol") String symbol);

    /**
     * Find SIPs with next due date in the next N days
     * @param days number of days ahead
     * @return list of upcoming SIPs
     */
    @Query("SELECT s FROM Sip s WHERE s.nextDueDate BETWEEN CURRENT_TIMESTAMP AND :futureDate AND s.status = 'ACTIVE'")
    List<Sip> findUpcomingSips(@Param("futureDate") LocalDateTime futureDate);

    /**
     * Delete SIPs by user ID
     * @param userId the user ID
     */
    void deleteByUserId(Long userId);

    /**
     * Check if user has any active SIPs
     * @param userId the user ID
     * @return true if user has active SIPs
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Sip s WHERE s.userId = :userId AND s.status = 'ACTIVE'")
    boolean hasActiveSips(@Param("userId") Long userId);
}
