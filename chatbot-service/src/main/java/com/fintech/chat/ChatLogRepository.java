package com.fintech.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatLogRepository extends JpaRepository<ChatLog, Long> {

    /**
     * Find chat logs by user ID ordered by timestamp descending
     * @param userId the user ID
     * @return list of chat logs
     */
    List<ChatLog> findByUserIdOrderByTimestampDesc(Long userId);

    /**
     * Find chat logs by user ID ordered by timestamp ascending
     * @param userId the user ID
     * @return list of chat logs
     */
    List<ChatLog> findByUserIdOrderByTimestampAsc(Long userId);

    /**
     * Find chat logs by user ID and date range
     * @param userId the user ID
     * @param startDate start date
     * @param endDate end date
     * @return list of chat logs in the date range
     */
    @Query("SELECT c FROM ChatLog c WHERE c.userId = :userId AND c.timestamp BETWEEN :startDate AND :endDate ORDER BY c.timestamp DESC")
    List<ChatLog> findByUserIdAndTimestampBetween(@Param("userId") Long userId, 
                                                 @Param("startDate") LocalDateTime startDate, 
                                                 @Param("endDate") LocalDateTime endDate);

    /**
     * Count chat logs by user ID
     * @param userId the user ID
     * @return count of chat logs
     */
    long countByUserId(Long userId);

    /**
     * Find recent chat logs for a user
     * @param userId the user ID
     * @param limit maximum number of results
     * @return list of recent chat logs
     */
    @Query("SELECT c FROM ChatLog c WHERE c.userId = :userId ORDER BY c.timestamp DESC")
    List<ChatLog> findRecentChatLogs(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * Find chat logs with high response time
     * @param minResponseTime minimum response time in milliseconds
     * @return list of chat logs with high response time
     */
    @Query("SELECT c FROM ChatLog c WHERE c.responseTimeMs >= :minResponseTime ORDER BY c.responseTimeMs DESC")
    List<ChatLog> findByResponseTimeGreaterThanEqual(@Param("minResponseTime") Long minResponseTime);

    /**
     * Find chat logs by date range
     * @param startDate start date
     * @param endDate end date
     * @return list of chat logs in the date range
     */
    @Query("SELECT c FROM ChatLog c WHERE c.timestamp BETWEEN :startDate AND :endDate ORDER BY c.timestamp DESC")
    List<ChatLog> findByTimestampBetween(@Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate);

    /**
     * Count chat logs by date range
     * @param startDate start date
     * @param endDate end date
     * @return count of chat logs in the date range
     */
    @Query("SELECT COUNT(c) FROM ChatLog c WHERE c.timestamp BETWEEN :startDate AND :endDate")
    long countByTimestampBetween(@Param("startDate") LocalDateTime startDate, 
                                @Param("endDate") LocalDateTime endDate);

    /**
     * Get average response time by user
     * @param userId the user ID
     * @return average response time in milliseconds
     */
    @Query("SELECT AVG(c.responseTimeMs) FROM ChatLog c WHERE c.userId = :userId AND c.responseTimeMs IS NOT NULL")
    Double getAverageResponseTimeByUserId(@Param("userId") Long userId);

    /**
     * Get total tokens used by user
     * @param userId the user ID
     * @return total tokens used
     */
    @Query("SELECT COALESCE(SUM(c.tokensUsed), 0) FROM ChatLog c WHERE c.userId = :userId AND c.tokensUsed IS NOT NULL")
    Long getTotalTokensByUserId(@Param("userId") Long userId);

    /**
     * Find chat logs with sources
     * @param userId the user ID
     * @return list of chat logs that have sources
     */
    @Query("SELECT c FROM ChatLog c WHERE c.userId = :userId AND c.sources IS NOT NULL AND c.sources != '' ORDER BY c.timestamp DESC")
    List<ChatLog> findByUserIdWithSources(@Param("userId") Long userId);

    /**
     * Delete chat logs older than specified date
     * @param cutoffDate the cutoff date
     * @return number of deleted records
     */
    @Query("DELETE FROM ChatLog c WHERE c.timestamp < :cutoffDate")
    int deleteByTimestampBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
}
